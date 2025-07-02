package com.pickyboy.blingBackend.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickyboy.blingBackend.common.config.AuthProperties;
import com.pickyboy.blingBackend.common.context.UserContext;
import com.pickyboy.blingBackend.common.exception.JwtException;
import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.common.utils.JwtUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT认证过滤器
 * 拦截请求，验证Token，并将用户信息存储到线程本地空间
 *
 * @author pickyboy
 */
@Slf4j
@WebFilter(filterName = "jwtAuthenticationFilter", urlPatterns = "/*")
@Component
public class JwtAuthenticationFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthProperties authProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        log.debug("JWT过滤器处理请求: {} {}", method, requestURI);

        try {
            // 1. 直接检查原始路径是否为排除路径
            if (isExcludedPath(requestURI)) {
                log.debug("路径 {} 在排除列表中，跳过认证", requestURI);
                chain.doFilter(request, response);
                return;
            }

            // 2. 提取并验证Token
            String token = jwtUtil.extractTokenFromRequest(httpRequest);
            if (!StringUtils.hasText(token)) {
                log.warn("请求 {} 缺少Token", requestURI);
                writeErrorResponse(httpResponse, 401, "请先登录");
                return;
            }

            // 3. 解析Token获取用户信息
            UserContext userContext = jwtUtil.getUserContext(token);
            log.debug("Token验证成功，用户: {}, 角色: {}", userContext.getUsername());

            // 4. 检查Token是否需要刷新
            String refreshedToken = jwtUtil.refreshTokenIfNeeded(token);
            if (!token.equals(refreshedToken)) {
                log.info("Token已刷新，用户: {}", userContext.getUsername());
                httpResponse.setHeader("New-Token", refreshedToken);
            }

            // 5. 将用户信息存储到线程本地空间
            CurrentHolder.setCurrentUser(userContext);

            // 6. 继续处理请求
            chain.doFilter(request, response);

        } catch (JwtException.TokenExpiredException e) {
            log.warn("Token已过期: {}", e.getMessage());
            writeErrorResponse(httpResponse, 401, "登录已过期，请重新登录");
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            writeErrorResponse(httpResponse, 401, "认证失败，请重新登录");
        } catch (Exception e) {
            log.error("JWT过滤器处理异常", e);
            writeErrorResponse(httpResponse, 500, "服务器内部错误");
        } finally {
            // 7. 清理线程本地存储
            CurrentHolder.remove();
        }
    }

    /**
     * 检查是否为排除路径
     *
     * @param path 请求路径
     * @return 是否为排除路径
     */
    private boolean isExcludedPath(String path) {
        List<String> excludePaths = authProperties.getExcludePaths();
        if (excludePaths == null || excludePaths.isEmpty()) {
            return false;
        }

        for (String excludePath : excludePaths) {
            if (pathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 写入错误响应
     *
     * @param response 响应对象
     * @param status   状态码
     * @param message  错误消息
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Result<Object> errorResult = Result.error(message);
        String jsonResponse = objectMapper.writeValueAsString(errorResult);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("JWT认证过滤器初始化");
        log.info("排除路径: {}", authProperties.getExcludePaths());
    }

    @Override
    public void destroy() {
        log.info("JWT认证过滤器销毁");
    }
}