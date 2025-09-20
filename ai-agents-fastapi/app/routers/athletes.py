"""
FastAPI router for athlete endpoints.
"""
from fastapi import APIRouter, HTTPException, status, Query
from typing import List, Union
from app.models.athlete import (
    AthleteInput, 
    AthleteSuggestion, 
    SuggestionResponse
)
from app.services.athlete_service import AthleteService
from app.services.athlete_db_service import AthleteDBService

router = APIRouter()


@router.post(
    "/suggest-athletes",
    response_model=List[AthleteSuggestion],
    summary="Get athlete sponsorship suggestions",
    description="Process athlete data and return sponsorship suggestions based on performance metrics"
)
async def suggest_athletes(
    athletes: Union[List[AthleteInput], AthleteInput]
) -> List[AthleteSuggestion]:
    """
    Suggest athletes for sponsorship based on their performance metrics.
    
    This endpoint processes athlete data including height, weight, training duration,
    and rank position to calculate a sponsorship score and provide recommendations.
    
    Args:
        athletes: Single athlete or list of athletes to evaluate
        
    Returns:
        List of AthleteSuggestion objects
        
    Raises:
        HTTPException: If there's an error processing the request
    """
    try:
        # Ensure we have a list of athletes
        if isinstance(athletes, AthleteInput):
            athlete_list = [athletes]
        else:
            athlete_list = athletes
        
        # Validate that we have at least one athlete
        if not athlete_list:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="At least one athlete must be provided"
            )
        
        # Process athletes through the service
        suggestions = await AthleteService.suggest_athletes_list(athlete_list)
        
        return suggestions
        
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error processing athlete suggestions: {str(e)}"
        )


@router.post(
    "/evaluate-athlete",
    response_model=AthleteSuggestion,
    summary="Evaluate a single athlete",
    description="Evaluate a single athlete and return their sponsorship potential"
)
async def evaluate_athlete(athlete: AthleteInput) -> AthleteSuggestion:
    """
    Evaluate a single athlete for sponsorship potential.
    
    Args:
        athlete: Athlete data to evaluate
        
    Returns:
        AthleteSuggestion with score and reasoning
        
    Raises:
        HTTPException: If there's an error processing the request
    """
    try:
        suggestion = await AthleteService.suggest_single_athlete(athlete)
        return suggestion
        
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error evaluating athlete: {str(e)}"
        )


@router.get(
    "/sponsor-recommendation",
    response_model=List[AthleteSuggestion],
    summary="Get sponsor athlete recommendations",
    description="Get a list of recommended athletes for sponsorship using real database data"
)
async def get_sponsor_recommendations(
    sponsor_requirements: str = "fit",
    budget: int = 100000
) -> List[AthleteSuggestion]:
    """
    Get sponsor athlete recommendations using real database data.
    
    Args:
        sponsor_requirements: Requirements for the sponsorship (default: "fit")
        budget: Budget for sponsorship (default: 100000)
        
    Returns:
        List of AthleteSuggestion objects based on real athlete data
    """
    try:
        # Fetch real athletes from database
        db_athletes = await AthleteDBService.get_all_athletes()
        
        if not db_athletes:
            # Fallback to sample data if no database data available
            sample_athletes = [
                AthleteInput(
                    height=180,
                    weight=75,
                    trainingDurationMinutes=120,
                    rankPosition=1,
                    athleteName="Sample Athlete 1"
                ),
                AthleteInput(
                    height=160,
                    weight=90,
                    trainingDurationMinutes=30,
                    rankPosition=2,
                    athleteName="Sample Athlete 2"
                )
            ]
            suggestions = await AthleteService.suggest_athletes_list(sample_athletes)
            return suggestions
        
        # Convert database athletes to AthleteInput format for AI processing
        athlete_inputs = []
        for db_athlete in db_athletes:
            # Safely convert database fields to AthleteInput format
            height = float(db_athlete.get('height', 170)) if db_athlete.get('height') else 170.0
            weight = float(db_athlete.get('weight', 70)) if db_athlete.get('weight') else 70.0
            athlete_name = db_athlete.get('athleteName', f"Athlete_{db_athlete.get('athleteId', 'Unknown')}")
            
            athlete_input = AthleteInput(
                height=height,
                weight=weight,
                trainingDurationMinutes=120,  # Default since not in current DB schema
                rankPosition=1,  # Default since not in current DB schema  
                athleteName=athlete_name
            )
            athlete_inputs.append(athlete_input)
        
        # Process athletes through the AI service
        suggestions = await AthleteService.suggest_athletes_list(athlete_inputs)
        
        return suggestions
        
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error getting sponsor recommendations: {str(e)}"
        )


