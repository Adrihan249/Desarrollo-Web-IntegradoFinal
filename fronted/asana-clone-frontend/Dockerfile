# ----------------------------------------------------
# FASE 1: BUILD (Compilación)
# ----------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# Instalar dependencias con cache
COPY package.json .
COPY package-lock.json .
RUN npm install

# Copiar el resto del proyecto
COPY . .

# ❌ IMPORTANTE: NO FORZAR VITE_API_URL AQUÍ
# Render inyectará VITE_API_URL automáticamente
RUN npm run build

# ----------------------------------------------------
# FASE 2: NGINX (Producción)
# ----------------------------------------------------
FROM nginx:alpine

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar archivos de producción
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
