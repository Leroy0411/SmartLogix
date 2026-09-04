# Build: compila el SPA de React con Vite.
# Las variables VITE_* se "hornean" en el bundle en tiempo de build,
# por eso se reciben como build-args (ver docker-compose.yml).
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .

ARG VITE_BFF_URL=http://localhost:8080
ARG VITE_MS_INVENTARIO=http://localhost:8081
ARG VITE_MS_PEDIDOS=http://localhost:8082
ARG VITE_API_KEY=smartlogix-dev-key
ENV VITE_BFF_URL=$VITE_BFF_URL
ENV VITE_MS_INVENTARIO=$VITE_MS_INVENTARIO
ENV VITE_MS_PEDIDOS=$VITE_MS_PEDIDOS
ENV VITE_API_KEY=$VITE_API_KEY

RUN npm run build

# Runtime: sirve los archivos estáticos ya compilados con nginx.
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
