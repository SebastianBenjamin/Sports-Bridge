import os
import json
from typing import Any, Dict, Optional
from fastapi import FastAPI, BackgroundTasks, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv

# Local utils and workflows
from utils import db as db_utils
from workflows import athlete_summary, coach_insights, sponsor_recommendation, admin_report

load_dotenv()

LANGGRAPH_KEY = os.getenv("LANGGRAPH_KEY")

app = FastAPI(title="SportsBridge AI Service", version="0.1.0")


class RunWorkflowBody(BaseModel):
    name: str
    params: Dict[str, Any] | None = None


WORKFLOWS = {
    "athlete_summary": athlete_summary.run,
    "coach_insights": coach_insights.run,
    "sponsor_recommendation": sponsor_recommendation.run,
    "admin_report": admin_report.run,
}


async def _update_result_status(result_id: int, status: str, payload: Optional[dict] = None):
    payload_json = json.dumps(payload) if payload is not None else None
    await db_utils.execute(
        """
        UPDATE ai_results
        SET status = :status, payload_json = :payload_json
        WHERE id = :id
        """,
        {"status": status, "payload_json": payload_json, "id": result_id},
    )


async def _run_and_persist(workflow_name: str, params: Optional[Dict[str, Any]], result_id: int):
    try:
        runner = WORKFLOWS.get(workflow_name)
        if not runner:
            raise ValueError(f"Unknown workflow: {workflow_name}")
        result = await runner(params or {})
        await _update_result_status(result_id, "COMPLETED", result)
    except Exception as e:
        await _update_result_status(result_id, "FAILED", {"error": str(e)})


@app.post("/run-workflow")
async def run_workflow(body: RunWorkflowBody, background: BackgroundTasks):
    # Insert a pending ai_results row
    row = await db_utils.fetch_one(
        """
        INSERT INTO ai_results (workflow_name, status, payload_json, created_at)
        VALUES (:workflow_name, :status, NULL, NOW())
        RETURNING id
        """,
        {"workflow_name": body.name, "status": "PENDING"},
    )
    result_id = row["id"] if isinstance(row, dict) else row[0]

    background.add_task(_run_and_persist, body.name, body.params or {}, result_id)
    return {"id": result_id, "status": "PENDING"}


@app.get("/ai-results/{result_id}")
async def get_ai_result(result_id: int):
    row = await db_utils.fetch_one(
        """
        SELECT id, workflow_name, status, payload_json, created_at
        FROM ai_results WHERE id = :id
        """,
        {"id": result_id},
    )
    if not row:
        raise HTTPException(status_code=404, detail="Result not found")

    # Parse JSON payload if string
    payload = row["payload_json"] if isinstance(row, dict) else row[3]
    try:
        if isinstance(payload, str):
            payload = json.loads(payload)
    except Exception:
        pass

    return {
        "id": row["id"] if isinstance(row, dict) else row[0],
        "workflow_name": row["workflow_name"] if isinstance(row, dict) else row[1],
        "status": row["status"] if isinstance(row, dict) else row[2],
        "payload": payload,
        "created_at": row["created_at"] if isinstance(row, dict) else row[4],
    }


@app.get("/sponsor-recommendations")
async def sponsor_recommendations():
    # Immediate computation (no persistence)
    try:
        result = await sponsor_recommendation.run({})
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/")
async def root():
    return {"service": "ai", "ok": True}
