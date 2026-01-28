package com.sqlcanvas.sharedkernel.shared.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoConstructorTest {

    @Test
    void money_constructor_guard() {
        // コンストラクタを直接呼んで例外が出ることを確認
        assertThatThrownBy(() -> new Money(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void positiveInt_constructor_guard() {
        assertThatThrownBy(() -> new PositiveInt(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PositiveInt(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nonNegativeLong_constructor_guard() {
        assertThatThrownBy(() -> new NonNegativeLong(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void positiveBigDecimal_constructor_guard() {
        assertThatThrownBy(() -> new PositiveBigDecimal(null))
                .isInstanceOf(NullPointerException.class); // requireNonNull

        assertThatThrownBy(() -> new PositiveBigDecimal(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new PositiveBigDecimal(BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void email_constructor_guard() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void phoneNumber_constructor_guard() {
        assertThatThrownBy(() -> new PhoneNumber(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void postalCode_constructor_guard() {
        assertThatThrownBy(() -> new PostalCode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_check() {
        // equals / hashCode のカバレッジを稼ぐ
        Money m1 = new Money(100);
        Money m2 = new Money(100);
        Money m3 = new Money(200);

        assertThat(m1).isEqualTo(m2);
        assertThat(m1).hasSameHashCodeAs(m2);
        assertThat(m1).isNotEqualTo(m3);
        assertThat(m1).isNotEqualTo(null);
        assertThat(m1).isNotEqualTo(new Object());
    }
}