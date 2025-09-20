"""
Pydantic models for athlete data and API responses.
"""
from pydantic import BaseModel, Field, validator
from typing import List, Optional
from uuid import uuid4


class AthleteInput(BaseModel):
    """Input model for athlete data."""
    
    height: float = Field(
        ..., 
        ge=100, 
        le=250, 
        description="Height in centimeters (100-250)"
    )
    weight: float = Field(
        ..., 
        ge=30, 
        le=200, 
        description="Weight in kilograms (30-200)"
    )
    training_duration_minutes: int = Field(
        ..., 
        ge=0, 
        le=600, 
        description="Daily training duration in minutes (0-600)",
        alias="trainingDurationMinutes"
    )
    rank_position: int = Field(
        ..., 
        ge=1, 
        description="Rank position (1 = first place, 2 = second, etc.)",
        alias="rankPosition"
    )
    athlete_name: Optional[str] = Field(
        None, 
        description="Optional athlete name",
        alias="athleteName"
    )
    
    class Config:
        """Pydantic config."""
        allow_population_by_field_name = True
        json_encoders = {
            # Custom encoders if needed
        }


class AthleteSuggestion(BaseModel):
    """Output model for athlete suggestion."""
    
    athlete_id: str = Field(
        default_factory=lambda: str(uuid4()), 
        description="Unique athlete identifier",
        alias="athleteId"
    )
    athlete_name: str = Field(
        ..., 
        description="Athlete name",
        alias="athleteName"
    )
    score: int = Field(
        ..., 
        ge=0, 
        le=100, 
        description="Athlete score out of 100"
    )
    reason: str = Field(
        ..., 
        description="Explanation for the suggestion"
    )
    
    class Config:
        """Pydantic config."""
        allow_population_by_field_name = True
        json_encoders = {
            # Custom encoders if needed
        }


class DatabaseAthlete(BaseModel):
    """Model for athlete data from database."""
    
    athlete_id: str = Field(..., alias="athleteId")
    athlete_name: str = Field(..., alias="athleteName") 
    height: Optional[float] = None
    weight: Optional[float] = None
    training_duration_minutes: Optional[int] = Field(None, alias="trainingDurationMinutes")
    rank_position: Optional[int] = Field(None, alias="rankPosition")
    score: Optional[int] = None
    reason: Optional[str] = None
    
    class Config:
        """Pydantic config."""
        allow_population_by_field_name = True


class SuggestionResponse(BaseModel):
    """Response model for athlete suggestions."""
    
    suggestions: List[AthleteSuggestion] = Field(
        ...,
        description="List of suggested athletes"
    )
    total_count: int = Field(
        ...,
        description="Total number of suggestions",
        alias="totalCount"
    )
    processing_time_ms: Optional[float] = Field(
        None,
        description="Processing time in milliseconds",
        alias="processingTimeMs"
    )
    
    class Config:
        """Pydantic config."""
        allow_population_by_field_name = True


class WorkflowState(BaseModel):
    """State model for LangGraph workflow."""
    
    athlete_input: AthleteInput
    fitness_score: float = 0.0
    training_score: float = 0.0
    rank_score: float = 0.0
    total_score: float = 0.0
    reason: str = ""
    athlete_id: str = ""
    athlete_name: str = ""
    
    class Config:
        """Pydantic config."""
        arbitrary_types_allowed = True
