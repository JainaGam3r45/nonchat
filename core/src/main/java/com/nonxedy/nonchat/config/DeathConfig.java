package com.nonxedy.nonchat.config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import com.nonxedy.nonchat.Nonchat;

/**
 * Independent configuration class for deaths.yml
 * Manages death message system settings separate from main config
 */
public class DeathConfig {
    private final File configFile;
    private final Logger logger;
    private final Nonchat plugin;
    private FileConfiguration config;

    /**
     * Creates a new DeathConfig instance
     * @param dataFolder Plugin data folder
     * @param logger Plugin logger
     * @param plugin Plugin instance for resource access
     */
    public DeathConfig(File dataFolder, Logger logger, Nonchat plugin) {
        this.configFile = new File(dataFolder, "deaths.yml");
        this.logger = logger;
        this.plugin = plugin;
    }

    /**
     * Loads the deaths.yml configuration file
     * Creates default file if it doesn't exist and updates with missing keys
     */
    public void load() {
        try {
            boolean fileExisted = configFile.exists();
            saveDefaultConfig();
            
            if (!fileExisted) {
                logger.info("Created default deaths.yml configuration file");
            }
            
            ConfigUpdater.updateFile(plugin, configFile, "deaths.yml", null);
            
            this.config = YamlConfiguration.loadConfiguration(configFile);
            
            if (this.config == null) {
                throw new IllegalStateException("Configuration loaded as null");
            }
            
            logger.info("Death messages configuration loaded successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load deaths.yml: " + e.getMessage(), e);
            this.config = new YamlConfiguration();
        }
    }

