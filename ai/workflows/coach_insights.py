from typing import Dict, Any
from ai.utils import db

async def run(params: Dict[str, Any]) -> Dict[str, Any]:
    coach_id = params.get("coach_id")
    if not coach_id:
        return {"error": "coach_id is required"}

    coach = await db.fetch_one(
        "SELECT id, first_name, last_name, email, role, createdAt FROM users WHERE id = :id",
        {"id": coach_id},
    )
    if not coach:
        return {"error": "coach not found", "coach_id": coach_id}

    full_name = f"{coach.get('first_name') or ''} {coach.get('last_name') or ''}".strip()

    # Placeholder: count of athletes present
    athletes_count = 0
    try:
        row = await db.fetch_one("SELECT COUNT(*) AS c FROM users WHERE role = 'ATHELETE'", {})
        if row:
            athletes_count = int(row.get("c") or 0)
    except Exception:
        pass

    return {
        "coach_id": coach["id"],
        "name": full_name or coach.get("email"),
        "role": coach.get("role"),
        "insights": {
            "total_athletes": athletes_count,
            "note": "Sample data for development"
        }
    }
