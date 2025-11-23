import time
import logging
from django.db import transaction
from .models import Job, AnalysisData
from .analyzer import analysis_code_contributions

# Configure logger
logger = logging.getLogger(__name__)

def analysis_contributions(data):
    logger.info("Starting code contribution analysis.")
    try:
        analysis_code_contributions(data)
        logger.info("Code contribution analysis completed successfully.")
    except Exception as e:
        logger.exception(f"Error during contribution analysis: {e}")
        raise

def process_job(job):
    logger.info(f"Processing job {job.id} with name '{job.name}'.")
    try:
        job_name = job.name

        if job_name == 'analysis_code_contributions':
            analysis_contributions(job.data)
        else:
            logger.warning(f"Unknown job type: {job_name}")

        job.status = "completed"
        job.save()
        logger.info(f"Job {job.id} completed successfully.")
    except Exception as e:
        logger.exception(f"Error while processing job {job.id}: {e}")
        job.status = "failed"
        job.save()

def worker_loop():
    logger.info("Worker started and waiting for jobs.")
    while True:
        try:
            with transaction.atomic():
                job = (
                    Job.objects.select_for_update(skip_locked=True)
                    .filter(status="pending")
                    .first()
                )
                if job:
                    logger.info(f"Job {job.id} fetched for processing.")
                    job.status = "processing"
                    job.save()
                else:
                    job = None

            if job:
                process_job(job)
            else:
                logger.debug("No pending jobs found. Sleeping for 5 seconds.")
                time.sleep(5)
        except Exception as e:
            logger.exception(f"Worker encountered an error: {e}")
            time.sleep(5)
