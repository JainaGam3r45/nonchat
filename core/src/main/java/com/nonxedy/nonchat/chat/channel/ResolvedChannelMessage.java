package com.nonxedy.nonchat.chat.channel;

import com.nonxedy.nonchat.api.Channel;

public record ResolvedChannelMessage(Channel channel, String message, boolean updatePlayerChannel) {}
