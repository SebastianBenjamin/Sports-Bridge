import os

LANGGRAPH_KEY = os.getenv("LANGGRAPH_KEY")

# Placeholder helper for any future LLM/graph calls.
# Keep this safe and minimal; do not call external services without explicit keys.

def ensure_key_present() -> bool:
    return bool(LANGGRAPH_KEY)

