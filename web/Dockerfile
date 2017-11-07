FROM nginx:1.13

MAINTAINER OPAP-JP

# パッケージのインストール
RUN apt-get update \
    && apt-get install -y curl gnupg \
    && curl -sL https://deb.nodesource.com/setup_6.x | bash -

# 設定ファイルを配置します
COPY conf/nginx.conf /etc/nginx
COPY conf/mime.types /etc/nginx
COPY conf/default.conf /etc/nginx/conf.d

# アプリケーションのソースをコピーし、ビルドします
COPY src /usr/share/nginx/html
RUN perl -i -ple 's!http://localhost:8080!$ENV{REST_HOST}/!;' /usr/share/nginx/html/js/rest-client.js

# アセットを配置します

# ポート開放
EXPOSE 80

CMD "nginx"
