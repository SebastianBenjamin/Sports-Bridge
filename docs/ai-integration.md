# AI Microservice Integration

This doc shows how to smoke test the AI service, invitations, chat, and sponsor recommendations.

## Prereqs
- Postgres locally or via docker-compose
- Backend running (Spring Boot)
- AI service running

## Run with docker-compose
```powershell
cd C:\Sports-Bridge
# Start only db first if you need to run migrations before other services
docker compose up -d db

# Apply migrations (create ai_results, invitations, chat tables)
# Option A: using docker compose and psql inside the postgres container
$env:PGPASSWORD = "admin"
docker compose exec -T db psql -U postgres -d sportsbridge -f /migrations.sql

# If you can't mount the file, run the migration from host:
# powershell
psql "host=localhost port=5432 user=postgres password=admin dbname=sportsbridge" -f .\migrations\V1__ai_results_and_chat.sql

# Start AI service
docker compose up -d --build ai
```

Note: If you prefer to bring everything up at once, you can run:
```powershell
docker compose up -d --build
```

Services:
- ai: http://localhost:8001
- db: postgres on localhost:5432 (postgres/admin, db sportsbridge)

## Smoke: AI workflow
```powershell
# Run a workflow
curl -X POST http://localhost:8001/run-workflow ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"athlete_summary\",\"params\":{\"athlete_id\":2}}"

# Fetch result (replace ID returned above)
curl http://localhost:8001/ai-results/1
```

## Smoke: Sponsor recommendation
Two options:
1) Background-run via run-workflow
```powershell
curl -X POST http://localhost:8001/run-workflow ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"sponsor_recommendation\",\"params\":{}}"
```
2) Direct synchronous endpoint
```powershell
curl http://localhost:8001/sponsor-recommendations
```

## Smoke: Invitations + Chat (Backend)
These endpoints will be available after backend integration steps.
```powershell
# create invitation (coach invites player)
curl -X POST http://localhost:8080/api/invitations ^
  -H "Authorization: Bearer <token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"playerId\":2}"

# respond accept
curl -X POST http://localhost:8080/api/invitations/1/respond ^
  -H "Authorization: Bearer <token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"action\":\"accept\"}"

# chat messages
curl http://localhost:8080/api/chat/rooms/1/messages
```
