package net.wtako.MineralLimiter.Methods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.wtako.MineralLimiter.Main;

import org.bukkit.scheduler.BukkitRunnable;

public class MemoryDatabase {

    private static MemoryDatabase instance;
    private final Connection      conn;

    public MemoryDatabase() throws SQLException {
        MemoryDatabase.instance = this;
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        restore();
        final BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    MemoryDatabase.save();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.runTaskTimerAsynchronously(Main.getInstance(), 30L * 20L, 180L * 20L);
    }

    private void restore() throws SQLException {
        final Statement cur = conn.createStatement();
        cur.execute("CREATE TABLE `mineral_records` (`rowid` INTEGER PRIMARY KEY AUTOINCREMENT, `player` VARCHAR(20) NOT NULL, `mineral_type` VARCHAR(32) NOT NULL, `timestamp` INT NOT NULL)");
        cur.execute("CREATE TABLE `configs` (`config` VARCHAR(128) PRIMARY KEY, `value` VARCHAR(128) NULL)");
        cur.close();
        MemoryDatabase.copyMineralDatabase(conn, HardDiskDatabase.getInstance().getConn());
    }

    public static void save() throws SQLException {
        MemoryDatabase.instance.purgeData();
        HardDiskDatabase.getInstance().flushDatabase();
        MemoryDatabase.copyMineralDatabase(HardDiskDatabase.getInstance().getConn(), MemoryDatabase.instance.conn);
    }

    public void purgeData() throws SQLException {
        final int oldTime = (int) (System.currentTimeMillis() / 1000L)
                - Main.getInstance().getConfig().getInt("variable.TimeLimit");
        final PreparedStatement delStmt = conn.prepareStatement("DELETE FROM `mineral_records` WHERE timestamp < ?");
        delStmt.setInt(1, oldTime);
        delStmt.execute();
        delStmt.close();
    }

    public static void copyMineralDatabase(Connection toConn, Connection fromConn) throws SQLException {
        toConn.setAutoCommit(false);
        final PreparedStatement insStmt = toConn
                .prepareStatement("INSERT INTO `mineral_records` (`player`, `mineral_type`, `timestamp`) VALUES (?, ?, ?)");
        final PreparedStatement selStmt = fromConn.prepareStatement("SELECT * FROM `mineral_records`");
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            insStmt.setString(1, result.getString(2));
            insStmt.setString(2, result.getString(3));
            insStmt.setInt(3, result.getInt(4));
            insStmt.execute();
        }
        final PreparedStatement insStmt2 = toConn
                .prepareStatement("INSERT INTO `configs` (`config`, `value`) VALUES (?, ?)");
        final PreparedStatement selStmt2 = fromConn.prepareStatement("SELECT * FROM `mineral_records`");
        final ResultSet result2 = selStmt2.executeQuery();
        while (result2.next()) {
            insStmt2.setString(1, result2.getString(1));
            insStmt2.setString(2, result2.getString(2));
            insStmt2.execute();
        }
        toConn.commit();
        toConn.setAutoCommit(true);
    }

    public static MemoryDatabase getInstance() {
        return MemoryDatabase.instance;
    }

    public Connection getConn() {
        return conn;
    }

}
