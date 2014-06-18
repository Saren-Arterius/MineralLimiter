package net.wtako.MineralLimiter.Methods;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.MineralLimiter.Main;
import net.wtako.MineralLimiter.Utils.Lang;

import org.bukkit.entity.Player;

public class MineralLimiterDatabase extends Database {

    public MineralLimiterDatabase() throws SQLException {
        super();
    }

    public static boolean canMineThisBlock(Player player, String blockType) throws SQLException {
        int currentTime = (int) (System.currentTimeMillis() / 1000L);
        int TimeLimit = Main.getInstance().getConfig().getInt("variable.TimeLimit");
        int oldTime = currentTime - TimeLimit;
        final PreparedStatement selStmt = Database.getInstance().conn
                .prepareStatement("SELECT count(*) FROM `mineral_records` WHERE player = ? AND mineral_type = ? AND timestamp > ?");
        selStmt.setString(1, player.getName().toLowerCase());
        selStmt.setString(2, blockType);
        selStmt.setInt(3, oldTime);
        final int count = selStmt.executeQuery().getInt(1);
        selStmt.close();
        int limit;
        if (Main.getInstance().getConfig().isInt("variable.BlockTypesLimitPerPeriod." + blockType)) {
            limit = Main.getInstance().getConfig().getInt("variable.BlockTypesLimitPerPeriod." + blockType);
        } else {
            limit = Main.getInstance().getConfig().getInt("variable.BlockTypesLimitPerPeriod.Default");
        }
        if (count >= limit) {
            player.sendMessage(MessageFormat.format(Lang.YOU_MINED_TOO_MUCH.toString(), blockType, TimeLimit, count,
                    limit));
            return false;
        }
        player.sendMessage(Lang.DO_NOT_MINE_IN_THIS_WORLD.toString());
        insertRecord(player, blockType, currentTime);
        return true;
    }

    private static void insertRecord(Player player, String blockType, int currentTime) throws SQLException {
        final PreparedStatement insStmt = Database.getInstance().conn
                .prepareStatement("INSERT INTO `mineral_records` (player, mineral_type, timestamp) VALUES (?, ?, ?)");
        insStmt.setString(1, player.getName().toLowerCase());
        insStmt.setString(2, blockType);
        insStmt.setInt(3, currentTime);
        insStmt.execute();
        insStmt.close();
    }
}
