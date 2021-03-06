package net.wtako.MineralLimiter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.wtako.MineralLimiter.Commands.CommandMlimit;
import net.wtako.MineralLimiter.EventHandlers.BlockActionsListener;
import net.wtako.MineralLimiter.Methods.HardDiskDatabase;
import net.wtako.MineralLimiter.Methods.MemoryDatabase;
import net.wtako.MineralLimiter.Utils.Lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main             instance;
    public static CoreProtectAPI    coreProtect;
    public static YamlConfiguration LANG;
    public static File              LANG_FILE;
    public static Logger            log = Logger.getLogger("MineralLimiter");

    @Override
    public void onEnable() {
        Main.instance = this;
        Main.coreProtect = getCoreProtect();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getCommand(getProperty("mainCommand")).setExecutor(new CommandMlimit());
        getServer().getPluginManager().registerEvents(new BlockActionsListener(), this);
        loadLang();
        try {
            new HardDiskDatabase();
            new MemoryDatabase();
        } catch (final SQLException e) {
            Main.log.severe("When you see this, that means this plugin is screwed.");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            MemoryDatabase.save();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadLang() {
        final File lang = new File(getDataFolder(), "messages.yml");
        if (!lang.exists()) {
            try {
                getDataFolder().mkdir();
                lang.createNewFile();
                final InputStream defConfigStream = getResource("messages.yml");
                if (defConfigStream != null) {
                    final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    defConfig.save(lang);
                    Lang.setFile(defConfig);
                    return;
                }
            } catch (final IOException e) {
                e.printStackTrace(); // So they notice
                Main.log.severe("[" + Main.getInstance().getName() + "] Couldn't create language file.");
                Main.log.severe("[" + Main.getInstance().getName() + "] This is a fatal error. Now disabling");
                setEnabled(false); // Without it loaded, we can't send them
                                   // messages
            }
        }
        final YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        for (final Lang item: Lang.values()) {
            if (conf.getString(item.getPath()) == null) {
                conf.set(item.getPath(), item.getDefault());
            }
        }
        Lang.setFile(conf);
        Main.LANG = conf;
        Main.LANG_FILE = lang;
        try {
            conf.save(getLangFile());
        } catch (final IOException e) {
            Main.log.log(Level.WARNING, "[" + Main.getInstance().getName() + "] Failed to save messages.yml.");
            Main.log.log(Level.WARNING, "[" + Main.getInstance().getName() + "] Report this stack trace to "
                    + getProperty("author") + ".");
            e.printStackTrace();
        }
    }

    /**
     * Gets the messages.yml config.
     * 
     * @return The messages.yml config.
     */
    public YamlConfiguration getLang() {
        return Main.LANG;
    }

    /**
     * Get the messages.yml file.
     * 
     * @return The messages.yml file.
     */
    public File getLangFile() {
        return Main.LANG_FILE;
    }

    public String getProperty(String key) {
        final YamlConfiguration spawnConfig = YamlConfiguration.loadConfiguration(getResource("plugin.yml"));
        return spawnConfig.getString(key);
    }

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (CoreProtect.isEnabled() == false) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 2) {
            return null;
        }

        return CoreProtect;
    }

    public static Main getInstance() {
        return Main.instance;
    }
}
