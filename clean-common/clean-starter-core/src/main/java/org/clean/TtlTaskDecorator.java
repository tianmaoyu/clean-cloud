package org.clean;

import com.alibaba.ttl.TtlRunnable;
import org.springframework.core.task.TaskDecorator;

public class TtlTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        // 使用 TtlRunnable 包装，实现上下文传递
        return TtlRunnable.get(runnable);
    }
}