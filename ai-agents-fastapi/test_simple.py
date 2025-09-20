#!/usr/bin/env python3

import asyncio
import sys
import os
sys.path.append('/home/zaibonk/Desktop/plan-c/ai-agents-fastapi')

from app.models.athlete import AthleteInput
from app.services.athlete_service import AthleteService
import json

async def test():
    sample_athletes = [
        AthleteInput(
            height=180,
            weight=75,
            trainingDurationMinutes=120,
            rankPosition=1,
            athleteName="John Doe"
        ),
        AthleteInput(
            height=160,
            weight=90,
            trainingDurationMinutes=30,
            rankPosition=2,
            athleteName="Jane Smith"
        )
    ]
    
    suggestions = await AthleteService.suggest_athletes_list(sample_athletes)
    
    # Convert to JSON format you want
    result = []
    for suggestion in suggestions:
        result.append({
            "athleteId": suggestion.athlete_id,
            "athleteName": suggestion.athlete_name,
            "score": suggestion.score,
            "reason": suggestion.reason
        })
    
    print("API Response:")
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    asyncio.run(test())
