package org.clean;


import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;

// 用户上下文
public class UserContextTLL {
    private static final TransmittableThreadLocal<Map<String, String>> userContext = new TransmittableThreadLocal<>();
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
