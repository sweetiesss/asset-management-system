# Stage 1: Build the React app
FROM node:24-alpine AS builder

WORKDIR /app

#ARG VITE_BASE_URL
#ENV VITE_BASE_URL=$VITE_BASE_URL

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:stable-alpine

# Add custom nginx config for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build output to nginx public directory
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]