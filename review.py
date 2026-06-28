import os
import requests

api_key = os.getenv("GROQ_API_KEY")

headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/json"
}

payload = {
    "model": "llama-3.3-70b-versatile",
    "messages": [
        {
            "role": "user",
            "content": "Say Hello from Groq."
        }
    ]
}

response = requests.post(
    "https://api.groq.com/openai/v1/chat/completions",
    headers=headers,
    json=payload
)

print(response.text)