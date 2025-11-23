from django.views.decorators.csrf import csrf_exempt
from .models import Job, AnalysisData
from drf_spectacular.utils import extend_schema
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status


@extend_schema(
    summary="API to initiate code contribution analysis",
    description=(
        "Accepts repository details and creates a new job entry "
        "with related analysis data. Returns the created job ID,"
        "and hits the callback URL when done, with the results."
    ),
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "repo_url": {"type": "string", "example": "https://github.com/org/repo"},
                "user_id": {"type": "string", "example": "123"},
                "call_back_url": {"type": "string", "example": "https://example.com/callback"},
            },
            "required": ["repo_url", "user_id", "call_back_url"],
        }
    },
    responses={
        200: {
            "type": "object",
            "properties": {
                "message": {"type": "string", "example": "accepted"},
                "job_id": {"type": "integer", "example": 1},
            },
        },
        400: {
            "type": "object",
            "properties": {
                "error": {"type": "string", "example": "Invalid input"},
            },
        },
    },
)
@api_view(['POST'])
@csrf_exempt
def initiate_code_contributor_analysis(request):
    try:
        data = request.data 
        repo_url = data.get('repo_url')
        user_id = data.get('user_id')
        call_back_url = data.get('call_back_url')

        # Validate required fields
        if not all([repo_url, user_id, call_back_url]):
            return Response({"error": "Missing required fields."}, status=status.HTTP_400_BAD_REQUEST)

        # Create AnalysisData record
        analysis_data = AnalysisData.objects.create(
            repo_url=repo_url,
            user_id=user_id,
            call_back_url=call_back_url,
            status='pending'
        )

        # Create related Job record
        job = Job.objects.create(
            name='analysis_code_contributions',
            data={'id': analysis_data.id},
            status='pending'
        )

        return Response({"message": "accepted", "job_id": job.id}, status=status.HTTP_200_OK)

    except Exception as e:
        return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)
    

@extend_schema(
    summary="Get job status and results",
    description=(
        "Fetch the current status of a job using its ID. "
        "If the job is completed, returns the analysis result as well."
    ),
    parameters=[
        {
            "name": "job_id",
            "in": "path",
            "required": True,
            "schema": {"type": "integer"},
            "description": "The ID of the job to fetch."
        }
    ],
    responses={
        200: {
            "type": "object",
            "properties": {
                "job_id": {"type": "integer", "example": 1},
                "status": {"type": "string", "example": "completed"},
                "result": {
                    "type": "object",
                    "example": {
                        "statistics": [{"author": "Alice", "total_commits": 15}],
                        "ai_summary": {"contributors": [], "overall_summary": "Excellent collaboration."}
                    }
                },
            },
        },
        404: {
            "type": "object",
            "properties": {
                "error": {"type": "string", "example": "Job not found"},
            },
        },
    },
)
@api_view(['GET'])
@csrf_exempt
def get_analysis_status(request, job_id):
    try:
        job = Job.objects.filter(id=job_id).first()
        if not job:
            return Response({"error": "Job not found"}, status=status.HTTP_404_NOT_FOUND)

        response_data = {
            "job_id": job.id,
            "status": job.status,
        }

        if job.status == "completed":
            analysis_data = AnalysisData.objects.filter(id=job.data.get("id")).first()
            if analysis_data:
                response_data["result"] = analysis_data.analysis_result

        return Response(response_data, status=status.HTTP_200_OK)

    except Exception as e:
        return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)
