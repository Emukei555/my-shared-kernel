package com.sqlcanvas.sharedkernel.shared.vo;

import java.io.Serializable;

/**
 * すべての値オブジェクト (Value Object) の基底インターフェース。
 * <p>
 * マーカーインターフェースとしての役割と、
 * JPAやシリアライズに必要な Serializable を強制する役割を持つ。
 * </p>
 */
public interface ValueObject extends Serializable {
    // 必要に応じて共通メソッドを定義（今回はマーカーとして利用）
}