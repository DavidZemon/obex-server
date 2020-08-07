"""
Django settings for obex project.

Generated by 'django-admin startproject' using Django 3.0.5.

For more information on this file, see
https://docs.djangoproject.com/en/3.0/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/3.0/ref/settings/
"""

import os

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/3.0/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = '4@f9cx0)ag7c@9%pig2zlk^maspmb1^qvei@hq^i9%_cn1ky_%'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = 'True' == os.environ.get('OBEX_DEBUG', 'True')

ALLOWED_HOSTS = [h.strip() for h in os.environ.get('OBEX_ALLOWED_HOSTS', '').split(',') if h.strip()]

# Application definition

INSTALLED_APPS = [
    'django.contrib.contenttypes',
    'django.contrib.messages',
    'django.contrib.staticfiles',
]

MIDDLEWARE = [
    'django.middleware.common.CommonMiddleware'
]

ROOT_URLCONF = 'obex.urls'


WSGI_APPLICATION = 'obex.wsgi.application'


# Internationalization
# https://docs.djangoproject.com/en/3.0/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_L10N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/3.0/howto/static-files/

STATIC_URL = '/static/'
