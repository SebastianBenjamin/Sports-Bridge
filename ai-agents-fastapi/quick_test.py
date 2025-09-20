#!/usr/bin/env python3

"""
Quick test to verify the API response format
"""

import asyncio
import sys
import os

# Add the app directory to Python path
sys.path.append('/home/zaibonk/Desktop/plan-c/ai-agents-fastapi')

from app.models.athlete import AthleteInput
from app.services.athlete_service import AthleteService
import json

async def test_api_format():
    """Test the API format without running the server"""
    
    # Sample data that matches what the API endpoint uses
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
    
    # Process through the service
    suggestions = await AthleteService.suggest_athletes_list(sample_athletes)
    
    # Convert to the expected JSON format
    result = []
    for suggestion in suggestions:
        result.append({
            "athleteId": suggestion.athlete_id,
            "athleteName": suggestion.athlete_name,
            "score": suggestion.score,
            "reason": suggestion.reason
        })
    
    print("API Response Format:")
    print(json.dumps(result, indent=2))

if __name__ == "__main__":
    asyncio.run(test_api_format())
