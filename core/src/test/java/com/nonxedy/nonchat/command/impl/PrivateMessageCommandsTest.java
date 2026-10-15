package com.nonxedy.nonchat.command.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.nonxedy.nonchat.Nonchat;
import com.nonxedy.nonchat.config.PluginConfig;
import com.nonxedy.nonchat.config.PluginMessages;
import com.nonxedy.nonchat.core.MessageManager;
import com.nonxedy.nonchat.service.ConfigService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

class PrivateMessageCommandsTest {

    private final Nonchat plugin = mock(Nonchat.class);
    private final PluginConfig config = mock(PluginConfig.class);
    private final PluginMessages messages = mock(PluginMessages.class);
    private final MessageManager manager = mock(MessageManager.class);
    private final Command command = mock(Command.class);
    private final Player alice = mock(Player.class);
    private final Player bob = mock(Player.class);
    private MockedStatic<Bukkit> bukkit;

    private MessageCommand msg;
    private ReplyCommand reply;

    @BeforeEach
    void setUp() {
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(() -> Bukkit.getPlayer("Bob")).thenReturn(bob);

        ConfigService configService = mock(ConfigService.class);
        when(configService.getConfig()).thenReturn(config);
        when(plugin.getConfigService()).thenReturn(configService);

        when(config.isPrivateChatEnabled()).thenReturn(true);
        when(messages.getString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(command.getName()).thenReturn("msg");
        when(alice.getName()).thenReturn("Alice");
        when(alice.hasPermission(anyString())).thenReturn(true);

        msg = new MessageCommand(plugin, config, messages, manager);
        reply = new ReplyCommand(plugin, manager, messages);
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    private static List<String> received(CommandSender recipient) {
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(recipient, atLeast(0)).sendMessage(captor.capture());
        return captor.getAllValues().stream()
                .map(component -> PlainTextComponentSerializer.plainText().serialize(component))
                .toList();
    }

    @Test
    void msgPassesJoinedMessageToManager() {
        assertTrue(msg.onCommand(alice, command, "msg", new String[]{"Bob", "hello", "there"}));

        verify(manager).sendPrivateMessage(alice, bob, "hello there", false);
    }

    @Test
    void msgIsRefusedWhenPrivateChatIsDisabled() {
        when(config.isPrivateChatEnabled()).thenReturn(false);

        msg.onCommand(alice, command, "msg", new String[]{"Bob", "hello"});

        assertEquals(List.of("command-disabled"), received(alice));
        verifyNoInteractions(manager);
    }

    @Test
    void msgReportsDisabledBeforeUsageErrors() {
        when(config.isPrivateChatEnabled()).thenReturn(false);

        msg.onCommand(alice, command, "msg", new String[0]);

        assertEquals(List.of("command-disabled"), received(alice));
    }

    @Test
    void msgRequiresPermission() {
        when(alice.hasPermission("nonchat.message")).thenReturn(false);

        msg.onCommand(alice, command, "msg", new String[]{"Bob", "hello"});

        assertEquals(List.of("no-permission"), received(alice));
        verifyNoInteractions(manager);
    }

    @Test
    void msgRequiresTargetAndMessage() {
        msg.onCommand(alice, command, "msg", new String[]{"Bob"});

        assertEquals(List.of("invalid-usage-message"), received(alice));
        verifyNoInteractions(manager);
    }

    @Test
    void msgToUnknownPlayerIsRefused() {
        msg.onCommand(alice, command, "msg", new String[]{"Nobody", "hello"});

        assertEquals(List.of("player-not-found"), received(alice));
        verifyNoInteractions(manager);
    }

    @Test
    void msgTabCompleteIsEmptyWhenDisabled() {
        when(config.isPrivateChatEnabled()).thenReturn(false);

        assertTrue(msg.onTabComplete(alice, command, "msg", new String[]{""}).isEmpty());
    }

    @Test
    void replyPassesJoinedMessageToManager() {
        reply.onCommand(alice, command, "r", new String[]{"hi", "back"});

        verify(manager).replyToLastMessage(alice, "hi back");
    }

    @Test
    void replyIsRefusedWhenPrivateChatIsDisabled() {
        when(config.isPrivateChatEnabled()).thenReturn(false);

        reply.onCommand(alice, command, "r", new String[]{"hi"});

        assertEquals(List.of("command-disabled"), received(alice));
        verifyNoInteractions(manager);
    }

    @Test
    void replyRequiresPermission() {
        when(alice.hasPermission("nonchat.reply")).thenReturn(false);

        reply.onCommand(alice, command, "r", new String[]{"hi"});

        assertEquals(List.of("no-permission"), received(alice));
        verify(manager, never()).replyToLastMessage(any(), anyString());
    }

    @Test
    void replyTabCompleteIsEmptyWhenDisabled() {
        when(config.isPrivateChatEnabled()).thenReturn(false);

        assertTrue(reply.onTabComplete(alice, command, "r", new String[]{""}).isEmpty());
    }
}
