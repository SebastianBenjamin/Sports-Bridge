# Athlete Sponsorship Suggestion API

A FastAPI application with LangGraph workflow for suggesting athletes for sponsorship based on performance metrics.

## Features

- **FastAPI Backend**: Modern, fast web framework for building APIs
- **LangGraph Workflows**: State-based workflow for processing athlete data
- **Pydantic Models**: Data validation and serialization
- **Scoring System**: Multi-factor scoring based on fitness, training, and performance
- **REST API**: Clean endpoints for athlete evaluation

## API Endpoints

### `POST /api/v1/suggest-athletes`
Process athlete data and return sponsorship suggestions.

**Request Body:**
```json
{
  "height": 175,
  "weight": 70,
  "trainingDurationMinutes": 90,
  "rankPosition": 2,
  "athleteName": "John Doe"
}
```

**Response:**
```json
{
  "suggestions": [
    {
      "athleteId": "uuid-string",
      "athleteName": "John Doe",
      "score": 85.5,
      "reason": "Highly recommended for sponsorship due to excellent physical fitness metrics, exceptional training dedication (90+ min/day), strong competitive results"
    }
  ],
  "totalCount": 1,
  "processingTimeMs": 15.2
}
```

### `POST /api/v1/evaluate-athlete`
Evaluate a single athlete for sponsorship potential.

### `GET /api/v1/scoring-info`
Get information about the scoring methodology.

## Scoring System

The system evaluates athletes based on three main factors:

1. **Fitness Score (30% weight)**
   - Height and weight within optimal ranges
   - BMI calculation for health assessment
   - Range: 150-220 cm height, 40-150 kg weight

2. **Training Score (30% weight)**
   - Daily training duration commitment
   - Minimum threshold: 60 minutes/day
   - Bonus points for exceeding minimum

3. **Rank Score (40% weight)**
   - Competitive performance ranking
   - 1st place: 100 points
   - 2nd place: 85 points
   - 3rd place: 70 points
   - Decreasing scores for lower ranks

## Setup and Installation

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Run the application:
```bash
python main.py
```

3. Access the API documentation:
   - Swagger UI: http://localhost:8000/docs
   - ReDoc: http://localhost:8000/redoc

## Configuration

Configure the application through environment variables or modify `app/config.py`:

- `HOST`: Server host (default: 0.0.0.0)
- `PORT`: Server port (default: 8000)
- `DEBUG`: Debug mode (default: True)

## Docker Support

Run with Docker:

```bash
docker build -t athlete-api .
docker run -p 8000:8000 athlete-api
```

## Project Structure

```
ai-agents-fastapi/
├── main.py                 # FastAPI application entry point
├── requirements.txt        # Python dependencies
├── README.md              # Project documentation
├── Dockerfile             # Docker configuration
├── docker-compose.yml     # Docker Compose configuration
└── app/
    ├── __init__.py
    ├── config.py          # Application configuration
    ├── models/
    │   ├── __init__.py
    │   └── athlete.py     # Pydantic models
    ├── routers/
    │   ├── __init__.py
    │   └── athletes.py    # API endpoints
    ├── services/
    │   ├── __init__.py
    │   └── athlete_service.py  # Business logic
    └── workflows/
        ├── __init__.py
        └── sponsor_athlete_suggestion.py  # LangGraph workflow
```

## Testing

Test the API endpoints:

```bash
# Test single athlete
curl -X POST "http://localhost:8000/api/v1/evaluate-athlete" \
     -H "Content-Type: application/json" \
     -d '{
       "height": 175,
       "weight": 70,
       "trainingDurationMinutes": 90,
       "rankPosition": 2,
       "athleteName": "Test Athlete"
     }'

# Test multiple athletes
curl -X POST "http://localhost:8000/api/v1/suggest-athletes" \
     -H "Content-Type: application/json" \
     -d '[
       {
         "height": 175,
         "weight": 70,
         "trainingDurationMinutes": 90,
         "rankPosition": 2,
         "athleteName": "Athlete 1"
       },
       {
         "height": 180,
         "weight": 75,
         "trainingDurationMinutes": 120,
         "rankPosition": 1,
         "athleteName": "Athlete 2"
       }
     ]'
```
