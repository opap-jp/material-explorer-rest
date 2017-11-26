FROM python:3.4

# パッケージのインストール
RUN apt-get update \
    && apt-get install -y imagemagick ghostscript \
    && pip install Flask==0.10.1

WORKDIR /app
COPY src /app
COPY cmd.sh /

EXPOSE 9090
CMD ["/cmd.sh"]
