package com.sqlcanvas.sharedkernel.shared.vo;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class EntityIdTest {

    // テスト用のダミー実装
    record UserId(UUID value) implements EntityId<UUID> {}

    @Test
    void default_method_behavior() {
        UUID uuid = UUID.randomUUID();
        UserId userId = new UserId(uuid);

        // デフォルトメソッド asString() のテスト
        assertThat(userId.asString()).isEqualTo(uuid.toString());

        // value() のテスト
        assertThat(userId.value()).isEqualTo(uuid);
    }
}