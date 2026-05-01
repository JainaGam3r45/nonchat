package com.nonxedy.nonchat.adapter.v1_18_R2;

import com.nonxedy.nonchat.api.IMessageHandler;
import com.nonxedy.nonchat.api.ServiceAdapter;
import org.bukkit.event.Listener;

public final class PlatformAdapter118 extends ServiceAdapter {
    public PlatformAdapter118() {
        super("1.18");
    }

    @Override
    protected Listener createChatListener(IMessageHandler handler) {
        return new ChatListener118(handler);
    }
}
