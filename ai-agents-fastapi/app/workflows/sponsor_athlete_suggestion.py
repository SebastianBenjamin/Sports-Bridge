"""
Simplified workflow for sponsor athlete suggestion.
"""
from typing import Dict, Any
from app.models.athlete import WorkflowState, AthleteInput
from app.config import settings
import uuid


"""
Simplified workflow for sponsor athlete suggestion.
"""
from typing import Dict, Any
from app.models.athlete import WorkflowState, AthleteInput
from app.config import settings
import uuid


def calculate_fitness_score(state: WorkflowState) -> WorkflowState:
    """
    Calculate fitness score based on height and weight.
    Score ranges from 0-100 based on whether athlete is within optimal range.
    """
    athlete = state.athlete_input
    
    # Check if height is within range (150-220 cm)
    height_in_range = settings.MIN_HEIGHT <= athlete.height <= settings.MAX_HEIGHT
    
    # Check if weight is within range (40-150 kg)
    weight_in_range = settings.MIN_WEIGHT <= athlete.weight <= settings.MAX_WEIGHT
    
    # Calculate BMI for additional fitness assessment
    height_m = athlete.height / 100  # Convert to meters
    bmi = athlete.weight / (height_m ** 2)
    
    # BMI ranges: underweight <18.5, normal 18.5-24.9, overweight 25-29.9, obese >30
    bmi_score = 0
    if 18.5 <= bmi <= 24.9:
        bmi_score = 100  # Optimal BMI
    elif 17 <= bmi < 18.5 or 25 <= bmi <= 27:
        bmi_score = 80   # Good BMI
    elif 16 <= bmi < 17 or 27 < bmi <= 30:
        bmi_score = 60   # Acceptable BMI
    else:
        bmi_score = 30   # Poor BMI
    
    # Combine height, weight, and BMI scores
    fitness_score = 0
    if height_in_range and weight_in_range:
        fitness_score = bmi_score
    elif height_in_range or weight_in_range:
        fitness_score = bmi_score * 0.7
    else:
        fitness_score = bmi_score * 0.4
    
    state.fitness_score = min(100, max(0, fitness_score))
    return state


def calculate_training_score(state: WorkflowState) -> WorkflowState:
    """
    Calculate training score based on daily training duration.
    Score ranges from 0-100 based on training commitment.
    """
    athlete = state.athlete_input
    training_minutes = athlete.training_duration_minutes
    
    # Score calculation based on training duration
    if training_minutes >= settings.MIN_TRAINING_MINUTES:
        # Bonus for exceeding minimum
        excess_minutes = training_minutes - settings.MIN_TRAINING_MINUTES
        training_score = 70 + min(30, excess_minutes / 2)  # Up to 100 for 120+ minutes
    else:
        # Penalty for not meeting minimum
        training_score = (training_minutes / settings.MIN_TRAINING_MINUTES) * 70
    
    state.training_score = min(100, max(0, training_score))
    return state


def calculate_rank_score(state: WorkflowState) -> WorkflowState:
    """
    Calculate rank score based on competition position.
    Score ranges from 0-100 with 1st place getting highest score.
    """
    athlete = state.athlete_input
    rank = athlete.rank_position
    
    # Score calculation based on rank position
    if rank == 1:
        rank_score = 100
    elif rank == 2:
        rank_score = 85
    elif rank == 3:
        rank_score = 70
    elif rank <= 5:
        rank_score = 55
    elif rank <= 10:
        rank_score = 40
    elif rank <= 20:
        rank_score = 25
    else:
        rank_score = max(10, 50 - rank)  # Decreasing score for lower ranks
    
    state.rank_score = min(100, max(0, rank_score))
    return state


def calculate_total_score(state: WorkflowState) -> WorkflowState:
    """
    Calculate total score as weighted average of all component scores.
    """
    total_score = (
        state.fitness_score * settings.FITNESS_SCORE_WEIGHT +
        state.training_score * settings.TRAINING_SCORE_WEIGHT +
        state.rank_score * settings.RANK_SCORE_WEIGHT
    )
    
    state.total_score = round(total_score, 1)
    return state


def generate_reason(state: WorkflowState) -> WorkflowState:
    """
    Generate explanation for why this athlete is suggested for sponsorship.
    """
    athlete = state.athlete_input
    
    reasons = []
    
    # Fitness-related reasons
    if state.fitness_score >= 80:
        reasons.append("excellent physical fitness metrics")
    elif state.fitness_score >= 60:
        reasons.append("good physical condition")
    else:
        reasons.append("needs improvement in physical fitness")
    
    # Training-related reasons
    if state.training_score >= 80:
        reasons.append("exceptional training dedication ({}+ min/day)".format(
            athlete.training_duration_minutes
        ))
    elif state.training_score >= 60:
        reasons.append("solid training commitment")
    else:
        reasons.append("limited training schedule")
    
    # Rank-related reasons
    if state.rank_score >= 90:
        reasons.append("top-tier competitive performance (rank #{})".format(
            athlete.rank_position
        ))
    elif state.rank_score >= 70:
        reasons.append("strong competitive results")
    else:
        reasons.append("developing competitive potential")
    
    # Overall assessment
    if state.total_score >= 80:
        reason = "Highly recommended for sponsorship due to " + ", ".join(reasons)
    elif state.total_score >= 60:
        reason = "Good sponsorship candidate with " + ", ".join(reasons)
    else:
        reason = "Potential sponsorship candidate, though " + ", ".join(reasons)
    
    state.reason = reason
    return state


def set_athlete_metadata(state: WorkflowState) -> WorkflowState:
    """
    Set athlete ID and name for the final output.
    """
    state.athlete_id = str(uuid.uuid4())
    state.athlete_name = (
        state.athlete_input.athlete_name or 
        f"Athlete_{state.athlete_id[:8]}"
    )
    return state


async def process_athlete_workflow(athlete_input: AthleteInput) -> WorkflowState:
    """
    Process athlete through the complete workflow.
    This replaces the LangGraph workflow with a simple sequential process.
    """
    # Create initial state
    state = WorkflowState(athlete_input=athlete_input)
    
    # Execute workflow steps in sequence
    state = calculate_fitness_score(state)
    state = calculate_training_score(state)
    state = calculate_rank_score(state)
    state = calculate_total_score(state)
    state = generate_reason(state)
    state = set_athlete_metadata(state)
    
    return state


# For backward compatibility
sponsor_athlete_workflow = type('WorkflowWrapper', (), {
    'ainvoke': process_athlete_workflow
})()
