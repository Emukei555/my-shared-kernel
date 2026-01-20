# ADR-001: エラー処理にResultパターンを採用し、例外を廃止

## ステータス
Accepted

## 日付
- 提案日: 2026-01-20
- 決定日: 2026-01-20

## コンテキスト
ドメイン層ではバリデーションやビジネスルール違反（残高不足、在庫切れなど）が頻発します。  
従来の例外ベースのエラーハンドリングには以下の課題がありました：

- 制御フローの隠蔽（例外 = GOTOのような突然のジャンプ）
- 型安全性の欠如（コンパイラがハンドリングを強制しない）
- パフォーマンスコスト（スタックトレース生成の負荷）
- ドメイン表現の欠落（ビジネス上の「失敗」は重要な事実なのに例外扱い）

Java 21の機能（record, sealed interface, pattern matching）を活用し、より堅牢で可読性の高いエラー処理を実現するため、見直しを行いました。

## 検討した選択肢

### 選択肢1: Result<T> パターン（Railway Oriented Programming）
自作ライブラリ（my-shared-kernel）のResult<T>型を使用。  
成功（Success<T>）または失敗（Failure<T>）を値として返す。

**メリット**
- コンパイラによる網羅性チェック（switch式 + sealedで強制）
- 制御フローの明示（シグネチャで失敗可能性が一目瞭然）
- Railwayスタイルで直線的な正常パス記述が可能

**デメリット**
- 記述量の増加（map/flatMapのチェーンが必要）
- Spring @Transactionalとの相性問題（自動ロールバックしない）

### 選択肢2: 実行時例外（RuntimeException）の利用
Spring Boot標準の例外ハンドリング。

**メリット**
- 記述がシンプル
- @Transactionalによる自動ロールバック

**デメリット**
- ハンドリング漏れが発生しやすい
- 失敗理由の詳細が伝わりにくい
- 制御フローが隠蔽され、可読性低下

## 決定
**選択肢1: Result<T> パターンを採用**

## 決定の理由
本プロジェクトの最優先事項は「ドメインルールの整合性維持」と「堅牢性」です。  
Resultパターンはエラーを値として伝播し、呼び出し側にハンドリングを強制できるため、  
不正状態の伝播を防ぎ、システム全体の信頼性を最大化できます。

例外方式は制御フローが隠蔽されやすく、特にチェーン処理でエラーが見逃されやすいため不採用としました。  
記述量増加のトレードオフは、Java 21の機能（pattern matching / switch式）で大幅に緩和されると判断しました。

例外は「DB接続断」「メモリ不足」などのシステム異常（復旧不可能なもの）のみに限定し、  
ドメイン層のビジネスロジック制御には一切使用しません。

## 影響範囲・結果（Consequences）

### プラス面
- ドメイン層から try-catch が排除され、ロジックが直線的（Railway）になる
- CommonErrorCode / GachaErrorCode でエラー定義が一元管理される
- テストで `assert(result.isFailure())` のように直感的な検証が可能

### マイナス面・対策（重要）
**トランザクション制御の落とし穴**  
Result.failure を返しても例外が発生しないため、Springの@Transactionalが自動ロールバックしない。

**対策（実装ルール）**  
Service層（@Transactional境界）でResultを処理する際、必ず以下のように明示的にロールバックをトリガーします。

```java
if (result instanceof Result.Failure) {
    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    return result;
}