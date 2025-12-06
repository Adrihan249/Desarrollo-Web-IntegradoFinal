# ----------------------------------------------------
# FASE 1: BUILD (Compilación)
# Esta fase compila tu aplicación React/Vite.
# ----------------------------------------------------
FROM node:20-alpine AS builder

# Establece el directorio de trabajo
WORKDIR /app

# Copia los archivos de manifiesto e instala dependencias
# Esto permite que Docker cachee este paso si los archivos package.json no cambian.
COPY package.json .
COPY package-lock.json .
RUN npm install

# Copia el resto del código fuente
COPY . .

# Compila la aplicación. Vite genera los archivos estáticos en la carpeta 'dist'.
# **¡Asegúrate de que VITE_API_URL en tu .env apunta a la URL de producción de tu backend!**
# Si lo estás desplegando, el valor debería ser algo como:
# VITE_API_URL=/api (para usar el proxy de Render/Railway) o la URL pública. <-- LÍNEA CORREGIDA
RUN npm run build

# ----------------------------------------------------
# FASE 2: PRODUCTION (Servir)
# Se usa Nginx, una imagen mínima y segura para servir los archivos estáticos.
# ----------------------------------------------------
FROM nginx:alpine

# ¡IMPORTANTE! Crea un archivo de configuración de Nginx personalizado
# para manejar el enrutamiento (ej. con react-router-dom) y las peticiones al API.

# Copia el archivo de configuración personalizado de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia los archivos compilados ('dist') de la fase 'builder' al directorio de Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Puerto por defecto de Nginx que el contenedor expone
EXPOSE 80

# Comando para iniciar Nginx
CMD ["nginx", "-g", "daemon off;"]
