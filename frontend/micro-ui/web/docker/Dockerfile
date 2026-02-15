FROM node:20-alpine AS build
RUN apk update && apk upgrade
RUN apk add --no-cache git>2.30.0
# Install dependencies required for node-gyp (Python, make, g++, etc.)
RUN apk add --no-cache python3 make g++ \
    && ln -sf /usr/bin/python3 /usr/bin/python

ARG WORK_DIR
WORKDIR /app
ENV NODE_OPTIONS "--max-old-space-size=8500 --openssl-legacy-provider"
ENV GENERATE_SOURCEMAP "false"

COPY ${WORK_DIR} .
RUN ls -lah

RUN cd web/ \
    && node -e 'console.log(v8.getHeapStatistics().heap_size_limit/(1024*1024))' \
    && ./install-deps.sh \
    && yarn install \
    && yarn build:webpack

FROM nginx:mainline-alpine
ENV WORK_DIR=/var/web/digit-ui

RUN mkdir -p ${WORK_DIR}

COPY --from=build /app/web/build ${WORK_DIR}/
COPY --from=build /app/web/docker/nginx.conf /etc/nginx/conf.d/default.conf