    /**
     * Creates deaths.yml from resource if it doesn't exist
     */
    private void saveDefaultConfig() {
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                plugin.saveResource("deaths.yml", false);
                logger.info("Created default deaths.yml");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to create default deaths.yml: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Checks if the death message system is enabled
     * @return true if enabled
     */
    public boolean isEnabled() {
        return config.getBoolean("settings.enabled", true);
    }

    /**
     * Checks if fallback to generic message is enabled when no custom message is found
     * @return true if fallback is enabled
     */
    public boolean useFallback() {
        return config.getBoolean("settings.use-fallback", true);
    }

    /**
     * Checks if debug logging is enabled for death message system
     * @return true if debug is enabled
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug", false);
    }

    /**
     * Checks if death coordinates should be shown in chat
     * @return true if coordinates should be shown
     */
    public boolean showCoordinates() {
        return config.getBoolean("settings.show-coordinates", true);
    }

    /**
     * Gets the underlying FileConfiguration object
     * @return FileConfiguration instance
     */
    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets the deaths.yml file
     * @return File instance
     */
    @NotNull
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Saves the configuration to file
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save deaths.yml: " + e.getMessage(), e);
        }
    }

    /**
     * Reloads the configuration from file
     */
    public void reload() {
        load();
    }

    /**
     * Gets the placeholder text for "unknown" values
     * @return Configured unknown placeholder text
     */
    @NotNull
    public String getUnknownPlaceholder() {
        return config.getString("placeholders.unknown", "Unknown");
    }

    /**
     * Gets the placeholder text for "none" values
     * @return Configured none placeholder text
     */
    @NotNull
    public String getNonePlaceholder() {
        return config.getString("placeholders.none", "None");
    }

    /**
     * Gets the placeholder text for "unknown entity" values
     * @return Configured unknown entity placeholder text
     */
    @NotNull
    public String getUnknownEntityPlaceholder() {
        return config.getString("placeholders.unknown-entity", "Something");
    }

    /**
     * Gets the placeholder text for "unknown player" values
     * @return Configured unknown player placeholder text
     */
    @NotNull
    public String getUnknownPlayerPlaceholder() {
        return config.getString("placeholders.unknown-player", "Someone");
    }

    // ========================================
    // Indirect Death Tracking Configuration
    // ========================================

    /**
     * Checks if indirect death tracking is enabled
     * @return true if indirect death tracking is enabled
     */
    public boolean isIndirectTrackingEnabled() {
        return config.getBoolean("indirect-deaths.enabled", true);
    }

    /**
     * Gets the tracking window in seconds for indirect deaths
     * Valid range: 5-60 seconds
     * @return Tracking window in seconds (default: 10)
     */
    public int getTrackingWindow() {
        int window = config.getInt("indirect-deaths.tracking-window", 10);
        
        // Validate range: 5-60 seconds
        if (window < 5) {
            logger.log(Level.WARNING, "Indirect death tracking window is too low ({0}s). Using minimum value of 5 seconds.", window);
            return 5;
        }
        if (window > 60) {
            logger.log(Level.WARNING, "Indirect death tracking window is too high ({0}s). Using maximum value of 60 seconds.", window);
            return 60;
        }
        
        return window;
    }

    /**
     * Gets the minimum damage required to trigger indirect death tracking (in hearts)
     * Valid range: 0.5-20.0 hearts
     * @return Minimum damage in hearts (default: 1.0)
     */
    public double getMinimumDamage() {
        double damage = config.getDouble("indirect-deaths.minimum-damage", 1.0);
        
        // Validate range: 0.5-20.0 hearts
        if (damage < 0.5) {
            logger.log(Level.WARNING, "Indirect death minimum damage is too low ({0} hearts). Using minimum value of 0.5 hearts.", damage);
            return 0.5;
        }
        if (damage > 20.0) {
            logger.log(Level.WARNING, "Indirect death minimum damage is too high ({0} hearts). Using maximum value of 20.0 hearts.", damage);
            return 20.0;
        }
        
        return damage;
    }

    /**
     * Checks if melee damage should be tracked for indirect deaths
     * @return true if melee damage tracking is enabled
     */
    public boolean isTrackMelee() {
        return config.getBoolean("indirect-deaths.track-melee", true);
    }

    /**
     * Checks if projectile damage should be tracked for indirect deaths
     * @return true if projectile damage tracking is enabled
     */
    public boolean isTrackProjectile() {
        return config.getBoolean("indirect-deaths.track-projectile", true);
    }

    /**
     * Checks if explosion damage should be tracked for indirect deaths
     * @return true if explosion damage tracking is enabled
     */
    public boolean isTrackExplosion() {
        return config.getBoolean("indirect-deaths.track-explosion", true);
    }

    /**
     * Gets the list of death causes that are eligible for indirect attribution
     * @return List of death cause names (e.g., "FALL", "VOID", "LAVA")
     */
    @NotNull
    public List<String> getTrackedCauses() {
        List<String> causes = config.getStringList("indirect-deaths.track-causes");
        
        // Return default list if not configured
        if (causes.isEmpty()) {
            return List.of("FALL", "VOID", "LAVA", "DROWNING", "FIRE", "FIRE_TICK", "SUFFOCATION", "CONTACT");
        }
        
        return causes;
    }

    /**
     * Checks if custom entity names (name tags) should be used in death messages
     * @return true if custom names should be used (default: true)
     */
    public boolean useCustomEntityNames() {
        return config.getBoolean("settings.entity-names.use-entity-nametags", true);
    }

    /**
     * Gets the priority order for entity names
     * @return "nametag" if name tags have priority, "custom-type" if custom-type-names have priority
     */
    @NotNull
    public String getEntityNamePriority() {
        String priority = config.getString("settings.entity-names.priority", "nametag");
        
        // Validate the priority value
        if (!"nametag".equalsIgnoreCase(priority) && !"custom-type".equalsIgnoreCase(priority)) {
            logger.log(Level.WARNING, "Invalid entity name priority: {0}. Using default: nametag", priority);
            return "nametag";
        }
        
        return priority.toLowerCase();
    }

    /**
     * Gets a custom name for an entity type if configured
     * @param entityType The entity type (e.g., "ZOMBIE", "SKELETON")
     * @return Custom name if configured, null otherwise
     */
    public String getCustomEntityTypeName(String entityType) {
        if (entityType == null || entityType.isEmpty()) {
            return null;
        }
        
        String path = "settings.entity-names.custom-type-names." + entityType.toUpperCase();
        if (config.contains(path)) {
            return config.getString(path);
        }
        
        return null;
    }
}
