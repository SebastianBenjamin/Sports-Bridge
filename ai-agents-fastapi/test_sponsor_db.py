#!/usr/bin/env python3
"""
Test database integration for sponsor suggestions.
"""
import asyncio
import sys
import os
sys.path.append('/home/zaibonk/Desktop/plan-c/plan-c/Sports-Bridge/ai-agents-fastapi')

from app.services.athlete_db_service import AthleteDBService
from app.models.athlete import AthleteInput
from app.services.athlete_service import AthleteService
import json

async def test_sponsor_suggestion_with_db():
    print("Testing sponsor suggestion with database data...")
    try:
        # Fetch real athletes from database
        db_athletes = await AthleteDBService.get_all_athletes()
        print(f"Found {len(db_athletes)} athletes in database")
        
        if not db_athletes:
            print("No athletes found in database")
            return []
        
        # Convert first few database athletes to AthleteInput format for AI processing
        athlete_inputs = []
        for db_athlete in db_athletes[:3]:  # Process first 3 athletes
            athlete_input = AthleteInput(
                height=float(db_athlete.get('height', 170)) if db_athlete.get('height') else 170.0,
                weight=float(db_athlete.get('weight', 70)) if db_athlete.get('weight') else 70.0,
                trainingDurationMinutes=120,  # Default since not in DB schema
                rankPosition=1,  # Default since not in DB schema
                athleteName=db_athlete.get('athleteName', f"Athlete_{db_athlete.get('athleteId')}")
            )
            athlete_inputs.append(athlete_input)
            print(f"Converted athlete: {athlete_input.athleteName}")
        
        # Get AI suggestions using real data
        suggestions = await AthleteService.suggest_athletes_list(athlete_inputs)
        
        # Format response like sponsor suggestion endpoint
        result = []
        for suggestion in suggestions:
            result.append({
                "athleteId": suggestion.athlete_id,
                "athleteName": suggestion.athlete_name,
                "score": suggestion.score,
                "reason": suggestion.reason
            })
        
        print("\n🎯 Sponsor Suggestions with Real Database Data:")
        print(json.dumps(result, indent=2))
        return result
        
    except Exception as e:
        print(f"Error: {e}")
        return []

if __name__ == "__main__":
    asyncio.run(test_sponsor_suggestion_with_db())
