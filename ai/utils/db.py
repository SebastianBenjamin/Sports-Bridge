import os
from typing import Any, Dict, Optional, Sequence
from sqlalchemy.ext.asyncio import AsyncEngine, create_async_engine, AsyncSession
from sqlalchemy import text
from sqlalchemy.engine import Result

DATABASE_URL = os.getenv("DATABASE_URL") or "postgresql+asyncpg://postgres:admin@localhost:5432/sportsbridge"

_engine: Optional[AsyncEngine] = None


def get_engine() -> AsyncEngine:
    global _engine
    if _engine is None:
        _engine = create_async_engine(DATABASE_URL, pool_pre_ping=True, pool_size=5, max_overflow=5)
    return _engine


async def fetch_all(sql: str, params: Optional[Dict[str, Any]] = None) -> Sequence[Dict[str, Any]]:
    engine = get_engine()
    async with engine.connect() as conn:
        result: Result = await conn.execute(text(sql), params or {})
        rows = result.mappings().all()
        return [dict(r) for r in rows]


async def fetch_one(sql: str, params: Optional[Dict[str, Any]] = None) -> Optional[Dict[str, Any]]:
    engine = get_engine()
    async with engine.connect() as conn:
        result: Result = await conn.execute(text(sql), params or {})
        row = result.mappings().first()
        return dict(row) if row else None


async def execute(sql: str, params: Optional[Dict[str, Any]] = None) -> None:
    engine = get_engine()
    async with engine.begin() as conn:
        await conn.execute(text(sql), params or {})

