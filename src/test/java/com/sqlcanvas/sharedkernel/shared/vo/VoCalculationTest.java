package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class VoCalculationTest {

    // --- PositiveBigDecimal ---

    @Test
    void positiveBigDecimal_operations() {
        PositiveBigDecimal val = PositiveBigDecimal.of(10.5).unwrap();

        // setScale
        PositiveBigDecimal scaled = val.setScale(0, RoundingMode.DOWN);
        assertThat(scaled.value()).isEqualByComparingTo("10");

        // multiply success
        assertThat(val.multiply(BigDecimal.valueOf(2)))
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v.value()).isEqualByComparingTo("21.0"));

        // multiply failure (0 or negative)
        assertThat(val.multiply(BigDecimal.ZERO))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);

        // double factory
        assertThat(PositiveBigDecimal.of(10.0)).isSuccess();
    }

    // --- NonNegativeLong ---

    @Test
    void nonNegativeLong_operations() {
        NonNegativeLong val = NonNegativeLong.of(10).unwrap();

        // Zero constant
        assertThat(NonNegativeLong.ZERO.value()).isEqualTo(0);

        // compareTo
        assertThat(val.compareTo(NonNegativeLong.of(20).unwrap())).isNegative();
        assertThat(val.compareTo(NonNegativeLong.of(5).unwrap())).isPositive();
    }

    // --- PositiveInt ---

    @Test
    void positiveInt_operations() {
        PositiveInt val = PositiveInt.of(10).unwrap();

        // add
        PositiveInt added = val.add(PositiveInt.of(5).unwrap());
        assertThat(added.value()).isEqualTo(15);

        // multiply success
        assertThat(val.multiply(2))
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v.value()).isEqualTo(20));

        // multiply fail
        assertThat(val.multiply(0))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);

        // compareTo
        assertThat(val.compareTo(PositiveInt.of(10).unwrap())).isEqualTo(0);
    }
}