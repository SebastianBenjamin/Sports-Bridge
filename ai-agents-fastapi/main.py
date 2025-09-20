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
    # Sample athlete inputs for the workflow
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
    
    # Process through the workflow service
    suggestions = await AthleteService.suggest_athletes_list(sample_athletes)
    
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



if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG,
    )
