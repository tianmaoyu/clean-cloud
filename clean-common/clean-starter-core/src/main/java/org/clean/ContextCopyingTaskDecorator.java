package org.clean;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class ContextCopyingTaskDecorator implements TaskDecorator {
    
    @Override
    public Runnable decorate(Runnable runnable) {
        // 1. 捕获提交任务时的上下文快照
        Map<String, String> contextSnapshot = UserContext.getUser();
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();

        return () -> {
            // 2. 保存线程当前已有的上下文（可能是上一个任务留下的）
            Map<String, String> originalContext = UserContext.getUser();
            Map<String, String> originalMdc = MDC.getCopyOfContextMap();

            try {
                // 3. 设置捕获的上下文
                if (contextSnapshot != null && !contextSnapshot.isEmpty()) {
                    UserContext.setUser(contextSnapshot);
                }
                if (mdcSnapshot != null && !mdcSnapshot.isEmpty()){
                    MDC.setContextMap(mdcSnapshot);
                }

                // 4. 执行任务
                runnable.run();
                
            } catch (Exception e) {
                // 可选：记录日志或处理异常
                throw e;
            } finally {
                // 5. 恢复线程原有上下文（关键！）
                if (originalContext != null && !originalContext.isEmpty()) {
                    UserContext.setUser(originalContext);
                } else {
                    UserContext.clear();
                }

                if (originalMdc != null && !originalMdc.isEmpty()){
                    MDC.setContextMap(originalMdc);
                }else {
                    MDC.clear();
                }
            }
        };
    }
}