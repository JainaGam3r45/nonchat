package com.nonxedy.nonchat.listener;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.nonxedy.nonchat.util.chat.MentionCompletionUtil;

public class MentionTabCompleteListener implements Listener {

    public void refreshAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayerCompletions(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            refreshPlayerCompletions(onlinePlayer);
        }

        refreshPlayerCompletions(joinedPlayer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String mention = "@" + event.getPlayer().getName();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            removeCustomChatCompletions(onlinePlayer, List.of(mention));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
        String lastToken = event.getLastToken();
        List<String> suggestions = MentionCompletionUtil.getMentionSuggestions(event.getPlayer(), lastToken);
        if (suggestions.isEmpty()) {
            return;
        }

        event.getTabCompletions().clear();
        event.getTabCompletions().addAll(suggestions);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
        if (event.isCommand() || !(event.getSender() instanceof Player player)) {
            return;
        }

        List<String> suggestions = MentionCompletionUtil.getMentionSuggestions(player, MentionCompletionUtil.extractLastToken(event.getBuffer()));
        if (suggestions.isEmpty()) {
            return;
        }

        event.setCompletions(suggestions);
        event.setHandled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent event) {
        if (event.isCommand() || !(event.getSender() instanceof Player player)) {
            return;
        }

        List<String> suggestions = MentionCompletionUtil.getMentionSuggestions(player, MentionCompletionUtil.extractLastToken(event.getBuffer()));
        if (suggestions.isEmpty()) {
            return;
        }

        event.setCompletions(suggestions);
    }

    private void refreshPlayerCompletions(Player player) {
        Collection<String> mentions = Bukkit.getOnlinePlayers().stream()
                .filter(player::canSee)
                .map(onlinePlayer -> "@" + onlinePlayer.getName())
                .collect(Collectors.toList());

        setCustomChatCompletions(player, mentions);
    }

    private void removeCustomChatCompletions(Player player, List<String> mentions) {
        invokePlayerMethod(player, "removeCustomChatCompletions", List.class, mentions);
    }

    private void setCustomChatCompletions(Player player, Collection<String> mentions) {
        invokePlayerMethod(player, "setCustomChatCompletions", Collection.class, mentions);
    }

    private void invokePlayerMethod(Player player, String methodName, Class<?> parameterType, Object argument) {
        try {
            Method method = player.getClass().getMethod(methodName, parameterType);
            method.invoke(player, argument);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
