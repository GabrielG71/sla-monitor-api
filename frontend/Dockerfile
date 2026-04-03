# 1. Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Instala dependências
COPY package*.json ./
RUN npm install

# Copia o resto do projeto
COPY . .

# Build do Next
RUN npm run build

# 2. Runtime stage (leve)
FROM node:20-alpine

WORKDIR /app

# Copia apenas o necessário
COPY --from=builder /app/package*.json ./
RUN npm install --omit=dev

COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.js ./

EXPOSE 3000

CMD ["npm", "start"]