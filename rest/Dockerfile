FROM ubuntu:17.10

MAINTAINER OPAP-JP

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
COPY . .
RUN sbt -Dpackaging.type=jar assembly
RUN mv target/material-explorer.jar /root/material-explorer/
WORKDIR /root/material-explorer/

# ポート開放
EXPOSE 8080
