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
将来的には、Money, PositiveInt, Identifier などの値オブジェクトを追加予定です。

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
ドメイン特有の値を型安全に扱います（現在は最小限、拡張予定）。

- **RequestId**: UUID v7 ベースの識別子生成（時系列順にソート可能なUUID）。

## インストール

### JitPack経由
このリポジトリをライブラリとして利用するには、build.gradle.kts に以下を追加してください。

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.Emukei555:my-shared-kernel:0.1.0-SNAPSHOT") // 最新リリースタグやコミットハッシュを指定
}
```

Mavenの場合:xml
```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Emukei555</groupId>
        <artifactId>my-shared-kernel</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

注意: ローカル開発時は、gradle publishToMavenLocal でインストールして使用してください。使用方法Resultパターン基本的な使用例（成功/失敗のハンドリング）
メソッドからResultを返すことで、エラーを型として明示的に扱います。java
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

パターンマッチングによる結果処理（Java 21+）
switch式を使って、コンパイラの支援を受けながら結果を処理します。java
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

関数型スタイル（チェーン処理）
flatMap/mapを使って、Railway Oriented Programmingを実現します。java
```java
userService.registerUser("Alice", "alice@example.com")
    .flatMap(user -> walletService.createWallet(user.getId())) // 成功時のみ実行（Wallet作成）
    .tap(wallet -> log.info("Wallet created: {}", wallet.getId())) // 成功時のログ
    .tapFailure(failure -> log.error("Error: {}", failure.message())) // 失敗時のログ
    .map(wallet -> new AccountResponse(user, wallet)); // 最終変換
```
例外との連携（unwrap）
トランザクション境界などで例外が必要な場合に使用します。java
```java
@Transactional
public User registerWithWallet(String name, String email) {
    return userService.registerUser(name, email)
            .flatMap(user -> walletService.createWallet(user.getId()))
            .unwrap(); // 失敗時はGachaExceptionをスロー（ロールバック誘発）
}
```

エラーコードの標準化アプリケーション固有のエラーコードを定義します。エラーコードの定義例
ErrorCodeインターフェースを実装してEnumを作成します。java
```java
import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    CONFLICT("COMMON-001", "リソースが既に存在します", HttpStatus.CONFLICT),
    INVALID_ARGUMENT("COMMON-002", "無効な引数です", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    CommonErrorCode(String code, String defaultMessage, HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }

    @Override
    public String getCode() { return code; }
    @Override
    public String getDefaultMessage() { return defaultMessage; }
    @Override
    public HttpStatus getStatus() { return status; }
}
```

使用例
Result.failureでエラーコードを指定します。java
```java
return Result.failure(CommonErrorCode.INVALID_ARGUMENT, "Email is invalid");
```

ドメイン値オブジェクトドメイン特有の値を型安全に扱います。RequestIdの使用例
時系列ソート可能なUUID v7を生成します。java

RequestId requestId = RequestId.generate(); // UUID v7生成
log.info("Request ID: {}", requestId.value()); // UUID文字列取得

内部実装: UUID v7（時系列順にソート可能、衝突耐性高）

プロジェクト構造

com.yourcompany.domain.shared
├── result/           # Result<T>, Success, Failure
├── error/            # ErrorCode, CommonErrorCode
├── value/            # ValueObject基底 (将来拡張)
├── id/               # Identifier基底, UUIDv7Id, RequestId
└── logging/          # MDC Utility (将来追加予定)

今後の予定:Money / PositiveInt などの値オブジェクト追加  
Logging Utility (MDC自動設定)  
Validation Helper (共通ガード節)


