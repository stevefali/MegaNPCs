package com.steve.megaNPCs.core.util;

import com.steve.megaNPCs.core.MegaNPCsCore;
import com.steve.megaNPCs.core.npc.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrphanFinder {

    public static List<Entity> findOrphansByWorld(String worldName) {

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            JavaPlugin.getPlugin(MegaNPCsCore.class).getLogger().warning("Error: world " + worldName + " is null");
            return List.of();
        }

        List<Entity> worldOrphans = new ArrayList<>();

        List<UUID> worldRegisteredNpcDisplays = new ArrayList<>();
        if (NpcManager.getNpcsByWorld(worldName) != null) {
            for (UUID uuid : NpcManager.getNpcsByWorld(worldName)) {
                worldRegisteredNpcDisplays.addAll(NpcManager.getById(uuid).getDisplayEntities());
            }
        }

        List<Entity> worldDisplays = Bukkit.getWorld(worldName).getEntities()
                .stream().filter(entity -> ((entity.getType() == EntityType.ARMOR_STAND && entity.getCustomName() != null)
                        || entity.getType() == EntityType.TEXT_DISPLAY
                )).toList();

        for (Entity display : worldDisplays) {
            if (!worldRegisteredNpcDisplays.contains(display.getUniqueId())) {
                worldOrphans.add(display);
            }
        }

        return worldOrphans;
    }

}
