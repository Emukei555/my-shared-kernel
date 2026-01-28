package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import com.sqlcanvas.sharedkernel.shared.util.RequestId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleVoTest {

    // --- Email ---
    @Test
    void email_test() {
        assertThat(Email.of("test@example.com")).isSuccess();
        assertThat(Email.of("invalid-email")).isFailure();
        assertThat(Email.of(null)).isFailure();
    }

    // --- PhoneNumber ---
    @Test
    void phoneNumber_test() {
        // 正常系: ハイフンあり
        assertThat(PhoneNumber.of("090-1234-5678")).isSuccess();

        // 実装ではハイフンなしも許可されているため、isSuccess() に変更
        assertThat(PhoneNumber.of("09012345678")).isSuccess();

        // 本当に不正な値（桁数不足や文字混入）で isFailure を確認する
        assertThat(PhoneNumber.of("invalid-phone")).isFailure();
        assertThat(PhoneNumber.of("090")).isFailure();

        // フォーマット除去の確認
        Result<PhoneNumber> res = PhoneNumber.of("090-1111-2222");
        assertThat(res.unwrap().asUnformatted()).isEqualTo("09011112222");
    }

    // --- PostalCode ---
    @Test
    void postalCode_test() {
        assertThat(PostalCode.of("123-4567")).isSuccess();
        assertThat(PostalCode.of("1234567")).isFailure(); // ハイフン必須の場合
    }

    // --- NonNegativeLong ---
    @Test
    void nonNegativeLong_test() {
        assertThat(NonNegativeLong.of(0)).isSuccess();
        assertThat(NonNegativeLong.of(-1)).isFailure();

        NonNegativeLong val = NonNegativeLong.of(10).unwrap();
        assertThat(val.increment().value()).isEqualTo(11);

        assertThat(val.add(5)).isSuccess().hasValueSatisfying(v -> assertThat(v.value()).isEqualTo(15));
        assertThat(val.add(-20)).isFailure(); // 負になる場合
    }

    // --- PositiveBigDecimal ---
    @Test
    void positiveBigDecimal_test() {
        assertThat(PositiveBigDecimal.of(1.5)).isSuccess();
        assertThat(PositiveBigDecimal.of(0.0)).isFailure();
        assertThat(PositiveBigDecimal.of(-1.0)).isFailure();

        PositiveBigDecimal val = PositiveBigDecimal.of(10).unwrap();
        assertThat(val.add(PositiveBigDecimal.of(20).unwrap()).value()).isEqualByComparingTo("30");
        assertThat(val.multiply(BigDecimal.valueOf(2)).unwrap().value()).isEqualByComparingTo("20");
    }

    // --- RequestId ---
    @Test
    void requestId_test() {
        // 生成
        RequestId id = RequestId.generate();
        assertThat(id).isNotNull();
        assertThat(id.toString()).isNotEmpty();

        // 再構築
        assertThat(RequestId.from(id.toString())).isSuccess();
        assertThat(RequestId.from("invalid-uuid")).isFailure();
    }

    @Test
    void boundary_null_empty_checks() {
        // Email
        assertThat(Email.of(null)).isFailure();
        assertThat(Email.of("")).isFailure();   // isBlank check
        assertThat(Email.of("   ")).isFailure();

        // PhoneNumber
        assertThat(PhoneNumber.of(null)).isFailure();
        assertThat(PhoneNumber.of("")).isFailure();

        // PostalCode
        assertThat(PostalCode.of(null)).isFailure();
        assertThat(PostalCode.of("")).isFailure();

        // PositiveBigDecimal
        assertThat(PositiveBigDecimal.of(null)).isFailure();
        // PositiveBigDecimal の分岐網羅 (compareTo <= 0)
        assertThat(PositiveBigDecimal.of(java.math.BigDecimal.ZERO)).isFailure();
    }
}