"""
Configuration settings for the FastAPI application.
"""
import os
from typing import Optional


class Settings:
    """Application settings."""
    
    # Server settings
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))
    DEBUG: bool = os.getenv("DEBUG", "True").lower() == "true"
    
    # Database settings (from existing application.properties)
    DB_HOST: str = os.getenv("DB_HOST", "localhost")
    DB_PORT: int = int(os.getenv("DB_PORT", "3306"))
    DB_NAME: str = os.getenv("DB_NAME", "sportsbridge")
    DB_USER: str = os.getenv("DB_USER", "root")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "Baymaxhiro@156")
    
    @property
    def database_url(self) -> str:
        return f"mysql://sportsbridge:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
    
    # Athlete scoring thresholds
    MIN_HEIGHT: int = 150  # cm
    MAX_HEIGHT: int = 220  # cm
    MIN_WEIGHT: int = 40   # kg
    MAX_WEIGHT: int = 150  # kg
    MIN_TRAINING_MINUTES: int = 60  # minutes per day
    
    # Scoring weights
    FITNESS_SCORE_WEIGHT: float = 0.3
    TRAINING_SCORE_WEIGHT: float = 0.3
    RANK_SCORE_WEIGHT: float = 0.4


settings = Settings()
