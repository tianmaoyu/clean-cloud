package org.clean.system.config;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;

public class CustomSendMessageHook implements SendMessageHook {

    @Override
    public String hookName() {
        return "ContextSendMessageHook";
    }

    @Override
    public void sendMessageBefore(SendMessageContext sendMessageContext) {

    }

    @Override
    public void sendMessageAfter(SendMessageContext sendMessageContext) {

    }

}