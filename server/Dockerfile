FROM node:20-bookworm-slim

WORKDIR /app

COPY package*.json ./
COPY server ./server

RUN npm install && npm install -D tsx

EXPOSE 3001

CMD ["npx", "tsx", "server/index.ts"]