@router.get(
    "/sponsor-suggestion",
    summary="Get sponsor athlete suggestions from database",
    description="Get AI-powered athlete suggestions for sponsors using real database data"
)
async def get_sponsor_suggestion():
    """
    Get sponsor athlete suggestions using real database data.
    
    Returns athlete suggestions in the exact format requested:
    [
      {
        "athleteId": "A1",
        "athleteName": "John Doe",
        "score": 100,
        "reason": "Fit body measurements; Sufficient training; Top rank athlete"
      }
    ]
    
    Returns:
        List of athlete suggestions with scores and reasons
    """
    try:
        # Fetch real athletes from database
        db_athletes = await AthleteDBService.get_all_athletes()
        
        if not db_athletes:
            return {
                "message": "No athletes found in database",
                "suggestions": []
            }
        
        # Convert database athletes to AthleteInput format for AI processing
        athlete_inputs = []
        for db_athlete in db_athletes:
            # Safely convert database fields to AthleteInput format
            height = float(db_athlete.get('height', 170)) if db_athlete.get('height') else 170.0
            weight = float(db_athlete.get('weight', 70)) if db_athlete.get('weight') else 70.0
            athlete_name = db_athlete.get('athleteName', f"Athlete_{db_athlete.get('athleteId', 'Unknown')}")
            
            athlete_input = AthleteInput(
                height=height,
                weight=weight,
                trainingDurationMinutes=120,  # Default since not in current DB schema
                rankPosition=1,  # Default since not in current DB schema  
                athleteName=athlete_name
            )
            athlete_inputs.append(athlete_input)
        
        # Get AI suggestions using real data
        suggestions = await AthleteService.suggest_athletes_list(athlete_inputs)
        
        # Format response in the exact format requested
        result = []
        for suggestion in suggestions:
            result.append({
                "athleteId": suggestion.athlete_id,
                "athleteName": suggestion.athlete_name,
                "score": suggestion.score,
                "reason": suggestion.reason
            })
        
        return result
        
    except Exception as e:
        # Return error in JSON format as requested
        return {
            "error": f"Failed to get sponsor suggestions: {str(e)}",
            "suggestions": []
        }


@router.get(
    "/scoring-info",
    summary="Get scoring methodology information",
    description="Get information about how athlete scores are calculated"
)
async def get_scoring_info():
    """
    Get information about the scoring methodology.
    
    Returns:
        Dictionary with scoring criteria and weights
    """
    return {
        "scoring_methodology": {
            "fitness_score": {
                "weight": 0.3,
                "description": "Based on height, weight, and BMI within optimal ranges",
                "criteria": {
                    "height_range": "150-220 cm",
                    "weight_range": "40-150 kg",
                    "optimal_bmi": "18.5-24.9"
                }
            },
            "training_score": {
                "weight": 0.3,
                "description": "Based on daily training duration commitment",
                "criteria": {
                    "minimum_training": "60 minutes/day",
                    "bonus_for_excess": "Additional points for training over 60 min"
                }
            },
            "rank_score": {
                "weight": 0.4,
                "description": "Based on competitive performance ranking",
                "criteria": {
                    "rank_1": "100 points",
                    "rank_2": "85 points",
                    "rank_3": "70 points",
                    "rank_4_5": "55 points",
                    "rank_6_10": "40 points",
                    "rank_11_20": "25 points",
                    "rank_20_plus": "Decreasing points"
                }
            }
        },
        "total_score_range": "0-100",
        "recommendation_thresholds": {
            "highly_recommended": "80+",
            "good_candidate": "60-79",
            "potential_candidate": "Below 60"
        }
    }


@router.get(
    "/athletes",
    summary="Get all athletes from database",
    description="Fetch all athlete data from MySQL database with calculated scores and reasons"
)
async def get_all_athletes():
    """
    Fetch all athletes from the MySQL database.
    
    Returns athlete data directly from the database including:
    - athleteId: Unique identifier
    - athleteName: Athlete name  
    - score: Calculated sponsorship score (0-100)
    - reason: Explanation for the score
    
    Returns:
        List of athletes with scores and reasons
        
    Raises:
        HTTPException: If there's a database connection error
    """
    try:
        athletes = await AthleteDBService.get_all_athletes()
        return athletes
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch athletes from database: {str(e)}"
        )


@router.get(
    "/athletes/top",
    summary="Get top performing athletes",
    description="Fetch athletes with score above threshold from MySQL database"
)
async def get_top_athletes(
    min_score: int = Query(
        default=60,
        ge=0,
        le=100,
        description="Minimum score threshold for athletes"
    )
):
    """
    Fetch top performing athletes from the database.
    
    Args:
        min_score: Minimum score threshold (default: 60)
    
    Returns:
        List of athletes meeting the score criteria
        
    Raises:
        HTTPException: If there's a database connection error
    """
    try:
        athletes = await AthleteDBService.get_athletes_by_score_threshold(min_score)
        return {
            "criteria": f"Athletes with score >= {min_score}",
            "count": len(athletes),
            "athletes": athletes
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch top athletes: {str(e)}"
        )
