"""
Database service for athlete operations.
"""
import logging
from typing import List, Dict, Any, Optional
from app.database import get_database
from app.models.athlete import DatabaseAthlete, AthleteSuggestion
from app.config import settings

logger = logging.getLogger(__name__)


class AthleteDBService:
    """Service for database athlete operations."""
    
    @staticmethod
    async def get_all_athletes() -> List[Dict[str, Any]]:
        """
        Fetch all athletes from the database.
        
        Returns:
            List of athletes with their data
        """
        try:
            db = await get_database()
            
            # Query to get real athlete data with user names
            query = """
            SELECT 
                a.id as athleteId,
                CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')) as athleteName,
                a.height,
                a.weight
            FROM athletes a
            LEFT JOIN users u ON a.user_id = u.id
            ORDER BY a.id ASC
            """
            
            athletes_data = await db.execute_query(query)
            
            if not athletes_data:
                logger.warning("No athletes found in database")
                return []
            
            # Return raw data - scoring will be done by AI service
            results = []
            for athlete_data in athletes_data:
                result = {
                    "athleteId": str(athlete_data.get('athleteId', f"A{len(results) + 1}")),
                    "athleteName": athlete_data.get('athleteName', '').strip() or f"Athlete_{athlete_data.get('athleteId', len(results) + 1)}",
                    "height": float(athlete_data.get('height', 0) or 0),
                    "weight": float(athlete_data.get('weight', 0) or 0),
                }
                results.append(result)
            
            return results
            
        except Exception as e:
            logger.error(f"Error fetching athletes from database: {e}")
            # Return empty list on error as requested
            return []
    
    @staticmethod
    async def get_athletes_by_score_threshold(min_score: int = 60) -> List[Dict[str, Any]]:
        """
        Fetch athletes with score above threshold.
        
        Args:
            min_score: Minimum score threshold
            
        Returns:
            List of athletes meeting the criteria
        """
        try:
            all_athletes = await AthleteDBService.get_all_athletes()
            return [athlete for athlete in all_athletes if athlete['score'] >= min_score]
        except Exception as e:
            logger.error(f"Error filtering athletes by score: {e}")
            return []
    
    @staticmethod
    def _calculate_athlete_score(athlete_data: Dict[str, Any]) -> tuple[int, str]:
        """
        Calculate athlete score and reason based on metrics.
        
        Args:
            athlete_data: Raw athlete data from database
            
        Returns:
            Tuple of (score, reason)
        """
        try:
            height = athlete_data.get('height', 0) or 0
            weight = athlete_data.get('weight', 0) or 0
            is_disabled = athlete_data.get('isDisabled', False)
            state = athlete_data.get('state', '')
            
            # Fitness score (height and weight)
            fitness_score = 0
            fitness_reasons = []
            
            if height > 0:
                if settings.MIN_HEIGHT <= height <= settings.MAX_HEIGHT:
                    fitness_score += 50
                    fitness_reasons.append("Good height")
                else:
                    fitness_reasons.append("Height outside optimal range")
            else:
                fitness_reasons.append("Height not recorded")
            
            if weight > 0:
                if settings.MIN_WEIGHT <= weight <= settings.MAX_WEIGHT:
                    fitness_score += 50
                    fitness_reasons.append("Good weight")
                else:
                    fitness_reasons.append("Weight outside optimal range")
            else:
                fitness_reasons.append("Weight not recorded")
            
            # Base score for active athletes
            base_score = 60
            base_reasons = ["Active registered athlete"]
            
            # Bonus for complete profile
            profile_score = 0
            if height > 0 and weight > 0:
                profile_score = 20
                base_reasons.append("Complete physical profile")
            
            # Disability consideration (positive inclusion)
            disability_score = 0
            if is_disabled:
                disability_score = 10
                base_reasons.append("Paralympic category athlete")
            
            # Geographic bonus (you can customize this)
            geo_score = 0
            if state:
                geo_score = 5
                base_reasons.append(f"Registered from {state}")
            
            # Calculate total score
            total_score = min(100, base_score + (fitness_score * 0.3) + profile_score + disability_score + geo_score)
            
            # Generate reason
            all_reasons = base_reasons + fitness_reasons
            reason = "; ".join(all_reasons)
            
            return int(total_score), reason
            
        except Exception as e:
            logger.error(f"Error calculating athlete score: {e}")
            return 50, "Profile available with basic information"
