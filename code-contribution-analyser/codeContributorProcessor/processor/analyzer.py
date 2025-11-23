import json
import logging
import requests
from .models import AnalysisData
from .constants import GEMINI_API_URL, GEMINI_API_KEY, GITHUB_MS_URL
from .prompts import (
    EXTRACT_CONTRIBUTORS_PROMPT,
    ANALYZE_CONTRIBUTOR_IMPACT_PROMPT,
    ASSIGN_PERCENTAGES_PROMPT,
    GENERATE_FINAL_SUMMARY_PROMPT
)
from pathlib import Path

logger = logging.getLogger(__name__)


def call_gemini_api(prompt):
    """Helper to call Gemini API and return parsed JSON output."""
    logger.info("Sending request to Gemini API.")
    try:
        response = requests.post(
            GEMINI_API_URL,
            headers={
                "x-goog-api-key": GEMINI_API_KEY,
                "Content-Type": "application/json",
            },
            json={"contents": [{"parts": [{"text": prompt}]}]},
            timeout=120
        )
        logger.debug(f"Gemini API raw response: {response.text[:500]}...")  # log first 500 chars
        response.raise_for_status()

        text_output = response.json().get("candidates", [{}])[0].get("content", {}).get("parts", [{}])[0].get("text", "")
        text_output = text_output.replace("```json", "")
        text_output = text_output.replace("```", "")
        logger.info(text_output)
        return json.loads(text_output)
    except (requests.RequestException, json.JSONDecodeError) as e:
        logger.error(f"Gemini API error: {e}")
        return None


def extract_contributors(repo_data):
    logger.info("Starting Step 1: Extract contributors.")
    prompt = EXTRACT_CONTRIBUTORS_PROMPT.format(repo_data=json.dumps(repo_data, indent=2))
    result = call_gemini_api(prompt)
    if result:
        logger.info("Step 1 completed: Contributors extracted successfully.")
    else:
        logger.warning("Step 1 failed: Could not extract contributors.")
    return result


def analyze_contributor_impact(contributors_json):
    logger.info("Starting Step 2: Analyze contributor impact.")
    prompt = ANALYZE_CONTRIBUTOR_IMPACT_PROMPT.format(contributors_json=json.dumps(contributors_json, indent=2))
    result = call_gemini_api(prompt)
    if result:
        logger.info("Step 2 completed: Contributor impact analysis successful.")
    else:
        logger.warning("Step 2 failed: Contributor impact analysis failed.")
    return result


def assign_percentages(impact_json):
    logger.info("Starting Step 3: Assign contribution percentages.")
    prompt = ASSIGN_PERCENTAGES_PROMPT.format(impact_json=json.dumps(impact_json, indent=2))
    result = call_gemini_api(prompt)
    if result:
        logger.info("Step 3 completed: Percentages assigned successfully.")
    else:
        logger.warning("Step 3 failed: Could not assign contribution percentages.")
    return result


def generate_final_summary(contribution_json, repo_data):
    logger.info("Starting Step 4: Generate final summary.")
    prompt = GENERATE_FINAL_SUMMARY_PROMPT.format(
        contribution_json=json.dumps(contribution_json, indent=2),
        repo_data=json.dumps(repo_data, indent=2)
    )
    result = call_gemini_api(prompt)
    if result:
        logger.info("Step 4 completed: Final summary generated successfully.")
    else:
        logger.warning("Step 4 failed: Could not generate final summary.")
    return result


def summarize_with_gemini_chained(repo_data):
    logger.info("Initiating chained Gemini summarization workflow.")

    step1 = extract_contributors(repo_data)
    if not step1:
        logger.error("Workflow halted at Step 1.")
        return None

    step2 = analyze_contributor_impact(step1)
    if not step2:
        logger.error("Workflow halted at Step 2.")
        return None

    step3 = assign_percentages(step2)
    if not step3:
        logger.error("Workflow halted at Step 3.")
        return None

    final_summary = generate_final_summary(step3, repo_data)
    if final_summary:
        logger.info("Workflow completed successfully.")
    else:
        logger.error("Workflow failed during final summary generation.")

    return final_summary


def statistics(repo_data):
    logger.info("Calculating commit statistics for contributors.")
    analysis = []
    for contributor in repo_data:
        code_changes = sum(
            file["additions"] + file["deletions"]
            for commit in contributor["commits"]
            for file in commit["files"]
        )
        analysis.append({
            "author": contributor["authorName"],
            "total_commits": len(contributor["commits"]),
            "lines_changed": code_changes
        })
    logger.info("Statistics computation completed.")
    return analysis


def get_github_data(repo_url, user_id):
    logger.info(f"Fetching GitHub data for repo: {repo_url}")
    graphql_query = """
    query($repoUrl: String!, $userId: ID!) {
        repoData(repoUrl: $repoUrl, userId: $userId) {
            authorName
            githubUrl
            contributions
            commits {
                msg
                sha
                date
                files {
                    fileName
                    extension
                    code
                    additions
                    deletions
                }
            }
        }
    }
    """

    payload = {
        "query": graphql_query,
        "variables": {"repoUrl": repo_url, "userId": user_id}
    }

    try:
        response = requests.post(GITHUB_MS_URL, json=payload, timeout=60)
        response.raise_for_status()
        data = response.json()
        logger.info("GitHub data fetched successfully.")
        return data.get("data", {}).get("repoData", [])
    except requests.RequestException as e:
        logger.error(f"GitHub API request failed: {e}")
        return []


def analysis_code_contributions(data):
    logger.info("Starting full analysis process.")
    try:
        analysis_data = AnalysisData.objects.get(id=data.get("id"))
    except AnalysisData.DoesNotExist:
        logger.error(f"AnalysisData with ID {data.get('id')} not found.")
        return

    repo_data = get_github_data(analysis_data.repo_url, analysis_data.user_id)
    stats_summary = statistics(repo_data)
    gemini_summary = summarize_with_gemini_chained(repo_data)

    analysis_data.status = "completed"
    analysis_data.analysis_result = {
        "statistics": stats_summary,
        "ai_summary": gemini_summary,
    }
    analysis_data.save()
    logger.info(f"Analysis data saved for ID {analysis_data.id}.")

    payload = {
        "statistics": stats_summary,
        "ai_summary": gemini_summary,
    }

    try:
        response = requests.post(analysis_data.call_back_url, json=payload, timeout=10)
        response.raise_for_status()
        logger.info(f"Callback POST successful for analysis ID {analysis_data.id}.")
    except Exception as e:
        logger.error(f"Error sending callback POST request: {e}")
