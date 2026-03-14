package org.clean;


import java.util.Map;

// 用户上下文
public class UserContext {
    private static final ThreadLocal<Map<String, String>> userContext = new ThreadLocal<>();
    public static void setUser(Map<String, String> user) {
        userContext.set(user);
    }
    public static Map<String, String> getUser() {
        return userContext.get();
    }
    public static void clear() {
        userContext.remove();
    }
}
