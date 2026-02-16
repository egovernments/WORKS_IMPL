# -------- Build Stage --------
FROM node:20-alpine AS build

RUN apk update && apk upgrade

RUN apk add --no-cache git>2.30.0

RUN apk add --no-cache python3 make g++ \
    && ln -sf /usr/bin/python3 /usr/bin/python

ARG WORK_DIR
WORKDIR /app

ENV NODE_OPTIONS="--max-old-space-size=8500 --openssl-legacy-provider"
ENV GENERATE_SOURCEMAP=false
ENV YARN_DEBUG=true

ARG REACT_APP_PUBLIC_PATH
ENV REACT_APP_PUBLIC_PATH=${REACT_APP_PUBLIC_PATH}

# Copy frontend/micro-ui into /app
COPY ${WORK_DIR} .

# Confirm structure
RUN pwd && ls -lah

# Switch to web folder (THIS IS REQUIRED)
WORKDIR /app/web

# Confirm correct folder
RUN pwd && ls -lah

# Show heap size
RUN node -e "console.log('Heap limit (MB):', v8.getHeapStatistics().heap_size_limit / (1024 * 1024))"

# Make script executable
RUN chmod +x install-deps.sh

# Increase yarn timeout
RUN yarn config set network-timeout 600000

# Install dependencies
RUN ./install-deps.sh

RUN yarn install

# Build application
RUN yarn build:webpack


# -------- Production Stage --------
FROM nginx:mainline-alpine

ENV WORK_DIR=/var/web/digit-ui

RUN mkdir -p ${WORK_DIR}

COPY --from=build /app/web/build ${WORK_DIR}/

COPY --from=build /app/web/docker/nginx.conf /etc/nginx/conf.d/default.conf