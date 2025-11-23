from django.apps import AppConfig
import threading

class ProcessorConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'processor'

    def ready(self):
        from .worker import worker_loop
        threading.Thread(target=worker_loop, daemon=True).start()
