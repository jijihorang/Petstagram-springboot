package com.petstagram.config;

import com.petstagram.service.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        String token = null;

        if (query != null && query.contains("token=")) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                if (param.startsWith("token=")) {
                    token = param.split("token=")[1];
                    break;
                }
            }
        }

        log.info("Extracted Token: {}", token);
        if (StringUtils.hasText(token)) {
            // 토큰에서 사용자 정보 추출
            String username = jwtUtils.extractUsername(token);
            log.info("Extracted Username: {}", username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            log.info("Loaded UserDetails: {}", userDetails);

            // Spring Security Context 에 사용자 정보 설정
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication set in SecurityContext");

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // Handshake 후처리 로직
    }
}