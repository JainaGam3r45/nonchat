package com.nonxedy.nonchat.adapter.v1_19_R3;

import com.nonxedy.nonchat.api.IMessageHandler;
import org.bukkit.event.Listener;

public class PlatformAdapter119 extends AbstractBubblePlatformAdapter {
    public PlatformAdapter119() {
        super("1.19.4");
    }

    @Override
    protected Listener createChatListener(IMessageHandler handler) {
        return new ChatListener119(handler);
    }
}
