import os
import sys
import requests

api_key = os.environ.get("GROQ_API_KEY")
# GitHub Actions provides the PR diff via env vars if you pass it, or you can read a file
pr_diff = os.environ.get("PR_DIFF", "No diff provided.")

headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/json"
}

system_prompt = (
    "You are an AI code reviewer. Analyze the provided git diff.\n"
    "1. Provide constructive suggestions for improvement.\n"
    "2. If you find any syntax errors, security vulnerabilities, or critical breaking bugs, "
    "start your response with exactly '[CRITICAL_ERROR]' followed by the explanation.\n"
    "Keep your response concise and formatted in Markdown."
)

payload = {
    "model": "llama-3.3-70b-versatile",
    "messages": [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": f"Review this PR diff:\n\n{pr_diff}"}
    ]
}

response = requests.post(
    "https://api.groq.com/openai/v1/chat/completions",
    headers=headers,
    json=payload
)

review_content = response.json()["choices"][0]["message"]["content"]

# Save the review message to a file so the GitHub Action can read it easily
with open("pr_review.md", "w") as f:
    f.write(review_content)

# If the model flagged a critical error, exit with an error code (1)
if "[CRITICAL_ERROR]" in review_content:
    print("Critical errors detected. Flagging workflow to close the PR.")
    sys.exit(1)
else:
    sys.exit(0)
