package net.wtako.MineralLimiter.Methods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import net.wtako.MineralLimiter.Main;
import net.wtako.MineralLimiter.Utils.Lang;

public class Database {

    private static Database instance;
    public Connection       conn;

    public Database() throws SQLException {
        Database.instance = this;
        final String path = MessageFormat.format("jdbc:sqlite:{0}/{1}", Main.getInstance().getDataFolder()
                .getAbsolutePath(), "MineralLimiter.db");
        conn = DriverManager.getConnection(path);
    }

    private void addConfig(String config, String value) throws SQLException {
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
        Main.log.info(Lang.TITLE.toString() + "Purging data...");
        final PreparedStatement delStmt = conn.prepareStatement("DELETE FROM `mineral_records` WHERE timestamp < ?");
        final int oldTime = (int) (System.currentTimeMillis() / 1000L)
                + Main.getInstance().getConfig().getInt("variable.TimeLimit");
        delStmt.setInt(1, oldTime);
        delStmt.execute();
        delStmt.close();
    }

    private boolean areTablesExist() {
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

    public void check() throws SQLException {
        Main.log.info(Lang.TITLE.toString() + "Checking databases...");
        if (!areTablesExist()) {
            Main.log.info(Lang.TITLE.toString() + "Creating databases...");
            createTables();
            Main.log.info(Lang.TITLE.toString() + "Done.");
        }
    }

    public static Database getInstance() {
        return Database.instance;
    }
    // private void updateDatabase() {}
}