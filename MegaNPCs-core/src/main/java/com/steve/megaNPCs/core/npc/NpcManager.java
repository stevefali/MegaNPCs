package com.steve.megaNPCs.core.npc;

import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class NpcManager {

    private static final Map<String, NPC> npcsByName = new HashMap<>();
    private static final Map<Integer, NPC> npcsById = new HashMap<>();
    private static final Map<String, List<Integer>> npcIdsByWorld = new HashMap<>();

    public static void registerNpc(NPC npc) {
        npcsById.put(npc.getId(), npc);
        npcsByName.put(npc.getNpcName(), npc);
        String worldName = npc.serverLevel().getWorld().getName();
        List<Integer> worldNpcIds = npcIdsByWorld.getOrDefault(worldName, new ArrayList<>());
        worldNpcIds.add(npc.getId());
        npcIdsByWorld.put(worldName, worldNpcIds);
    }

    public static void unregisterNpc(int id) {
        NPC npc = npcsById.remove(id);
        if (npc == null) {
            return;
        }

        for (UUID uuid : npc.shownToPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
                connection.send(new ClientboundRemoveEntitiesPacket(id));
            }
        }

        npcsByName.remove(npc.getNpcName());

        String worldName = npc.serverLevel().getWorld().getName();
        var worldNpcIds = npcIdsByWorld.get(worldName);
        if (worldNpcIds != null) {
            worldNpcIds.remove(Integer.valueOf(id));
            if (npcIdsByWorld.get(worldName).isEmpty()) {
                npcIdsByWorld.remove(worldName);
            }
        }
        npc.cleanup();
    }

    public static NPC getById(int id) {
        return npcsById.get(id);
    }

    public static NPC getByNpcName(String npcName) {
        return npcsByName.get(npcName);
    }

    public static List<Integer> getNpcsByWorld(String worldName) {
        return npcIdsByWorld.get(worldName);
    }

    public static boolean unregisterNpcsByWorld(String worldName) {
        List<Integer> ids = npcIdsByWorld.get(worldName);
        if (ids == null) {
            return true;
        }
        for (Integer i : new ArrayList<>(ids)) {
            unregisterNpc(i);
        }
        return !npcIdsByWorld.containsKey(worldName);
    }

    public static List<UUID> getAllUuids() {
        return npcsById.values().stream().map(Entity::getUUID).toList();
    }

    public static void unregisterAllNpcs() {
        List<NPC> allNpcs = new ArrayList<>(npcsById.values());

        for (NPC npc : allNpcs) {
            unregisterNpc(npc.getId());
        }
        allNpcs.clear();
    }

    public static void removePlayerFromAllNpcShownTo(UUID playerUuid) {
        for (NPC npc : npcsById.values()) {
            if (npc != null) {
                npc.shownToPlayers.remove(playerUuid);
            }
        }
    }

}
