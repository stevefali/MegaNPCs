package com.steve.megaNPCs.core.command;

import com.steve.megaNPCs.core.util.OrphanFinder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

public class CleanupOrphansCommand implements CommandExecutor {

    private final Logger logger;

    public CleanupOrphansCommand(Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        String worldName = player.getWorld().getName();

        List<Entity> worldOrphans = OrphanFinder.findOrphansByWorld(worldName);
        for (org.bukkit.entity.Entity orphan : worldOrphans) {
            orphan.remove();
        }

        String message = !worldOrphans.isEmpty()
                ? "MegaNPCs removed " + worldOrphans.size() + " orphaned display entities from world " + worldName
                : "MegaNPCs scanned world " + worldName + " and found 0 orphaned display entities";

        logger.info(message);
        player.sendMessage("§a" + message);

        return true;
    }


}
