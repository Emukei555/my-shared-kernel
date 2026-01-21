# Shared Kernel (Domain Primitives & Utilities)

![Java CI](https://github.com/Emukei555/my-shared-kernel/actions/workflows/ci.yml/badge.svg)

ドメイン駆動設計 (DDD) を実践するための共通ライブラリです。  
例外駆動の制御フローを廃し、Railway Oriented Programming (Result型) を用いた堅牢なエラーハンドリングと、モダンなJava (Java 21+) の機能を活用するための基盤を提供します。  
このライブラリは、ドメインの不変条件を型安全に守り、再利用性を高めることを目的としています。

[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📖 目次
- [プロジェクト概要](#プロジェクト概要)
- [要件](#要件)
- [機能](#機能)
- [インストール](#インストール)
- [使用方法](#使用方法)
  - [Resultパターン](#resultパターン)
  - [エラーコードの標準化](#エラーコードの標準化)
  - [ドメイン値オブジェクト](#ドメイン値オブジェクト)
- [プロジェクト構造](#プロジェクト構造)
- [ライセンス](#ライセンス)
- [著者](#著者)
- [コントリビューション](#コントリビューション)

## プロジェクト概要

このリポジトリは、DDDを実践する際に頻出する基盤要素（Result型、エラーコード、値オブジェクトなど）を共通化するためのものです。  
主な目的は以下の通り：

- **型安全なエラー処理**: 例外を投げずにエラーを値として扱い、コンパイラの支援を受けてハンドリング漏れを防ぐ。
- **再利用性の向上**: 各プロジェクトで繰り返し実装される不変条件やID生成をライブラリ化し、生産性を高める。
- **モダンJavaの活用**: record, sealed interface, pattern matching などの機能を基盤に組み込み、堅牢で可読性の高いコードを実現。

このライブラリは、個人プロジェクトや小規模チームでのDDD実践に最適です。

## 要件
- **Java**: 21+ (record, sealed interface, pattern matching を活用するため)
- **Spring Boot**: 3.x+ (オプション: @Transactionalとの連携を考慮)
- **ビルドツール**: Gradle (Kotlin DSL推奨) または Maven

## 機能

### 1. Result Pattern (Railway Oriented Programming)
ビジネスロジックにおける「失敗」を例外ではなく値として扱います。  
Java 21の switch 式やパターンマッチングと組み合わせることで、型安全かつ可読性の高いコードを実現します。

- **Result<T>**: 成功 (Success<T>) または 失敗 (Failure<T>) を表す sealed interface。
- **主なメソッド**:
  - `map`, `flatMap`: 成功時のみ変換/チェーン。
  - `tap`, `tapFailure`: 成功/失敗時のみ副作用（例: ログ出力）を実行。
  - `unwrap`: 成功時値を返す（失敗時は例外スロー）。

### 2. Standardized Error Codes
システム全体で統一されたエラーコード管理を提供します。

- **ErrorCode**: エラーコード定義用インターフェース。
- **CommonErrorCode**: 汎用的なエラー（バリデーションエラー、システムエラーなど）の定義。

### 3. Domain Value Objects
ドメイン特有の値を型安全に扱います。

- **Numeric VOs**:
    - `Money`: 金額計算ロジック（加算・減算・負数チェック）を内包。
    - `PositiveInt`: 正の整数 (1以上) を保証。
    - `NonNegativeLong`: 非負の整数 (0以上) を保証。
    - `PositiveBigDecimal`: 正の小数を保証。
- **String VOs**:
    - `Email`: メールアドレス形式のチェック。
    - `PhoneNumber`: 電話番号形式のチェック。
    - `PostalCode`: 郵便番号形式のチェック。
- **Identifier**:
    - `RequestId`: UUID v7 ベースの識別子生成（時系列順にソート可能なUUID）。

## インストール

### JitPack経由
このリポジトリをライブラリとして利用するには、build.gradle に以下を追加してください。

```groovy
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation 'com.github.Emukei555:my-shared-kernel:v0.1.3'
}
```

注意: GitHub Packagesを利用する場合は、別途認証設定が必要です。

## 使用方法

### Resultパターン

**基本的な使用例（成功/失敗のハンドリング）**
メソッドからResultを返すことで、エラーを型として明示的に扱います。

```java
// ユーザー登録の例
public Result<User> registerUser(String name, String email) {
    if (userRepository.existsByEmail(email)) {
        // 失敗時: エラーコードを返す
        return Result.failure(CommonErrorCode.CONFLICT, "Email already exists");
    }
    
    User user = User.create(name, email);
    userRepository.save(user);
    
    // 成功時: 値を返す
    return Result.success(user);
}
```

**パターンマッチングによる結果処理（Java 21+）**

```java
Result<User> result = userService.registerUser("Alice", "alice@example.com");

return switch (result) {
    case Result.Success<User>(var user) -> {
        log.info("Registered user: {}", user.getId());
        yield ResponseEntity.ok(new UserResponse(user));
    }
    case Result.Failure<User> failure -> {
        log.warn("Registration failed: {}", failure.message());
        // ErrorCodeからHTTPステータスへの変換
        yield ResponseEntity
                .status(failure.errorCode().getStatus())
                .body(new ErrorResponse(failure.message()));
    }
};
```

### ドメイン値オブジェクト

**Moneyの使用例**

```java
Money price = new Money(1000);
Money tax = new Money(100);

// 計算結果もMoney型で返る (Resultでラップされる場合あり)
Result<Money> total = price.add(tax); 
```

**RequestIdの使用例**

```java
RequestId requestId = RequestId.generate(); // UUID v7生成
log.info("Request ID: {}", requestId.value()); 
```

## プロジェクト構造

```text
com.yourcompany.domain.shared
├── result/           # Result<T>, Success, Failure
├── error/            # ErrorCode, CommonErrorCode
├── vo/               # ValueObject (Money, PositiveInt, Email, etc.)
├── util/             # RequestId (UUIDv7)
└── docs/             # ADR (Architectural Decision Records)
```

## 今後の予定
- Logging Utility (MDC自動設定)
- Validation Helper (共通ガード節)

