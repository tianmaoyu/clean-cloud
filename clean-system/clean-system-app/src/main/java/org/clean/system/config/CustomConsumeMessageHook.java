package org.clean.system.config;

import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;

public class CustomConsumeMessageHook implements ConsumeMessageHook {
    @Override
    public String hookName() {
        return "CustomConsumeMessageHook";
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        // your logic
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        // your logic
    }
}