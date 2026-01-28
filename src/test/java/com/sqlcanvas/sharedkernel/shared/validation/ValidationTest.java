package com.sqlcanvas.sharedkernel.shared.validation;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import org.junit.jupiter.api.Test;

import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    @Test
    void combine2_success() {
        Result<String> r1 = Result.success("Hello");
        Result<String> r2 = Result.success("World");

        Result<String> combined = Validation.combine(r1, r2, (s1, s2) -> s1 + " " + s2);

        assertThat(combined)
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v).isEqualTo("Hello World"));
    }

    @Test
    void combine2_failure_collects_errors() {
        Result<String> r1 = Result.failure(CommonErrorCode.INVALID_PARAMETER, "Error1");
        Result<String> r2 = Result.failure(CommonErrorCode.SYSTEM_ERROR, "Error2");

        Result<String> combined = Validation.combine(r1, r2, (s1, s2) -> s1 + s2);

        // 両方のエラーが含まれていることを確認
        assertThat(combined)
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER); // 代表コード

        // メッセージに両方のエラーが含まれているか
        assertThat(combined.unwrapFailure().message())
                .contains("Error1")
                .contains("Error2");
    }

    @Test
    void combine3_success() {
        Result<Integer> r1 = Result.success(1);
        Result<Integer> r2 = Result.success(2);
        Result<Integer> r3 = Result.success(3);

        Result<Integer> combined = Validation.combine(r1, r2, r3, (i1, i2, i3) -> i1 + i2 + i3);

        assertThat(combined)
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v).isEqualTo(6));
    }

    @Test
    void combine_mixed_failure_coverage() {
        Result<String> ok = Result.success("OK");
        Result<String> ng = Result.failure(CommonErrorCode.SYSTEM_ERROR, "NG");

        // 1つ目がNG, 2つ目がOK
        Result<String> r1 = Validation.combine(ng, ok, (a, b) -> a + b);
        assertThat(r1).isFailure();
        assertThat(r1.unwrapFailure().message()).contains("NG");

        // 1つ目がOK, 2つ目がNG
        Result<String> r2 = Validation.combine(ok, ng, (a, b) -> a + b);
        assertThat(r2).isFailure();
    }

    @Test
    void combine3_mixed_failures() {
        Result<Integer> ok = Result.success(1);
        Result<Integer> ng1 = Result.failure(CommonErrorCode.INVALID_PARAMETER, "NG1");
        Result<Integer> ng2 = Result.failure(CommonErrorCode.SYSTEM_ERROR, "NG2");

        // 1つ目だけNG
        Result<Integer> r1 = Validation.combine(ng1, ok, ok, (a, b, c) -> a + b + c);
        assertThat(r1).isFailure();
        assertThat(r1.unwrapFailure().message()).contains("NG1");

        // 2つ目だけNG
        Result<Integer> r2 = Validation.combine(ok, ng1, ok, (a, b, c) -> a + b + c);
        assertThat(r2).isFailure();

        // 3つ目だけNG
        Result<Integer> r3 = Validation.combine(ok, ok, ng1, (a, b, c) -> a + b + c);
        assertThat(r3).isFailure();

        // 全部NG
        Result<Integer> rAll = Validation.combine(ng1, ng2, ng1, (a, b, c) -> a + b + c);
        assertThat(rAll).isFailure();
        assertThat(rAll.unwrapFailure().message())
                .contains("NG1")
                .contains("NG2");
    }
}