package com.sqlcanvas.sharedkernel.shared.filter;

import com.sqlcanvas.sharedkernel.shared.util.RequestId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Function;

/**
 * 共通リクエストログフィルター
 * アプリケーション固有のユーザーID抽出ロジックは、コンストラクタで受け取る。
 */
public class SharedRequestLoggingFilter extends OncePerRequestFilter {

    private static final String KEY_REQUEST_ID = "requestId";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_URI = "uri";
    private static final String KEY_METHOD = "method";

    // ユーザーIDを取り出すための関数（アプリ側から注入される）
    private final Function<Authentication, String> userIdExtractor;

    // デフォルトコンストラクタ（ユーザーIDは単に名前を使う場合）
    public SharedRequestLoggingFilter() {
        this(Authentication::getName);
    }

    // カスタムロジックを注入するコンストラクタ
    public SharedRequestLoggingFilter(Function<Authentication, String> userIdExtractor) {
        this.userIdExtractor = userIdExtractor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. RequestId (Shared Kernelの機能を利用)
            RequestId requestId = RequestId.generate();
            MDC.put(KEY_REQUEST_ID, requestId.value().toString());

            // 2. URI & Method
            MDC.put(KEY_URI, request.getRequestURI());
            MDC.put(KEY_METHOD, request.getMethod());

            // 3. User ID (注入されたロジックで取得)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String userId = userIdExtractor.apply(auth);
                MDC.put(KEY_USER_ID, userId);
            } else {
                MDC.put(KEY_USER_ID, "anonymous");
            }

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}