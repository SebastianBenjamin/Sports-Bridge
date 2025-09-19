from typing import Dict, Any
from ai.utils import db

async def run(params: Dict[str, Any]) -> Dict[str, Any]:
    athlete_id = params.get("athlete_id")
    if not athlete_id:
        return {"error": "athlete_id is required"}

    # Fetch minimal details from users table (source of truth in existing schema)
    user = await db.fetch_one(
        "SELECT id, first_name, last_name, email, role, created_at AS \"createdAt\" FROM users WHERE id = :id",
        {"id": athlete_id},
    )
    if not user:
        return {"error": "athlete not found", "athlete_id": athlete_id}

    # Basic safe summary
    full_name = f"{user.get('first_name') or ''} {user.get('last_name') or ''}".strip()

    # Optional stats (wrapped separately to avoid failures if tables missing)
    posts_count = 0
    try:
        row = await db.fetch_one("SELECT COUNT(*) AS c FROM sb_posts", {})
        if row:
            posts_count = int(row.get("c") or 0)
    except Exception:
        posts_count = 0

    return {
        "athlete_id": user["id"],
        "name": full_name or user.get("email"),
        "role": user.get("role"),
        "member_since": str(user.get("createdAt")),
        "stats": {
            "posts_count_sample": posts_count
        }
    }
