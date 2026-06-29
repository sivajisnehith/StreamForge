import os
import sys
import json
import subprocess
import requests
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# Configuration
RECIPIENT_EMAIL = "sivajisnehithchandolu@gmail.com"

def get_pr_details():
    github_repository = os.environ.get("GITHUB_REPOSITORY")
    github_token = os.environ.get("GITHUB_TOKEN")
    event_path = os.environ.get("GITHUB_EVENT_PATH")
    
    pr_number = None
    if event_path and os.path.exists(event_path):
        try:
            with open(event_path, "r") as f:
                event = json.load(f)
            if "pull_request" in event:
                pr_number = event["pull_request"]["number"]
        except Exception as e:
            print(f"Error reading GitHub event file: {e}")
            
    return github_repository, pr_number, github_token

def run_compilation():
    print("Running Maven compilation check...")
    cmd = ["mvn", "clean", "compile"]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, check=False)
        return result.returncode == 0, result.stdout, result.stderr
    except FileNotFoundError:
        wrapper_cmd = ["./mvnw", "clean", "compile"] if os.name != 'nt' else ["mvnw.cmd", "clean", "compile"]
        print(f"'mvn' command not found. Trying wrapper: {wrapper_cmd}")
        try:
            result = subprocess.run(wrapper_cmd, capture_output=True, text=True, check=False)
            return result.returncode == 0, result.stdout, result.stderr
        except Exception as e:
            error_msg = f"Failed to execute build command: {e}"
            print(error_msg)
            return False, "", error_msg
    except Exception as e:
        error_msg = f"Unexpected error during build command execution: {e}"
        print(error_msg)
        return False, "", error_msg

def extract_errors(stdout, stderr):
    output = stdout + "\n" + stderr
    lines = output.splitlines()
    error_lines = []
    
    for line in lines:
        if "[ERROR]" in line:
            error_lines.append(line)
            
    if error_lines:
        return "\n".join(error_lines)
    else:
        return "\n".join(lines[-40:])

def run_groq_review(diff_content, api_key):
    if not api_key:
        print("GROQ_API_KEY is not set. Skipping AI review.")
        return "GROQ_API_KEY environment variable is missing. Skipping AI review."
    
    if not diff_content or diff_content.strip() == "":
        print("PR diff content is empty. Skipping AI review.")
        return "No code changes detected in diff."
        
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    
    if len(diff_content) > 40000:
        diff_content = diff_content[:40000] + "\n... (diff truncated for size limits) ..."
        
    payload = {
        "model": "llama-3.3-70b-versatile",
        "messages": [
            {
                "role": "system",
                "content": (
                    "You are an AI code reviewer. Analyze the provided git diff.\n"
                    "Provide constructive suggestions for improvement, bugs, code style issues, or security flaws. "
                    "Keep your response concise and formatted in Markdown."
                )
            },
            {
                "role": "user",
                "content": f"Review this PR diff:\n\n{diff_content}"
            }
        ]
    }
    
    try:
        response = requests.post(
            "https://api.groq.com/openai/v1/chat/completions",
            headers=headers,
            json=payload,
            timeout=30
        )
        response.raise_for_status()
        return response.json()["choices"][0]["message"]["content"]
    except Exception as e:
        print(f"API Error occurred: {e}")
        return f"AI Code Review was skipped because an error occurred: {e}"

def send_email(subject, body, to_email):
    smtp_server = os.environ.get("SMTP_SERVER", "smtp.gmail.com")
    smtp_port_str = os.environ.get("SMTP_PORT", "587")
    try:
        smtp_port = int(smtp_port_str)
    except ValueError:
        smtp_port = 587
        
    sender_email = os.environ.get("SENDER_EMAIL")
    sender_password = os.environ.get("SENDER_PASSWORD")
    
    if not sender_email or not sender_password:
        print("SENDER_EMAIL or SENDER_PASSWORD environment variables are not set. Cannot send email.")
        return False
        
    msg = MIMEMultipart()
    msg['From'] = sender_email
    msg['To'] = to_email
    msg['Subject'] = subject
    msg.attach(MIMEText(body, 'plain'))
    
    try:
        server = smtplib.SMTP(smtp_server, smtp_port)
        server.starttls()
        server.login(sender_email, sender_password)
        server.sendmail(sender_email, to_email, msg.as_string())
        server.quit()
        print(f"Email sent successfully to {to_email}")
        return True
    except Exception as e:
        print(f"Failed to send email to {to_email}: {e}")
        return False

def main():
    repo, pr_num, github_token = get_pr_details()
    
    # Run compilation checks
    comp_success, stdout, stderr = run_compilation()
    
    if not comp_success:
        errors = extract_errors(stdout, stderr)
        print("Compilation check failed. Processing error workflow...")
        
        email_subject = f"PR #{pr_num} Compilation Failed in {repo}" if pr_num else "Repository Compilation Failed"
        email_body = (
            f"Hello,\n\n"
            f"The compilation for the repository code failed.\n"
            f"Details:\n"
            f"PR Number: {pr_num if pr_num else 'N/A'}\n"
            f"Repository: {repo if repo else 'N/A'}\n\n"
            f"Compilation Errors:\n"
            f"==================================================\n"
            f"{errors}\n"
            f"==================================================\n\n"
            f"The Pull Request has been closed automatically without merging."
        )
        
        # 1. Send Email
        send_email(email_subject, email_body, RECIPIENT_EMAIL)
        
        # 2. Write error message to pr_review.md so workflow posts it as comment
        with open("pr_review.md", "w") as f:
            f.write(
                f"### ❌ Compilation Failure\n\n"
                f"The build failed. Please fix the compilation errors below:\n"
                f"```\n{errors}\n```\n\n"
                f"This pull request has been automatically closed."
            )
            
        sys.exit(1)
        
    else:
        print("Compilation check passed! Processing success workflow...")
        
        # Read diff file if exists
        pr_diff = "No diff provided."
        if os.path.exists("diff.txt"):
            with open("diff.txt", "r") as f:
                pr_diff = f.read().strip()
                
        # Run Groq code review
        groq_key = os.environ.get("GROQ_API_KEY")
        ai_review = run_groq_review(pr_diff, groq_key)
        
        email_subject = f"PR #{pr_num} Checks Passed - Ready to Merge" if pr_num else "Repository Compilation Successful"
        email_body = (
            f"Hello,\n\n"
            f"All checks have been passed and the PR is ready to merge.\n"
            f"Details:\n"
            f"PR Number: {pr_num if pr_num else 'N/A'}\n"
            f"Repository: {repo if repo else 'N/A'}\n\n"
            f"AI Code Review Feedback:\n"
            f"==================================================\n"
            f"{ai_review}\n"
            f"==================================================\n\n"
            f"Please perform the merge manually, as automatic merging has been skipped per instructions."
        )
        
        # 1. Send Email
        send_email(email_subject, email_body, RECIPIENT_EMAIL)
        
        # 2. Write comment to pr_review.md so workflow posts it as comment
        with open("pr_review.md", "w") as f:
            f.write(
                f"###  All checks have been passed and the code is clear and ready to merge.\n\n"
                f"#### 🤖 AI Code Review Findings\n{ai_review}"
            )
            
        sys.exit(0)

if __name__ == "__main__":
    main()
