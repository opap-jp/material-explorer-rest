FROM haskell:8.0

MAINTAINER OPAP-JP

# ビルドツールの設定などを行ないます
# 依存パッケージを取得します
RUN mkdir /root/app
WORKDIR /root/app
COPY app/package.yaml .
COPY app/stack.yaml .
RUN stack setup \
    && stack build yesod-bin cabal-install --install-ghc \
    && stack build --dependencies-only

# アプリケーションのソースをコピーし、ビルドします
COPY app .
RUN stack build

# ポート開放
EXPOSE 3000
