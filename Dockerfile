FROM node:lts-slim

WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev

COPY src/ ./src/
COPY migrations/ ./migrations/

ENV NODE_ENV=production
ENV PORT=8080
ENV HOST=0.0.0.0
ENV DB_PATH=/data/webapi.db

EXPOSE 8080

VOLUME ["/data"]

CMD ["node", "src/server.js"]
