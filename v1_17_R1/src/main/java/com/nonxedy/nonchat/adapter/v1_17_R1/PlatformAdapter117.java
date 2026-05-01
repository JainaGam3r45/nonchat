package com.nonxedy.nonchat.adapter.v1_17_R1;

import com.nonxedy.nonchat.api.IMessageHandler;
import com.nonxedy.nonchat.api.ServiceAdapter;
import org.bukkit.event.Listener;

public final class PlatformAdapter117 extends ServiceAdapter {
    public PlatformAdapter117() {
        super("1.17");
    }

    @Override
    protected Listener createChatListener(IMessageHandler handler) {
        return new ChatListener117(handler);
    }
}
