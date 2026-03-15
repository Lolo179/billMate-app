FROM node:22-alpine AS builder
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=
ARG VITE_USE_MSW=false

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_USE_MSW=$VITE_USE_MSW

RUN npm run build

FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist ./

EXPOSE 8083

CMD ["nginx", "-g", "daemon off;"]
