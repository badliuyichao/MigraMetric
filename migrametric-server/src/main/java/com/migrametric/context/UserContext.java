package com.migrametric.context;

import com.migrametric.entity.user.User;
import lombok.Data;

/**
 * 用户上下文工具类
 * 使用ThreadLocal存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String name;
        private String role;
    }

    public static void setCurrentUser(UserInfo user) {
        USER_HOLDER.set(user);
    }

    public static void setCurrentUser(User user) {
        if (user != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setName(user.getName());
            userInfo.setRole(user.getRole());
            USER_HOLDER.set(userInfo);
        }
    }

    public static UserInfo getCurrentUser() {
        return USER_HOLDER.get();
    }

    public static Long getCurrentUserId() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUsername() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    public static String getCurrentName() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getName() : null;
    }

    public static String getCurrentRole() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
}