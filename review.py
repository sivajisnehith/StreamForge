import os
import requests

api_key = os.environ["GROQ_API_KEY"]

headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/json"
}

payload = {
    "model": "llama-3.3-70b-versatile",
    "messages": [
        {
            "role": "user",
            "content": "Say 'GitHub Actions successfully connected to Groq!'"
        }
    ]
}

response = requests.post(
    "https://api.groq.com/openai/v1/chat/completions",
    headers=headers,
    json=payload
)

print(response.json()["choices"][0]["message"]["content"])