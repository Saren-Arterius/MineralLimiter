package net.wtako.MineralLimiter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.wtako.MineralLimiter.Commands.CommandMlimit;
import net.wtako.MineralLimiter.EventHandlers.BlockActionsListener;
import net.wtako.MineralLimiter.Methods.Database;
import net.wtako.MineralLimiter.Utils.Lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main             instance;
    public static YamlConfiguration LANG;
    public static File              LANG_FILE;
    public static Logger            log = Logger.getLogger("MineralLimiter");

    @Override
    public void onEnable() {
        Main.instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getCommand("mlimit").setExecutor(new CommandMlimit());
        getServer().getPluginManager().registerEvents(new BlockActionsListener(), this);
        loadLang();
        try {
            new Database();
            Database.getInstance().check();
            Database.getInstance().purgeData();
        } catch (final SQLException e) {
            Main.log.severe("When you see this, that means this plugin is screwed.");
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
                Main.log.severe("[MineralLimiter] Couldn't create language file.");
                Main.log.severe("[MineralLimiter] This is a fatal error. Now disabling");
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
            Main.log.log(Level.WARNING, "PluginName: Failed to save messages.yml.");
            Main.log.log(Level.WARNING, "PluginName: Report this stack trace to Saren.");
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

    public static Main getInstance() {
        return Main.instance;
    }
}
