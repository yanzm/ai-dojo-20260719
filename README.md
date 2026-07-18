# Android Studio と Gemini で加速する、次世代の Android アプリ開発 (ハンズオン)

Google AI Dojo Japan のハンズオンセッション。Gemini in Android Studio の Agent モードを使いこなし、写真に絵文字スタンプを押せるアプリを実装する。

- **イベント**: [Google AI Dojo Japan](https://rsvp.withgoogle.com/events/google-ai-dojo-japan)
- **セッション**: Android Studio と Gemini で加速する、次世代の Android アプリ開発 (ハンズオン)
- **レベル**: L200（中級）
- **形式**: オンライン / 2時間
- **講師**: Yuki Anzai（Android Google Developer Expert）

## 内容

Agent モードの Rules 設定・コンテキスト管理・制約付きプロンプト・差分レビューといった「Agent を実務で使う技術」を軸に、Photo Picker / カメラ Intent + FileProvider / Compose のジェスチャ処理を Agent とペアで実装する。

作るアプリは「Intent で取得した（選んだ／カメラで撮った）写真に絵文字のスタンプを押せる」アプリ。

## Codelab

[googlecodelabs/tools](https://github.com/googlecodelabs/tools)（claat）形式の Codelab。

- ソース: [codelab/emoji-stamp-with-gemini.md](codelab/emoji-stamp-with-gemini.md)
- スクリーンショット: `codelab/img/`（`<ページ番号>-<ページ内連番>-<内容>.png` の命名規則）
- 完成版の参考アプリコード: [app/EmojiStamp](app/EmojiStamp)

### ビルド方法

```bash
# Go が必要。インストール後:
go install github.com/googlecodelabs/tools/claat@latest

cd codelab
claat export -o ../docs emoji-stamp-with-gemini.md   # → docs/ に HTML 一式を生成
claat export emoji-stamp-with-gemini.md && claat serve  # → ローカルでプレビュー
```

## 公開ページ

GitHub Pages（`main` ブランチの `/docs`）で公開:
https://yanzm.github.io/ai-dojo-20260719/emoji-stamp-with-gemini/

infobox の記法は `> aside positive` / `> aside negative`（引用ブロックの1行目）を使うこと。
