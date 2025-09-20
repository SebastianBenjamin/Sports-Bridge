"""
Database connection and utility functions.
"""
import aiomysql
import asyncio
import logging
from typing import List, Dict, Any, Optional
from app.config import settings

logger = logging.getLogger(__name__)


class DatabaseConnection:
    """MySQL database connection manager."""
    
    def __init__(self):
        self.pool = None
    
    async def create_pool(self):
        """Create database connection pool."""
        try:
            self.pool = await aiomysql.create_pool(
                host=settings.DB_HOST,
                port=settings.DB_PORT,
                user=settings.DB_USER,
                password=settings.DB_PASSWORD,
                db=settings.DB_NAME,
                charset='utf8mb4',
                autocommit=True,
                maxsize=10,
                minsize=1
            )
            logger.info("Database connection pool created successfully")
        except Exception as e:
            logger.error(f"Failed to create database pool: {e}")
            raise
    
    async def close_pool(self):
        """Close database connection pool."""
        if self.pool:
            self.pool.close()
            await self.pool.wait_closed()
            logger.info("Database connection pool closed")
    
    async def execute_query(self, query: str, params: Optional[tuple] = None) -> List[Dict[str, Any]]:
        """Execute a SELECT query and return results."""
        if not self.pool:
            await self.create_pool()
        
        async with self.pool.acquire() as conn:
            async with conn.cursor(aiomysql.DictCursor) as cursor:
                await cursor.execute(query, params)
                result = await cursor.fetchall()
                return result
    
    async def execute_update(self, query: str, params: Optional[tuple] = None) -> int:
        """Execute an INSERT/UPDATE/DELETE query and return affected rows."""
        if not self.pool:
            await self.create_pool()
        
        async with self.pool.acquire() as conn:
            async with conn.cursor() as cursor:
                affected_rows = await cursor.execute(query, params)
                await conn.commit()
                return affected_rows


# Global database instance
db = DatabaseConnection()


async def get_database() -> DatabaseConnection:
    """Get database connection instance."""
    if not db.pool:
        await db.create_pool()
    return db
