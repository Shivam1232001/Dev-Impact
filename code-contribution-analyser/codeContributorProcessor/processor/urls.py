# jobs/urls.py
from django.urls import path
from .views import initiate_code_contributor_analysis, get_analysis_status

urlpatterns = [
    path('v1.0.0/initiate/', initiate_code_contributor_analysis, name='initiate_code_contributor_analysis'),
    path('v1.0.0/status/<int:job_id>/', get_analysis_status, name='get_analysis_status'),
]
