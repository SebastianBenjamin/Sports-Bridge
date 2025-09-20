"""
Simplified service layer for athlete sponsorship suggestions.
"""
from typing import List
import time
from app.models.athlete import AthleteInput, AthleteSuggestion, SuggestionResponse
import uuid


class AthleteService:
    """Service class for athlete-related operations."""
    
    @staticmethod
    async def suggest_athletes_list(athlete_inputs: List[AthleteInput]) -> List[AthleteSuggestion]:
        """
        Process athlete inputs and return a list of sponsorship suggestions.
        
        Args:
            athlete_inputs: List of athlete data to process
            
        Returns:
            List of AthleteSuggestion objects
        """
        suggestions = []
        
        for athlete_input in athlete_inputs:
            # Calculate fitness score
            fitness_score = AthleteService._calculate_fitness_score(athlete_input)
            
            # Calculate training score  
            training_score = AthleteService._calculate_training_score(athlete_input)
            
            # Calculate rank score
            rank_score = AthleteService._calculate_rank_score(athlete_input)
            
            # Calculate total score (weighted average)
            total_score = int(fitness_score * 0.3 + training_score * 0.3 + rank_score * 0.4)
            
            # Generate reason
            reason = AthleteService._generate_reason(fitness_score, training_score, rank_score, athlete_input)
            
            # Create suggestion
            suggestion = AthleteSuggestion(
                athleteId=str(uuid.uuid4())[:8].upper(),  # Generate simple ID
                athleteName=athlete_input.athlete_name,
                score=total_score,
                reason=reason
            )
            suggestions.append(suggestion)
        
        return suggestions
    
    @staticmethod
    def _calculate_fitness_score(athlete: AthleteInput) -> float:
        """Calculate fitness score based on height and weight."""
        # Check if height is within range (150-220 cm)
        height_in_range = 150 <= athlete.height <= 220
        
        # Check if weight is within range (40-150 kg)  
        weight_in_range = 40 <= athlete.weight <= 150
        
        # Calculate BMI
        height_m = athlete.height / 100
        bmi = athlete.weight / (height_m ** 2)
        
        # BMI scoring
        if 18.5 <= bmi <= 24.9:
            bmi_score = 100
        elif 17 <= bmi < 18.5 or 25 <= bmi <= 27:
            bmi_score = 80
        elif 16 <= bmi < 17 or 27 < bmi <= 30:
            bmi_score = 60
        else:
            bmi_score = 30
            
        # Combine scores
        if height_in_range and weight_in_range:
            return bmi_score
        else:
            return bmi_score * 0.7  # Penalty for being outside ranges
    
    @staticmethod
    def _calculate_training_score(athlete: AthleteInput) -> float:
        """Calculate training score based on daily training duration."""
        # Score based on training minutes per day
        if athlete.training_duration_minutes >= 120:
            return 100
        elif athlete.training_duration_minutes >= 90:
            return 85
        elif athlete.training_duration_minutes >= 60:
            return 70
        elif athlete.training_duration_minutes >= 30:
            return 50
        else:
            return 25
    
    @staticmethod
    def _calculate_rank_score(athlete: AthleteInput) -> float:
        """Calculate rank score based on competitive ranking."""
        if athlete.rank_position == 1:
            return 100
        elif athlete.rank_position == 2:
            return 85
        elif athlete.rank_position == 3:
            return 70
        elif athlete.rank_position <= 5:
            return 55
        elif athlete.rank_position <= 10:
            return 40
        elif athlete.rank_position <= 20:
            return 25
        else:
            return max(10, 25 - (athlete.rank_position - 20))
    
    @staticmethod
    def _generate_reason(fitness_score: float, training_score: float, rank_score: float, athlete: AthleteInput) -> str:
        """Generate reason text based on scores."""
        reasons = []
        
        # Fitness assessment
        if fitness_score >= 80:
            reasons.append("Fit body measurements")
        else:
            reasons.append("Outside body measurement range")
            
        # Training assessment  
        if training_score >= 70:
            reasons.append("Sufficient training")
        else:
            reasons.append("Insufficient training")
            
        # Rank assessment
        if athlete.rank_position == 1:
            reasons.append("Top rank athlete")
        elif athlete.rank_position == 2:
            reasons.append("Second rank athlete")
        elif athlete.rank_position == 3:
            reasons.append("Third rank athlete")
        elif athlete.rank_position <= 10:
            reasons.append("Top 10 athlete")
        else:
            reasons.append("Lower rank athlete")
            
        return "; ".join(reasons)
