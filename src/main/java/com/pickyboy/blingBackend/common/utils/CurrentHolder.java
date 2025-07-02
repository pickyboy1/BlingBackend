package com.pickyboy.blingBackend.common.utils;


import com.pickyboy.blingBackend.common.context.UserContext;

/**
 * 当前用户线程本地存储工具类
 *
 * @author pickyboy
 */
public class CurrentHolder {

    private static final ThreadLocal<UserContext> CURRENT_USER_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前用户上下文
     *
     * @param userContext 用户上下文信息
     */
    public static void setCurrentUser(UserContext userContext) {
        CURRENT_USER_LOCAL.set(userContext);
    }

    /**
     * 获取当前用户上下文
     *
     * @return 用户上下文信息
     */
    public static UserContext getCurrentUser() {
        return CURRENT_USER_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        UserContext userContext = getCurrentUser();
        return userContext != null ? userContext.getUserId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        UserContext userContext = getCurrentUser();
        return userContext != null ? userContext.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     *
     * @return 用户角色
     */
//    public static String getCurrentUserRole() {
//        UserContext userContext = getCurrentUser();
//        return userContext != null ? userContext.getRole() : null;
//    }

    /**
     * 清除当前用户上下文
     */
    public static void remove() {
        CURRENT_USER_LOCAL.remove();
    }

    /**
     * 检查当前是否有用户登录
     *
     * @return 是否有用户登录
     */
    public static boolean hasCurrentUser() {
        return getCurrentUser() != null;
    }

    // 兼容性方法：保持与原有代码的兼容性
    /**
     * @deprecated 使用 getCurrentUserId() 替代
     */
    @Deprecated
    public static Integer getCurrentId() {
        Long userId = getCurrentUserId();
        return userId != null ? userId.intValue() : null;
    }

    /**
     * @deprecated 使用 setCurrentUser(UserContext) 替代
     */
    @Deprecated
    public static void setCurrentId(Integer employeeId) {
        if (employeeId != null) {
            UserContext userContext = new UserContext();
            userContext.setUserId(employeeId.longValue());
            setCurrentUser(userContext);
        }
    }
}