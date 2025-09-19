# AI Microservice Integration

This doc shows how to smoke test the AI service, invitations, chat, and sponsor recommendations.

## Prereqs
- Postgres locally or via docker-compose
- Backend running (Spring Boot)
- AI service running

## Run with docker-compose
```bash
# Windows PowerShell
cd C:\Sports-Bridge
docker compose up -d --build
```

Services:
- ai: http://localhost:8001
- db: postgres on localhost:5432 (postgres/admin, db sportsbridge)

## Smoke: AI workflow
```bash
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
```bash
curl -X POST http://localhost:8001/run-workflow ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"sponsor_recommendation\",\"params\":{}}"
```
2) Direct synchronous endpoint
```bash
curl http://localhost:8001/sponsor-recommendations
```

## Smoke: Invitations + Chat (Backend)
These endpoints will be available after backend integration steps.
```bash
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
