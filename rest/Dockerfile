FROM haskell:8.0

MAINTAINER OPAP-JP

# ビルドツールの設定などを行ないます
# 依存パッケージを取得します
RUN mkdir /root/app
WORKDIR /root/app
COPY package.yaml .
COPY stack.yaml .
RUN stack setup
RUN stack build --dependencies-only

# アプリケーションのソースをコピーし、ビルドします
COPY . .
RUN stack build

# ポート開放
EXPOSE 8080
