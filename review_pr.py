import os
import sys
import requests

api_key = os.environ.get("GROQ_API_KEY")
if not api_key:
    print("Error: GROQ_API_KEY environment variable is missing!")
    sys.exit(1)

# Read the diff from the file instead of an env var (avoids multi-line terminal string bugs)
pr_diff = "No diff provided."
if os.path.exists("diff.txt"):
    with open("diff.txt", "r") as f:
        pr_diff = f.read().strip()

if not pr_diff or pr_diff == "No diff provided.":
    print("Warning: Diff file is empty. Creating a fallback message.")
    with open("pr_review.md", "w") as f:
        f.write("### AI Code Review\nNo code changes were detected in this diff to analyze.")
    sys.exit(0)

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

try:
    print("Sending request to Groq API...")
    response = requests.post(
        "https://api.groq.com/openai/v1/chat/completions",
        headers=headers,
        json=payload,
        timeout=30
    )
    response.raise_for_status()
    review_content = response.json()["choices"][0]["message"]["content"]
except Exception as e:
    print(f"API Error occurred: {e}")
    # Write the error to the file so it displays inside GitHub instead of completely wiping out
    with open("pr_review.md", "w") as f:
        f.write(f"### AI Review Workflow Error\nFailed to get a response from Groq: `{str(e)}`")
    sys.exit(1)

# Save the review message to a file
with open("pr_review.md", "w") as f:
    f.write(review_content)

print("Review compiled successfully.")

# If the model flagged a critical error, exit with 1
if "[CRITICAL_ERROR]" in review_content:
    print("Critical errors detected by AI.")
    sys.exit(1)
else:
    sys.exit(0)
