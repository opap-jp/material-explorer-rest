FROM nginx:1.13

MAINTAINER OPAP-JP

# パッケージのインストール
RUN apt-get update \
    && apt-get install -y curl gnupg \
    && curl -sL https://deb.nodesource.com/setup_6.x | bash - \
    && apt-get install -y nodejs

# 設定ファイルを配置します
COPY conf/nginx.conf /etc/nginx
COPY conf/mime.types /etc/nginx
COPY conf/default.conf /etc/nginx/conf.d

# アプリケーションのソースをコピーし、ビルドします
RUN mkdir /tmp/material-explorer
COPY package.json /tmp/material-explorer
WORKDIR /tmp/material-explorer
RUN npm install
COPY . /tmp/material-explorer

ARG service_host
ENV SERVICE_HOST="$service_host"
RUN npm run build

# アセットを配置します
RUN cp -R dist/* /usr/share/nginx/html

# ポート開放
EXPOSE 80

CMD "nginx"
