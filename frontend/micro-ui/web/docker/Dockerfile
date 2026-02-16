# -------- Build Stage --------
FROM node:20-alpine AS build

# Update Alpine and install required packages
RUN apk update && apk upgrade

# Install git
RUN apk add --no-cache git>2.30.0

# Install dependencies required for node-gyp
RUN apk add --no-cache python3 make g++ \
    && ln -sf /usr/bin/python3 /usr/bin/python

# Set working directory
ARG WORK_DIR
WORKDIR /app

# Environment variables
ENV NODE_OPTIONS="--max-old-space-size=8500 --openssl-legacy-provider"
ENV YARN_DEBUG=true
ENV GENERATE_SOURCEMAP=false

# Accept public path argument
ARG REACT_APP_PUBLIC_PATH
ENV REACT_APP_PUBLIC_PATH=${REACT_APP_PUBLIC_PATH}

# Copy application files into container
COPY ${WORK_DIR} .

# Debug: verify files copied correctly (safe to keep)
RUN pwd && ls -lah

# Show Node heap size (debug)
RUN node -e "console.log('Heap limit (MB):', v8.getHeapStatistics().heap_size_limit / (1024 * 1024))"

# Make install script executable
RUN chmod +x install-deps.sh

# Increase yarn timeout for CI stability
RUN yarn config set network-timeout 600000

# Install dependencies
RUN ./install-deps.sh
RUN yarn install

# Build the application
RUN yarn build:webpack


# -------- Production Stage --------
FROM nginx:mainline-alpine

# Set target directory
ENV WORK_DIR=/var/web/digit-ui

# Create directory
RUN mkdir -p ${WORK_DIR}

# Copy built UI files
COPY --from=build /app/build ${WORK_DIR}/

# Copy nginx config
COPY --from=build /app/docker/nginx.conf /etc/nginx/conf.d/default.conf