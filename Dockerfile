FROM ubuntu:20.04

LABEL maintainer OPAP-JP

# ローケルを設定します
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
RUN apt-get update \
    && apt-get -y install locales \
    && locale-gen en_US.UTF-8

# 依存パッケージを取得します
# Scala, sbt のインストールを行ないます
RUN apt-get update
RUN apt-get install -y default-jdk
RUN apt-get install -y apt-transport-https
RUN apt-get install -y scala
RUN apt-get install -y dirmngr bc
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
    && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
    && apt-get update \
    && apt-get install -y sbt
RUN echo exit | sbt

# アプリケーションのソースをコピーし、ビルドします
RUN mkdir /tmp/material-explorer
RUN mkdir /root/material-explorer
WORKDIR /tmp/material-explorer
COPY build.sbt .
COPY project project
RUN sbt clean && sbt -Dpackaging.type=jar update

# ビルド
COPY ./project ./project
COPY ./src ./src
RUN sbt -Dpackaging.type=jar assembly
RUN mv target/material-explorer.jar /root/material-explorer/

WORKDIR /root/material-explorer/

# ポート公開
EXPOSE 8080
