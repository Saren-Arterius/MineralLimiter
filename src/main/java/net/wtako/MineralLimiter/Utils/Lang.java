package net.wtako.MineralLimiter.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * An enum for requesting strings from the language file.
 * 
 * @author gomeow
 */
public enum Lang {

    TITLE("title", "[MineralLimiter]"),
    DO_NOT_MINE_IN_THIS_WORLD(
            "do-not-mine-in-this-world",
            "&ePlease do not mine in this world. Please go resource world instead."),
    YOU_MINED_TOO_MUCH(
            "you-mined-too-much",
            "&cYou have reached the allowance of mining {0} within {1} seconds. ({2}/{3})"),
    THERE_IS_NO_MORE_DROPS("there-is-no-more-drops", "&eThere is no more drops of {0} for you."),
    DO_NOT_PLACE_ORES(
            "do-not-place-ores",
            "&eTo avoid confliction, please do not place ores. Exceptions will expire after server restart."),
    PLUGIN_RELOADED("plugin-reloaded", "&aPlugin reloaded."),
    DB_EXCEPTION("db-exception", "&4A database error occured! Please contact server administrators."),
    NO_PERMISSION_COMMAND("no-permission-command", "&cYou are not allowed to use this command.");

    private String                   path;
    private String                   def;
    private static YamlConfiguration LANG;

    /**
     * Lang enum constructor.
     * 
     * @param path
     *            The string path.
     * @param start
     *            The default string.
     */
    Lang(String path, String start) {
        this.path = path;
        def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     * 
     * @param config
     *            The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        Lang.LANG = config;
    }

    @Override
    public String toString() {
        if (this == TITLE) {
            return ChatColor.translateAlternateColorCodes('&', Lang.LANG.getString(path, def)) + " ";
        }
        return ChatColor.translateAlternateColorCodes('&', Lang.LANG.getString(path, def));
    }

    /**
     * Get the default value of the path.
     * 
     * @return The default value of the path.
     */
    public String getDefault() {
        return def;
    }

    /**
     * Get the path to the string.
     * 
     * @return The path to the string.
     */
    public String getPath() {
        return path;
    }
}