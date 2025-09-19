from typing import Dict, Any
from ai.utils import db

async def run(params: Dict[str, Any]) -> Dict[str, Any]:
    # Basic counts and health checks
    users_count = 0
    posts_count = 0
    try:
        row = await db.fetch_one("SELECT COUNT(*) AS c FROM users", {})
        users_count = int(row.get("c") or 0) if row else 0
    except Exception:
        pass
    try:
        row = await db.fetch_one("SELECT COUNT(*) AS c FROM sb_posts", {})
        posts_count = int(row.get("c") or 0) if row else 0
    except Exception:
        pass

    return {
        "totals": {
            "users": users_count,
            "posts": posts_count
        },
        "status": "ok"
    }
