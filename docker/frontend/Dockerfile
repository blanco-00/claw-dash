FROM --platform=linux/amd64 node:20-bookworm-slim AS builder

WORKDIR /app
COPY package*.json ./
COPY vite.config.ts ./
COPY index.html ./
COPY src ./src

RUN npm install
RUN npx vite build

FROM --platform=linux/amd64 nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
