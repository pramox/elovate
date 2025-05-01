FROM node:21-alpine as build-stage

WORKDIR /app

COPY package*.json .

RUN npm ci

COPY . .

RUN npm run build --prod

FROM nginx:alpine

COPY --from=build-stage /app/dist/elovate_frontend/ /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
