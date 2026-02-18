FROM python:3.11.9-alpine3.20
# Install gcc and other necessary build tools
ARG WORK_DIR
RUN apk add --no-cache gcc musl-dev libc-dev librdkafka-dev
WORKDIR /app/cron/
COPY ${WORK_DIR}/requirements.txt .
RUN pip3 install -r requirements.txt
COPY ${WORK_DIR}/.env .env
COPY ${WORK_DIR}/transformer.py transformer.py