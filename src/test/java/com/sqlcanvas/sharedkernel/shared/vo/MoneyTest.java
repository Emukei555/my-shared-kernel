package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat; // 通常のAssertJも使う場合

class MoneyTest {

    // --- Factory ---

    @Test
    void create_success() {
        assertThat(Money.of(1000))
                .isSuccess()
                .hasValueSatisfying(m -> assertThat(m.amount()).isEqualTo(1000));
    }

    @Test
    void create_zero() {
        // 0円はOK
        assertThat(Money.of(0))
                .isSuccess()
                .hasValueSatisfying(m -> assertThat(m.isZero()).isTrue());
    }

    @Test
    void create_fail_negative() {
        // マイナスはNG
        assertThat(Money.of(-1))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }

    // --- Calculation ---

    @Test
    void add_success() {
        var m1 = new Money(100);
        var m2 = new Money(200);

        assertThat(m1.add(m2))
                .isSuccess()
                .hasValueSatisfying(total -> assertThat(total.amount()).isEqualTo(300));
    }

    @Test
    void subtract_success() {
        var m1 = new Money(300);
        var m2 = new Money(100);

        assertThat(m1.subtract(m2))
                .isSuccess()
                .hasValueSatisfying(remain -> assertThat(remain.amount()).isEqualTo(200));
    }

    @Test
    void subtract_fail_insufficient() {
        var m1 = new Money(100);
        var m2 = new Money(200);

        // 残高不足
        assertThat(m1.subtract(m2))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }

    @Test
    void compare() {
        var m100 = new Money(100);
        var m200 = new Money(200);

        assertThat(m200.isGreaterThan(m100)).isTrue();
        assertThat(m100.isGreaterThan(m200)).isFalse();
        assertThat(m100.isGreaterThanOrEqual(m100)).isTrue();
    }

    @Test
    void boolean_checks_coverage() {
        Money zero = Money.zero();
        Money ten = new Money(10);
        Money twenty = new Money(20);

        // isZero
        assertThat(zero.isZero()).isTrue();
        assertThat(ten.isZero()).isFalse();

        // isGreaterThan
        assertThat(twenty.isGreaterThan(ten)).isTrue();
        assertThat(ten.isGreaterThan(twenty)).isFalse();
        assertThat(ten.isGreaterThan(ten)).isFalse(); // Equal case

        // isGreaterThanOrEqual
        assertThat(twenty.isGreaterThanOrEqual(ten)).isTrue();
        assertThat(ten.isGreaterThanOrEqual(ten)).isTrue(); // Equal case
        assertThat(ten.isGreaterThanOrEqual(twenty)).isFalse();
    }
}