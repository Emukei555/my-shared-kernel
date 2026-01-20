Shared Kernel (Domain Primitives & Utilities)
ドメイン駆動設計 (DDD) を実践するための共通ライブラリです。 例外駆動の制御フローを廃し、Railway Oriented Programming (Result型) を用いた堅牢なエラーハンドリングと、モダンなJava (Java 21+) の機能を活用するための基盤を提供します。

📦 Requirements
Java: 21+

Spring Boot: 3.x

Build Tool: Gradle (Kotlin DSL recommended) or Maven

🚀 Features
1. Result Pattern (Railway Oriented Programming)
ビジネスロジックにおける「失敗」を例外（Exception）ではなく、値（Value）として扱います。 Java 21の switch 式やパターンマッチングと組み合わせることで、型安全かつ可読性の高いコードを実現します。

Result<T>: 成功 (Success<T>) または 失敗 (Failure<T>) を表す直和型（Sealed Interface）。

Result.success(value): 成功時の値をラップ。

Result.failure(errorCode, message): エラー情報をラップ。

2. Standardized Error Codes
システム全体で統一されたエラーコード管理を提供します。

ErrorCode: エラーコード定義用インターフェース。

CommonErrorCode: 汎用的なエラー（バリデーションエラー、システムエラーなど）の定義。

3. Domain Value Objects
RequestId: UUID v7 ベースの識別子生成（時系列順にソート可能なUUID）。

ValueObject: マーカーインターフェース。

🛠 Usage
Installation (JitPack)
このリポジトリをライブラリとして利用するには、build.gradle.kts に以下を追加してください。

```Kotlin

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.Emukei555:my-shared-kernel:Tag") // Tagは最新のコミットハッシュまたはリリースタグ
}
1. Using Result<T>
```
例外を投げる代わりに Result を返します。

```Java

public Result<User> registerUser(String name, String email) {
    if (userRepository.existsByEmail(email)) {
        // 失敗: エラーコードを返す
        return Result.failure(CommonErrorCode.CONFLICT, "Email already exists");
    }
    
    User user = User.create(name, email);
    userRepository.save(user);
    
    // 成功: 値を返す
    return Result.success(user);
}
```
2. Handling Results (Java 21 Pattern Matching)
呼び出し元では、switch 式を使って成功と失敗を強制的にハンドリングします。

```Java

var result = userService.registerUser("Alice", "alice@example.com");

return switch (result) {
    case Result.Success<User>(var user) -> {
        log.info("Registered user: {}", user.getId());
        yield ResponseEntity.ok(new UserResponse(user));
    }
    case Result.Failure<User> failure -> {
        log.warn("Registration failed: {}", failure.message());
        // ErrorCode から HTTPステータスへの変換などもここで行えます
        yield ResponseEntity
                .status(failure.errorCode().getStatus())
                .body(new ErrorResponse(failure.message()));
    }
};
3. Handling Results (Functional Style)
```
関数型のアプローチでチェーンすることも可能です。

```Java

userService.registerUser("Alice", "alice@example.com")
    .map(user -> walletService.createWallet(user.getId())) // 成功時のみ実行
    .tap(wallet -> log.info("Wallet created"))
    .ifFailure(failure -> log.error("Error: {}", failure.message()));
```
4. Custom Error Codes
アプリケーション固有のエラーコードを定義する場合は、ErrorCode インターフェースを実装します。

```Java

@Getter
@RequiredArgsConstructor
public enum GachaErrorCode implements ErrorCode {
    OUT_OF_STOCK("GACHA-001", "在庫切れです", HttpStatus.CONFLICT),
    INSUFFICIENT_FUNDS("GACHA-002", "残高不足です", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;
}
```
📂 Project Structure
com.sqlcanvas.sharedkernel
├── shared
│   ├── error       # ErrorCode, CommonErrorCode
│   ├── result      # Result<T>, Success, Failure
│   └── util        # RequestId (UUID v7), Helper classes
└── ...
📜 License
MIT License

👤 Author
Emukei555
