#!/usr/bin/env python3
"""
Test the sponsor suggestion endpoint with real database data.
"""
import asyncio
import sys
import os
sys.path.append('/home/zaibonk/Desktop/plan-c/plan-c/Sports-Bridge/ai-agents-fastapi')

async def test_sponsor_suggestion():
    """Test the sponsor suggestion endpoint logic."""
    try:
        # Import after adding path
        from app.services.athlete_db_service import AthleteDBService
        from app.models.athlete import AthleteInput
        from app.services.athlete_service import AthleteService
        
        print("🔍 Testing sponsor suggestion with real database data...")
        
        # Step 1: Fetch real athletes from database
        print("\n1. Fetching athletes from database...")
        db_athletes = await AthleteDBService.get_all_athletes()
        
        if not db_athletes:
            print("❌ No athletes found in database")
            return
        
        print(f"✅ Found {len(db_athletes)} athletes in database")
        for i, athlete in enumerate(db_athletes[:3]):  # Show first 3
            print(f"   - Athlete {i+1}: {athlete}")
        
        # Step 2: Convert to AthleteInput format
        print("\n2. Converting to AI input format...")
        athlete_inputs = []
        for db_athlete in db_athletes:
            height = float(db_athlete.get('height', 170)) if db_athlete.get('height') else 170.0
            weight = float(db_athlete.get('weight', 70)) if db_athlete.get('weight') else 70.0
            athlete_name = db_athlete.get('athleteName', f"Athlete_{db_athlete.get('athleteId', 'Unknown')}")
            
            athlete_input = AthleteInput(
                height=height,
                weight=weight,
                trainingDurationMinutes=120,
                rankPosition=1,
                athleteName=athlete_name
            )
            athlete_inputs.append(athlete_input)
            print(f"   - Converted: {athlete_name} ({height}cm, {weight}kg)")
        
        # Step 3: Get AI suggestions
        print("\n3. Getting AI suggestions...")
        suggestions = await AthleteService.suggest_athletes_list(athlete_inputs)
        
        # Step 4: Format response
        print("\n4. Formatting response...")
        result = []
        for suggestion in suggestions:
            result.append({
                "athleteId": suggestion.athlete_id,
                "athleteName": suggestion.athlete_name,
                "score": suggestion.score,
                "reason": suggestion.reason
            })
        
        print(f"\n✅ Generated {len(result)} sponsor suggestions:")
        import json
        print(json.dumps(result, indent=2))
        
        return result
        
    except Exception as e:
        print(f"❌ Error during test: {e}")
        import traceback
        traceback.print_exc()
        return []

if __name__ == "__main__":
    asyncio.run(test_sponsor_suggestion())
