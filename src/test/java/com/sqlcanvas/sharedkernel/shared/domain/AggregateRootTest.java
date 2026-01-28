package com.sqlcanvas.sharedkernel.shared.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class AggregateRootTest {

    static class TestAggregate extends AggregateRoot<String> {
        protected TestAggregate(String id) {
            super(id);
        }

        public void doSomething() {
            registerEvent("Something happened");
        }
    }

    @Test
    void aggregate_root_lifecycle() {
        String expectedId = "id-123";
        TestAggregate agg = new TestAggregate(expectedId);

        // ★ここを追加：IDが正しく保持されているか検証（これでコンストラクタがカバーされます）
        assertThat(agg.getId()).isEqualTo(expectedId);

        // イベント登録の検証
        agg.doSomething();
        List<Object> events = agg.pullDomainEvents();
        assertThat(events).hasSize(1).contains("Something happened");

        // クリアの検証
        assertThat(agg.pullDomainEvents()).isEmpty();
    }
}