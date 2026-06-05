package com.nonxedy.nonchat;

import com.nonxedy.nonchat.api.IPlatformAdapter;
import java.util.Comparator;
import java.util.ServiceLoader;
import org.bukkit.Bukkit;

public final class VersionDetector {
    private VersionDetector() {
    }

    public static IPlatformAdapter detect() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        ClassLoader cl = IPlatformAdapter.class.getClassLoader();

        return ServiceLoader.load(IPlatformAdapter.class, cl)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(adapter -> adapter.supports(bukkitVersion))
            .max(Comparator.comparingInt(
                adapter -> adapter.getSupportedVersion().split("\\.").length
            ))
            .orElseThrow(() -> new IllegalStateException(
                "Unsupported server version: " + bukkitVersion
            ));
    }
}