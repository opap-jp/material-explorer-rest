FROM nginx:1.13

MAINTAINER OPAP-JP

# パッケージのインストール
RUN apt-get update \
    && apt-get install -y curl gnupg \
    && curl -sL https://deb.nodesource.com/setup_6.x | bash - \
    && apt-get install -y nodejs

# 設定ファイルを配置します

# アプリケーションのソースをコピーし、ビルドします

# アセットを配置します

# ポート開放
EXPOSE 80
