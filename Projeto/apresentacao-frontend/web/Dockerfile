# syntax=docker/dockerfile:1

# ─────────────────────────────────────────────────────────────────────────────
# Estágio de build — compila o SPA React/Vite (gera /app/dist).
# ─────────────────────────────────────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

# Instala dependências a partir do lockfile (camada cacheável).
COPY package.json package-lock.json ./
RUN npm ci

# Copia o restante e gera o build de produção (tsc -b && vite build).
COPY . .
RUN npm run build

# ─────────────────────────────────────────────────────────────────────────────
# Estágio de runtime — nginx servindo os estáticos + proxy de /api ao backend.
# ─────────────────────────────────────────────────────────────────────────────
FROM nginx:alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
