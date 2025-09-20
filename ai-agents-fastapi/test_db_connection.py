#!/usr/bin/env python3

import asyncio
import aiomysql
import sys
import os

# Add the app directory to path
sys.path.append('/home/zaibonk/Desktop/plan-c/plan-c/Sports-Bridge/ai-agents-fastapi')

async def test_database_connection():
    """Test direct database connection and check for data."""
    try:
        # Database connection settings (from your config)
        connection = await aiomysql.connect(
            host='localhost',
            port=3306,
            user='root',git 
            password='Baymaxhiro@156',
            db='sportsbridge',
            charset='utf8mb4'
        )
        
        print("✅ Database connection successful!")
        
        async with connection.cursor(aiomysql.DictCursor) as cursor:
            # Check if athletes table exists
            await cursor.execute("SHOW TABLES LIKE 'athletes'")
            table_exists = await cursor.fetchone()
            
            if table_exists:
                print("✅ Athletes table exists")
                
                # Count total athletes
                await cursor.execute("SELECT COUNT(*) as count FROM athletes")
                count_result = await cursor.fetchone()
                print(f"📊 Total athletes in database: {count_result['count']}")
                
                # Show table structure
                await cursor.execute("DESCRIBE athletes")
                columns = await cursor.fetchall()
                print("\n📋 Athletes table structure:")
                for col in columns:
                    print(f"   - {col['Field']}: {col['Type']}")
                
                # Show first 5 athletes with their data
                await cursor.execute("""
                    SELECT 
                        a.id,
                        a.height,
                        a.weight,
                        a.is_disabled,
                        a.state,
                        a.district,
                        u.username
                    FROM athletes a
                    LEFT JOIN users u ON a.user_id = u.id
                    LIMIT 5
                """)
                
                athletes = await cursor.fetchall()
                print(f"\n👥 Sample athletes data:")
                for athlete in athletes:
                    print(f"   ID: {athlete['id']}, Name: {athlete['username']}, Height: {athlete['height']}, Weight: {athlete['weight']}")
                
            else:
                print("❌ Athletes table does not exist!")
                
                # Show all tables
                await cursor.execute("SHOW TABLES")
                tables = await cursor.fetchall()
                print("Available tables:")
                for table in tables:
                    print(f"   - {list(table.values())[0]}")
        
        await connection.ensure_closed()
        
    except Exception as e:
        print(f"❌ Database connection failed: {e}")
        return False
    
    return True

if __name__ == "__main__":
    asyncio.run(test_database_connection())
