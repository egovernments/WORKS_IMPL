FROM python:3.9.0a3-alpine3.10
ARG WORK_DIR
WORKDIR /app/cron/
COPY ${WORK_DIR}/requirements.txt .
RUN pip3 install -r requirements.txt
COPY ${WORK_DIR}/.env .env
COPY ${WORK_DIR}/cronJobAPIConfig.py cronJobAPIConfig.py