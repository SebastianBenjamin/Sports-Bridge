"""
FastAPI application with LangGraph workflow for athlete sponsorship suggestions.
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Dict
from app.routers import athletes
from app.config import settings
from app.models.athlete import AthleteInput
from app.services.athlete_service import AthleteService
from app.services.athlete_db_service import AthleteDBService
from app.database import db
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Create FastAPI instance
app = FastAPI(
    title="Athlete Sponsorship Suggestion API",
    description="API for suggesting athletes for sponsorship based on performance metrics",
    version="1.0.0",
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def startup_event():
    """Initialize database connection on startup."""
    try:
        await db.create_pool()
        logger.info("Application started successfully")
    except Exception as e:
        logger.error(f"Failed to start application: {e}")


@app.on_event("shutdown") 
async def shutdown_event():
    """Close database connection on shutdown."""
    try:
        await db.close_pool()
        logger.info("Application shutdown successfully")
    except Exception as e:
        logger.error(f"Error during shutdown: {e}")


# Include routers
app.include_router(athletes.router, prefix="/api/v1", tags=["athletes"])

@app.get("/")
async def root():
    """Root endpoint for health check."""
    return {"message": "Athlete Sponsorship Suggestion API is running"}

@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy", "version": "1.0.0"}

@app.get("/suggest-athletes")
async def suggest_athletes():
    """Endpoint returning athlete data from workflow."""
    try:
        # Fetch real athletes from database
        db_athletes = await AthleteDBService.get_all_athletes()

        if not db_athletes:
            return {"message": "No athletes found in database", "suggestions": []}

        # Convert database athletes to AthleteInput format for AI processing
        athlete_inputs = []
        for db_athlete in db_athletes:
            # Safely convert database fields to AthleteInput format for AI processing
            height = float(db_athlete.get('height', 170)) if db_athlete.get('height') else 170.0
            weight = float(db_athlete.get('weight', 70)) if db_athlete.get('weight') else 70.0
            athlete_name = db_athlete.get('athleteName', f"Athlete_{db_athlete.get('athleteId', 'Unknown')}")

            # Prefer DB values if present, otherwise use safe defaults
            training_minutes_val = db_athlete.get('trainingDurationMinutes')
            try:
                training_minutes = int(training_minutes_val) if training_minutes_val is not None else 120
            except (TypeError, ValueError):
                training_minutes = 120

            rank_position_val = db_athlete.get('rankPosition')
            try:
                rank_position = int(rank_position_val) if rank_position_val is not None else 1
            except (TypeError, ValueError):
                rank_position = 1

            athlete_input = AthleteInput(
                height=height,
                weight=weight,
                trainingDurationMinutes=training_minutes,
                rankPosition=rank_position,
                athleteName=athlete_name
            )
            athlete_inputs.append(athlete_input)

        # Process through the AI workflow service
        suggestions = await AthleteService.suggest_athletes_list(athlete_inputs)
        
        # Convert to the JSON format you want
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
        logger.error(f"Error in suggest-athletes endpoint: {e}")
        return {"error": f"Failed to get suggestions: {str(e)}", "suggestions": []}



if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG,
    )
