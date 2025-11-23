from django.db import models

class Job(models.Model):
    STATUS_CHOICES = [
        ('pending', 'Pending'),
        ('processing', 'Processing'),
        ('completed', 'Completed'),
        ('failed', 'Failed'),
    ]

    name = models.CharField(max_length=255, default='default_job')
    data = models.JSONField(null=True, blank=True)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='pending')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"Job {self.id} - {self.status}"

class AnalysisData(models.Model):
    STATUS_CHOICES = [
        ('pending', 'Pending'),
        ('processing', 'Processing'),
        ('completed', 'Completed'),
        ('failed', 'Failed'),
    ]

    repo_url = models.URLField()
    user_id = models.CharField(max_length=100)
    call_back_url = models.URLField()
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='pending')
    analysis_result = models.JSONField(null=True, blank=True)
