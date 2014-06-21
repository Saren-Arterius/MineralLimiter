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
import org.bukkit.scheduler.BukkitRunnable;

public class BlockActionsListener implements Listener {

    private static ArrayList<Block> playerPlacedOres       = new ArrayList<Block>();
    private static List<String>     AffectedWorlds         = Main.getInstance().getConfig()
                                                                   .getStringList("variable.AffectedWorlds");
    private static List<String>     AffectedBlockTypes     = Main.getInstance().getConfig()
                                                                   .getStringList("variable.AffectedBlockTypes");
    private static boolean          ReverseAffectedWorlds  = Main.getInstance().getConfig()
                                                                   .getBoolean("variable.ReverseAffectedWorlds");
    private static boolean          NoDropsIfOverLimit     = Main.getInstance().getConfig()
                                                                   .getBoolean("variable.NoDropsIfOverLimit");
    private static boolean          CancelEventIfOverLimit = Main.getInstance().getConfig()
                                                                   .getBoolean("variable.CancelEventIfOverLimit");
    private static boolean          IgnoreCreative         = Main.getInstance().getConfig()
                                                                   .getBoolean("variable.IgnoreCreative");

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();

        if (BlockActionsListener.ReverseAffectedWorlds) {
            if (isListContainsStringIgnoreCase(BlockActionsListener.AffectedWorlds, player.getWorld().getName())) {
                return;
            }
        } else {
            if (!isListContainsStringIgnoreCase(BlockActionsListener.AffectedWorlds, player.getWorld().getName())) {
                return;
            }
        }

        if (player.hasPermission("MineralLimiter.ignore")) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE && BlockActionsListener.IgnoreCreative) {
            return;
        }
        if (BlockActionsListener.playerPlacedOres.contains(event.getBlock())) {
            BlockActionsListener.playerPlacedOres.remove(event.getBlock());
            return;
        }
        try {
            final String blockTypeString = event.getBlock().getType().toString();
            // Restricted block
            if (!isListContainsStringIgnoreCase(BlockActionsListener.AffectedBlockTypes, blockTypeString)) {
                return;
            }
            // in allowance
            if (MineralLimiterDatabase.canMineThisBlock(player, blockTypeString)) {
                final BukkitRunnable timer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            player.sendMessage(Lang.DO_NOT_MINE_IN_THIS_WORLD.toString());
                            MineralLimiterDatabase.insertRecord(player, blockTypeString,
                                    (int) (System.currentTimeMillis() / 1000L));
                        } catch (final SQLException e) {
                            e.printStackTrace();
                        }
                    }
                };
                timer.runTaskAsynchronously(Main.getInstance());
            } else {
                if (BlockActionsListener.CancelEventIfOverLimit) {
                    player.sendMessage(Lang.DO_NOT_MINE_IN_THIS_WORLD.toString());
                    event.setCancelled(true);
                } else if (BlockActionsListener.NoDropsIfOverLimit) {
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
        final Player player = event.getPlayer();
        if (BlockActionsListener.ReverseAffectedWorlds) {
            if (BlockActionsListener.AffectedWorlds.contains(player.getLocation().getWorld().getName())) {
                return;
            }
        } else {
            if (!BlockActionsListener.AffectedWorlds.contains(player.getLocation().getWorld().getName())) {
                return;
            }
        }
        final String blockTypeString = event.getBlock().getType().toString();
        if (isListContainsStringIgnoreCase(BlockActionsListener.AffectedBlockTypes, blockTypeString)) {
            player.sendMessage(Lang.DO_NOT_PLACE_ORES.toString());
            BlockActionsListener.playerPlacedOres.add(event.getBlock());
        }
    }

    @EventHandler
    public void onPistonEvent(BlockPistonExtendEvent event) {
        for (final Block block: event.getBlocks()) {
            if (BlockActionsListener.playerPlacedOres.contains(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonEvent(BlockPistonRetractEvent event) {
        if (BlockActionsListener.playerPlacedOres.contains(event.getRetractLocation().getBlock())) {
            event.setCancelled(true);
        }
    }

    public boolean isListContainsStringIgnoreCase(List<String> list, String string) {
        for (final String elem: list) {
            if (elem.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

}
