package com.nonxedy.nonchat.util.chat.formatting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.bukkit.entity.Player;

import com.nonxedy.nonchat.config.PluginConfig;
import com.nonxedy.nonchat.util.core.colors.ColorUtil;
import com.nonxedy.nonchat.util.integration.external.IntegrationUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class PrivateMessageUtil {

    public static Component createSenderMessage(
            PluginConfig config, Player sender, Player target, String message) {

        String format = config.getPrivateChatSenderFormat();
        String senderName = sender != null ? sender.getName() : "Console";

        String formattedMessage = format
                .replace("{sender}", senderName)
                .replace("{receiver}", target.getName())
                .replace("{message}", message);

        if (sender != null) {
            formattedMessage = IntegrationUtil.processPlaceholders(sender, formattedMessage);
        }

        Component baseComponent = ColorUtil.parseComponent(formattedMessage);

        if (config.isPrivateChatSenderHoverEnabled()) {
            baseComponent = addSenderInteractivity(config, baseComponent, sender, target);
        }

        return baseComponent;
    }

    public static Component createReceiverMessage(
            PluginConfig config, Player sender, Player target, String message) {

        String format = config.getPrivateChatReceiverFormat();
        String senderName = sender != null ? sender.getName() : "Console";

        String formattedMessage = format
                .replace("{sender}", senderName)
                .replace("{receiver}", target.getName())
                .replace("{message}", message);

        formattedMessage = IntegrationUtil.processPlaceholders(target, formattedMessage);

        Component baseComponent = ColorUtil.parseComponent(formattedMessage);

        if (config.isPrivateChatReceiverHoverEnabled()) {
            baseComponent = addReceiverInteractivity(config, baseComponent, sender, target);
        }

        return baseComponent;
    }

    private static Component addSenderInteractivity(
            PluginConfig config, Component component, Player sender, Player target) {

        List<String> hoverLines = config.getPrivateChatSenderHover();
        if (hoverLines.isEmpty()) return component;

        Component hoverComponent = buildHoverText(hoverLines, sender, target);
        Component resultComponent = component.hoverEvent(HoverEvent.showText(hoverComponent));

        if (config.isPrivateChatClickActionsEnabled()) {
            String clickCommand = config.getPrivateChatReplyCommand()
                    .replace("{sender}", sender != null ? sender.getName() : "Console")
                    .replace("{receiver}", target.getName());
            resultComponent = resultComponent.clickEvent(ClickEvent.suggestCommand(clickCommand));
        }

        return resultComponent;
    }

    private static Component addReceiverInteractivity(
            PluginConfig config, Component component, Player sender, Player target) {

        List<String> hoverLines = config.getPrivateChatReceiverHover();
        if (hoverLines.isEmpty()) return component;

        Component hoverComponent = buildHoverText(hoverLines, sender, target);
        Component resultComponent = component.hoverEvent(HoverEvent.showText(hoverComponent));

        if (config.isPrivateChatClickActionsEnabled()) {
            String clickCommand = config.getPrivateChatReplyCommand()
                    .replace("{sender}", sender != null ? sender.getName() : "Console")
                    .replace("{receiver}", target.getName());
            resultComponent = resultComponent.clickEvent(ClickEvent.suggestCommand(clickCommand));
        }

        return resultComponent;
    }

    private static Component buildHoverText(
            List<String> hoverLines, Player sender, Player target) {

        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        List<Component> components = new java.util.ArrayList<>();

        for (int i = 0; i < hoverLines.size(); i++) {
            String line = hoverLines.get(i);
            String senderName = sender != null ? sender.getName() : "Console";

            line = line
                    .replace("{sender}", senderName)
                    .replace("{receiver}", target.getName())
                    .replace("{time}", currentTime);

            if (sender != null) {
                line = IntegrationUtil.processPlaceholders(sender, line);
            } else {
                line = IntegrationUtil.processPlaceholders(target, line);
            }

            components.add(ColorUtil.parseComponent(line));

            if (i < hoverLines.size() - 1) {
                components.add(Component.newline());
            }
        }

        return Component.join(
            JoinConfiguration.noSeparators(),
            components
        );
    }
}