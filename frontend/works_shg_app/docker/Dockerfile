# -------- Build Stage --------
FROM ghcr.io/cirruslabs/flutter:3.16.5 AS build

ARG WORK_DIR
WORKDIR /app

# Copy Flutter project into container
COPY ${WORK_DIR} .

# Debug (optional but recommended)
RUN pwd && ls -lah

# Ensure Flutter web is enabled
RUN flutter config --enable-web

# Verify Flutter installation
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

# Copy built web app to nginx
COPY --from=build /app/build/web/ ${WEB_DIR}/

# Copy nginx config
COPY --from=build /app/docker/nginx.conf /etc/nginx/conf.d/default.conf