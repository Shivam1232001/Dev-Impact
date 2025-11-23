from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = 'django-insecure-sc^9w7r__(1#xtl-t=5c3jlrve@!&+b$27(#aehz#dgjdfc2f+'
DEBUG = True
ALLOWED_HOSTS = ['*']

# ---------------------------------------------------------------------
# INSTALLED_APPS – trimmed for API-only use
# ---------------------------------------------------------------------
INSTALLED_APPS = [
    'django.contrib.auth',          
    'django.contrib.contenttypes',  
    'django.contrib.staticfiles',   
    'rest_framework',
    'drf_spectacular',
    'processor',                    
]

# ---------------------------------------------------------------------
# REST Framework / Spectacular
# ---------------------------------------------------------------------
REST_FRAMEWORK = {
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.AllowAny',
    ],
    'DEFAULT_AUTHENTICATION_CLASSES': [],
    'DEFAULT_SCHEMA_CLASS': 'drf_spectacular.openapi.AutoSchema',
}

# ---------------------------------------------------------------------
# Middleware – minimal, no sessions or auth
# ---------------------------------------------------------------------
MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.middleware.common.CommonMiddleware',
]

ROOT_URLCONF = 'codeContributorProcessor.urls'

# ---------------------------------------------------------------------
# Templates – required for Swagger UI rendering
# ---------------------------------------------------------------------
TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
            ],
        },
    },
]

WSGI_APPLICATION = 'codeContributorProcessor.wsgi.application'

# ---------------------------------------------------------------------
# Database (PostgreSQL)
# ---------------------------------------------------------------------
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': 'dev-impact-processor',
        'USER': 'user',
        'PASSWORD': 'pass',
        'HOST': 'localhost',
        'PORT': '9234',
    }
}

# ---------------------------------------------------------------------
# Other Settings
# ---------------------------------------------------------------------
LANGUAGE_CODE = 'en-us'
TIME_ZONE = 'UTC'
USE_I18N = False
USE_TZ = False

SPECTACULAR_SETTINGS = {
    "TITLE": "Code Contributor Processor API Documentation",
    "DESCRIPTION": "API documentation for the Code Contributor Processor service",
    "VERSION": "1.0.0",
    "SERVE_INCLUDE_SCHEMA": False,
    "SECURITY": [],
}

STATIC_URL = '/static/'
DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'json': {
            'format': (
                '{"timestamp": "%(asctime)s", '
                '"level": "%(levelname)s", '
                '"service": "code-analyser", '
                '"message": "%(message)s"}'
            )
        }
    },
    'handlers': {
        'file': {
            'class': 'logging.FileHandler',
            'filename': 'logs/django.log',
            'formatter': 'json',
        }
    },
    'root': {
        'handlers': ['file'],
        'level': 'INFO',
    },
}

