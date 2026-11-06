# ------------- Etapa 1: compilar Angular -------------
FROM node:22-alpine AS build
WORKDIR /build

# Igual que en el backend: primero las dependencias, para aprovechar la cache
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ------------- Etapa 2: servir los archivos estaticos -------------
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
