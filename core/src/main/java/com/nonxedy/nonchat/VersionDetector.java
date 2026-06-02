package com.nonxedy.nonchat;

import com.nonxedy.nonchat.api.IPlatformAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.bukkit.Bukkit;

public final class VersionDetector {
    private VersionDetector() {
    }

    public static IPlatformAdapter detect() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        ClassLoader cl = IPlatformAdapter.class.getClassLoader();
        List<IPlatformAdapter> adapters = loadAdapters(cl);

        IPlatformAdapter versionMatch = adapters.stream()
            .filter(adapter -> isVersionMatch(adapter, bukkitVersion))
            .filter(adapter -> adapter.supports(bukkitVersion))
            .max(Comparator.comparingInt(
                adapter -> adapter.getSupportedVersion().split("\\.").length
            ))
            .orElse(null);

        if (versionMatch != null) {
            return versionMatch;
        }

        return adapters.stream()
            .filter(adapter -> adapter.supports(bukkitVersion))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Unsupported server version: " + bukkitVersion
                    + ". Make sure the server is running Paper or Spigot 1.16+."
            ));
    }

    private static List<IPlatformAdapter> loadAdapters(ClassLoader classLoader) {
        List<IPlatformAdapter> adapters = new ArrayList<>();
        ServiceLoader<IPlatformAdapter> loader = ServiceLoader.load(IPlatformAdapter.class, classLoader);
        java.util.Iterator<IPlatformAdapter> iterator = loader.iterator();

        while (true) {
            try {
                if (!iterator.hasNext()) {
                    break;
                }
                adapters.add(iterator.next());
            } catch (ServiceConfigurationError | LinkageError ignored) {
            }
        }

        return adapters;
    }

    private static boolean isVersionMatch(IPlatformAdapter adapter, String bukkitVersion) {
        if (bukkitVersion == null) {
            return false;
        }

        String serverVersion = bukkitVersion.split("-", 2)[0];
        String supportedVersion = adapter.getSupportedVersion();

        return serverVersion.equals(supportedVersion)
            || serverVersion.startsWith(supportedVersion + ".");
    }
}
