package net.wtako.MineralLimiter.Methods;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import net.wtako.MineralLimiter.Main;
import net.wtako.MineralLimiter.Utils.FileTool;
import net.wtako.MineralLimiter.Utils.Lang;

public class HardDiskDatabase {

    private final String            path;
    private static HardDiskDatabase instance;
    private Connection              conn;

    public HardDiskDatabase() throws SQLException {
        HardDiskDatabase.instance = this;
        path = MessageFormat.format("jdbc:sqlite:{0}/{1}", Main.getInstance().getDataFolder().getAbsolutePath(), Main
                .getInstance().getName() + ".db");
        conn = DriverManager.getConnection(path);
        check(true);
        purgeData();
    }

    public void addConfig(String config, String value) throws SQLException {
        final PreparedStatement stmt = conn.prepareStatement("INSERT INTO `configs` (`config`, `value`) VALUES (?, ?)");
        stmt.setString(1, config);
        stmt.setString(2, value);
        stmt.execute();
        stmt.close();
    }

    public void createTables() throws SQLException {
        final Statement cur = conn.createStatement();
        cur.execute("CREATE TABLE `mineral_records` (`rowid` INTEGER PRIMARY KEY AUTOINCREMENT, `player` VARCHAR(20) NOT NULL, `mineral_type` VARCHAR(32) NOT NULL, `timestamp` INT NOT NULL)");
        cur.execute("CREATE TABLE `configs` (`config` VARCHAR(128) PRIMARY KEY, `value` VARCHAR(128) NULL)");
        cur.close();
        addConfig("database_version", "1");
    }

    public void purgeData() throws SQLException {
        final int oldTime = (int) (System.currentTimeMillis() / 1000L)
                - Main.getInstance().getConfig().getInt("variable.TimeLimit");
        Main.log.info(Lang.TITLE.toString() + MessageFormat.format("Purging data older than {0}...", oldTime));
        final PreparedStatement delStmt = conn.prepareStatement("DELETE FROM `mineral_records` WHERE timestamp < ?");
        delStmt.setInt(1, oldTime);
        delStmt.execute();
        delStmt.close();
    }

    public boolean areTablesExist() {
        try {
            final Statement cur = conn.createStatement();
            cur.execute("SELECT * FROM `mineral_records` LIMIT 0");
            cur.execute("SELECT * FROM `configs` LIMIT 0");
            cur.close();
            return true;
        } catch (final SQLException ex) {
            return false;
        }
    }

    public void check(boolean log) throws SQLException {
        if (log) {
            Main.log.info(Lang.TITLE.toString() + "Checking tables...");
        }
        if (!areTablesExist()) {
            if (log) {
                Main.log.info(Lang.TITLE.toString() + "Creating tables...");
            }
            createTables();
        }
        if (log) {
            Main.log.info(Lang.TITLE.toString() + "Done.");
        }
    }

    public void flushDatabase() throws SQLException {
        conn.close();
        final File parent = new File(Main.getInstance().getDataFolder().getAbsolutePath());
        final File databaseFile = FileTool.getChildFile(parent, Main.getInstance().getName() + ".db", true);
        databaseFile.delete();
        conn = DriverManager.getConnection(path);
        check(false);
    }

    public static HardDiskDatabase getInstance() {
        return HardDiskDatabase.instance;
    }

    public Connection getConn() {
        return conn;
    }

}