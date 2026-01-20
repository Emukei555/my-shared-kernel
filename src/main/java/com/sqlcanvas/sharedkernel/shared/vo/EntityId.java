package com.sqlcanvas.sharedkernel.shared.vo;

import java.util.UUID;

/**
 * エンティティの識別子を表す基底インターフェース。
 * <p>
 * アプリ側では以下のように実装する:
 * {@code public record UserId(UUID value) implements EntityId<UUID> {}}
 * </p>
 */
public interface EntityId<T> extends ValueObject {

    T value();

    /**
     * 文字列形式の値を返します。
     */
    default String asString() {
        return value().toString();
    }
}