FROM python:3.8.5-slim

RUN apt-get update \
    && apt-get upgrade --yes \
    && apt-get install --yes --no-install-recommends git-core \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt /opt/server/requirements.txt
RUN pip install -r /opt/server/requirements.txt

COPY . /opt/server

EXPOSE 8000
ENV OBEX_DEBUG='False'
CMD ["python", "/opt/server/manage.py", "runserver", "0.0.0.0:8000"]
