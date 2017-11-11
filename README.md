# Material Explorer
素材検索システム（仮称）

## 開発環境

### 動作確認

#### Windows

[Docker for Windows](https://docs.docker.com/docker-for-windows/install/)がインストールされていることが前提です。

1. Windows PowerShell を開き、カレントディレクトリがこのプロジェクトのルートフォルダ（`docker-compose.yaml` があるフォルダ）になるようにします。
1. `docker-compose build` で Docker イメージのビルドを行ないます。初回のビルドには数分～数十分程度の時間がかかります。
1. `docker-compose up` で 開発用サーバーが起動します。
1. [http://localhost](http://localhost) でアプリケーションを開きます。

 docker-compose -f .\docker-compose.production.yaml up --build


npm
IntelliJ IDEA
Scala

アーキテクチャ
Docker イメージの構成
GitLab API への依存
webpack
TypeScript
SCSS
