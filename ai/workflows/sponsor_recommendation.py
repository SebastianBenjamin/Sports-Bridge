from typing import Dict, Any, List
from math import isfinite
from ai.utils import db

# Scoring: 0.45*Performance + 0.20*Experience + 0.15*Achievements + 0.10*Marketability + 0.10*Health
# All inputs normalized to 0..1

async def _get_players() -> List[dict]:
    rows = await db.fetch_all("""
        SELECT id, first_name, last_name, email, profile_image_url AS "profileImageUrl", created_at AS "createdAt"
        FROM users WHERE role = 'ATHELETE'
    """)
    for r in rows:
        r["name"] = f"{r.get('first_name') or ''} {r.get('last_name') or ''}".strip() or r.get("email")
    return rows


def _normalize(vals: List[float]) -> List[float]:
    if not vals:
        return []
    finite_vals = [v for v in vals if isfinite(v)]
    if not finite_vals:
        return [0.0 for _ in vals]
    vmin, vmax = min(finite_vals), max(finite_vals)
    if vmax - vmin == 0:
        return [0.5 for _ in vals]
    return [(v - vmin) / (vmax - vmin) for v in vals]


async def run(params: Dict[str, Any]) -> Dict[str, Any]:
    players = await _get_players()
    if not players:
        return {"players": [], "note": "no players found"}

    # Proxy metrics (safe placeholders):
    # - Performance: proxy by presence of profile image (1.0) vs none (0.4)
    # - Experience: account age in days
    # - Achievements: constant 0.2 (placeholder)
    # - Marketability: email present (1.0) else 0.5
    # - Health: constant 0.9 (placeholder)
    perf_raw = [1.0 if p.get("profileImageUrl") else 0.4 for p in players]
    # days since createdAt; if null, use 0
    import datetime as dt
    now = dt.datetime.utcnow()
    def days_since(d):
        try:
            if not d:
                return 0.0
            # d may be a datetime or string
            if isinstance(d, str):
                try:
                    return (now - dt.datetime.fromisoformat(d.replace('Z',''))).days
                except Exception:
                    return 0.0
            return (now - d).days
        except Exception:
            return 0.0
    exp_raw = [float(days_since(p.get("createdAt"))) for p in players]
    ach_raw = [0.2 for _ in players]
    mkt_raw = [1.0 if p.get("email") else 0.5 for p in players]
    hlth_raw = [0.9 for _ in players]

    perf = _normalize(perf_raw)
    exp = _normalize(exp_raw)
    ach = _normalize(ach_raw)
    mkt = _normalize(mkt_raw)
    hlth = _normalize(hlth_raw)

    ranked = []
    for i, p in enumerate(players):
        score = 0.45*perf[i] + 0.20*exp[i] + 0.15*ach[i] + 0.10*mkt[i] + 0.10*hlth[i]
        ranked.append({
            "player_id": p["id"],
            "name": p.get("name"),
            "score": round(float(score), 4),
            "components": {
                "performance": round(float(perf[i]), 4),
                "experience": round(float(exp[i]), 4),
                "achievements": round(float(ach[i]), 4),
                "marketability": round(float(mkt[i]), 4),
                "health": round(float(hlth[i]), 4)
            }
        })

    ranked.sort(key=lambda x: x["score"], reverse=True)
    return {"players": ranked}
