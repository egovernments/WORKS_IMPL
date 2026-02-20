# -------- Build Stage --------
FROM ghcr.io/cirruslabs/flutter:3.22.2 AS build

ARG WORK_DIR
WORKDIR /app

COPY ${WORK_DIR} .

RUN pwd && ls -lah

RUN flutter config --enable-web

RUN flutter doctor -v

RUN flutter clean

RUN flutter pub get --no-precompile

RUN flutter build web --release


# -------- Production Stage --------
FROM nginx:mainline-alpine

ENV WEB_DIR=/var/web/works-shg-app

RUN mkdir -p ${WEB_DIR}

COPY --from=build /app/build/web/ ${WEB_DIR}/

COPY --from=build /app/docker/nginx.conf /etc/nginx/conf.d/default.conf