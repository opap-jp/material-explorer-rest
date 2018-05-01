# 2. プロジェクトをサービスごとのリポジトリに分割する

日付: 2018-05-01

## 状態

Accepted

## 前提

[opap-jp/material-explorer](https://github.com/opap-jp/material-explorer) は、Docker Compose の使用を前提としたマイクロサービスアーキテクチャのアプリケーションとして開発されています。

これまで、 Docker Compose の設定ファイルですべてのマイクロサービスのソースを参照しなければならないために、すべてのマイクロサービスのソースを一つの GitHub リポジトリで一元的に管理して来ました。
しかし、GitHub の CI ツールである Travis CI は、リポジトリで単一のプログラミング言語が使われることを前提としているため、Scala，TypeScript，Python といったいろいろなプログラミング言語が使われているこのアプリケーションでは、すべてのサービスに対するテストは困難であり、満足なカバレッジの取得ができません。

## 決定

opap-jp/material-explorer をサービスごとのリポジトリに分割します。

## 影響

- どのサービスのリポジトリも、内容としてこれまでの opap-jp/material-explorer のコミットを継承したものとして作成されます。
- Docker Compose には、マイクロサービスのソースとして Git リポジトリを直接参照する機能があります。
  そのため、今後は Docker Compose の設定ファイルでは opap-jp/material-explorer 内のソースではなくそれぞれのサービスの GitHub リポジトリを参照することになるでしょう。
- opap-jp/material-explorer は、主に Docker Compose のためのリポジトリになります。
- 軽量アーキテクチャ決定記録を含む各ドキュメントは、それぞれのサービスのリポジトリに移動されません。
  すべてのドキュメントが opap-jp/material-explorer で一元的に管理されます。

https://github.com/opap-jp/material-explorer/issues/13
