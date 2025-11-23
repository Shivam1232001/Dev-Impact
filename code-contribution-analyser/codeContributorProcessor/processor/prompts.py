# constants.py

EXTRACT_CONTRIBUTORS_PROMPT = """
You are an expert software analyst.

Your ONLY task is to output a JSON object.
Do not include Markdown, code fences, explanations, comments, or any text outside the JSON.

Output MUST:
- Contain ONLY valid JSON.
- Begin with '{{' and end with '}}'.
- Follow EXACTLY this structure:

{{
  "contributors": [
    {{
      "name": "<contributor name>",
      "activities": ["<activity>", "..."]
    }}
  ]
}}

Do NOT add backticks.
Do NOT add extra text.
Do NOT explain anything.

Here is the GitHub contribution data:
{repo_data}
"""


ANALYZE_CONTRIBUTOR_IMPACT_PROMPT = """
You are a senior software project reviewer.

Your ONLY task is to output a JSON object.
Do not include Markdown, code fences, explanations, comments, or any text outside the JSON.

Output MUST:
- Contain ONLY valid JSON.
- Begin with '{{' and end with '}}'.
- Follow EXACTLY this structure:

{{
  "contributors": [
    {{
      "name": "<contributor name>",
      "impact_summary": "<summary>"
    }}
  ]
}}

Here is the contributor activity data:
{contributors_json}
"""


ASSIGN_PERCENTAGES_PROMPT = """
You are a software project evaluator.

Your ONLY task is to output a JSON object.
Do NOT include Markdown, code fences, explanations, or text outside the JSON.

Output MUST:
- Contain ONLY valid JSON.
- Begin with '{{' and end with '}}'.
- Total of all percentages MUST equal 100.
- Follow EXACTLY this structure:

{{
  "contributors": [
    {{
      "name": "<contributor name>",
      "contribution_percentage": <integer>,
      "impact_summary": "<existing impact summary>"
    }}
  ]
}}

Here is the contributor impact data:
{impact_json}
"""


GENERATE_FINAL_SUMMARY_PROMPT = """
You are a lead software analyst.

Your ONLY task is to output a JSON object.
Do NOT include Markdown, code fences, comments, or any text outside the JSON.

Output MUST:
- Contain ONLY valid JSON.
- Begin with '{{' and end with '}}'.
- Follow EXACTLY this structure:

{{
  "contributors": [
    {{
      "name": "<contributor name>",
      "contribution_percentage": <integer>,
      "impact_summary": "<concise summary>"
    }}
  ],
  "overall_summary": "<overall project summary>"
}}

Here is the contributor data:
{contribution_json}

Here is the raw project data:
{repo_data}
"""
