# -------- Build Stage --------
FROM ghcr.io/cirruslabs/flutter:3.16.5 AS build

ARG WORK_DIR
WORKDIR /app

# Copy Flutter project
COPY ${WORK_DIR} .

# Debug (safe to keep)
RUN pwd && ls -lah

# Verify Flutter
RUN flutter doctor -v

# Clean and fetch dependencies
RUN flutter clean
RUN flutter pub get --no-precompile

# Build Flutter web release
RUN flutter build web --release


# -------- Production Stage --------
FROM nginx:mainline-alpine

ENV WEB_DIR=/var/web/works-shg-app

RUN mkdir -p ${WEB_DIR}

# Copy built Flutter web files
COPY --from=build /app/build/web/ ${WEB_DIR}/

# Copy nginx config
COPY --from=build /app/docker/nginx.conf /etc/nginx/conf.d/default.conf