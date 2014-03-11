package net.wtako.MineralLimiter.EventHandlers;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.wtako.MineralLimiter.Main;
import net.wtako.MineralLimiter.Methods.MineralLimiterDatabase;
import net.wtako.MineralLimiter.Utils.Lang;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockActionsListener implements Listener {

    private static ArrayList<Block> playerPlacedOres = new ArrayList<Block>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final List<String> AffectedWorlds = Main.getInstance().getConfig().getStringList("variable.AffectedWorlds");
        final List<String> AffectedBlockTypes = Main.getInstance().getConfig()
                .getStringList("variable.AffectedBlockTypes");
        final boolean NoDropsIfOverLimit = Main.getInstance().getConfig().getBoolean("variable.NoDropsIfOverLimit");
        final boolean CancelEventIfOverLimit = Main.getInstance().getConfig()
                .getBoolean("variable.CancelEventIfOverLimit");
        final boolean IgnoreCreative = Main.getInstance().getConfig().getBoolean("variable.IgnoreCreative");
        final Player player = event.getPlayer();

        if (!AffectedWorlds.contains(player.getLocation().getWorld().getName())) {
            return;
        }
        if (player.hasPermission("MineralLimiter.ignore")) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE && IgnoreCreative) {
            return;
        }
        if (BlockActionsListener.playerPlacedOres.contains(event.getBlock())) {
            BlockActionsListener.playerPlacedOres.remove(event.getBlock());
            return;
        }
        try {
            final String blockTypeString = event.getBlock().getType().toString();
            if (AffectedBlockTypes.contains(blockTypeString)
                    && !(new MineralLimiterDatabase(player, blockTypeString).canMineThisBlock())) {
                if (CancelEventIfOverLimit) {
                    event.setCancelled(true);
                } else if (NoDropsIfOverLimit) {
                    event.getBlock().setType(Material.AIR);
                    event.setExpToDrop(0);
                    player.sendMessage(MessageFormat.format(Lang.THERE_IS_NO_MORE_DROPS.toString(), blockTypeString));
                }
            }
        } catch (final SQLException e) {
            player.sendMessage(Lang.DB_EXCEPTION.toString());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final List<String> AffectedWorlds = Main.getInstance().getConfig().getStringList("variable.AffectedWorlds");
        final List<String> AffectedBlockTypes = Main.getInstance().getConfig()
                .getStringList("variable.AffectedBlockTypes");
        final Player player = event.getPlayer();
        if (!AffectedWorlds.contains(player.getLocation().getWorld().getName())) {
            return;
        }
        final String blockTypeString = event.getBlock().getType().toString();
        if (AffectedBlockTypes.contains(blockTypeString)) {
            player.sendMessage(Lang.DO_NOT_PLACE_ORES.toString());
            BlockActionsListener.playerPlacedOres.add(event.getBlock());
        }
    }

    @EventHandler
    public void onPistonEvent(BlockPistonExtendEvent event) {
        for (Block block: event.getBlocks()) {
            if (playerPlacedOres.contains(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonEvent(BlockPistonRetractEvent event) {
        if (playerPlacedOres.contains(event.getRetractLocation().getBlock())) {
            event.setCancelled(true);
        }
    }

}
