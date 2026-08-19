package com.steve.megaNPCs.core.npc;

import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class NpcManager {

    private static final Map<String, NPC> npcsByName = new HashMap<>();
    private static final Map<UUID, NPC> npcsById = new HashMap<>();
    private static final Map<String, List<UUID>> npcIdsByWorld = new HashMap<>();

    public static void registerNpc(NPC npc) {
        npcsById.put(npc.getUUID(), npc);
        npcsByName.put(npc.getNpcName(), npc);
        String worldName = npc.serverLevel().getWorld().getName();
        List<UUID> worldNpcIds = npcIdsByWorld.getOrDefault(worldName, new ArrayList<>());
        worldNpcIds.add(npc.getUUID());
        npcIdsByWorld.put(worldName, worldNpcIds);
    }

    public static void unregisterNpc(UUID id) {
        NPC npc = npcsById.remove(id);
        if (npc == null) {
            return;
        }

        for (UUID uuid : npc.shownToPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
                connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));
            }
        }

        npcsByName.remove(npc.getNpcName());

        String worldName = npc.serverLevel().getWorld().getName();
        var worldNpcIds = npcIdsByWorld.get(worldName);
        if (worldNpcIds != null) {
            worldNpcIds.remove(id);
            if (npcIdsByWorld.get(worldName).isEmpty()) {
                npcIdsByWorld.remove(worldName);
            }
        }
        npc.cleanup();
    }

    public static NPC getById(UUID id) {
        return npcsById.get(id);
    }

    public static NPC getByNpcName(String npcName) {
        return npcsByName.get(npcName);
    }

    public static List<UUID> getNpcsByWorld(String worldName) {
        return npcIdsByWorld.get(worldName);
    }

    public static boolean unregisterNpcsByWorld(String worldName) {
        List<UUID> ids = npcIdsByWorld.get(worldName);
        if (ids == null) {
            return true;
        }
        for (UUID uuid : new ArrayList<>(ids)) {
            unregisterNpc(uuid);
        }
        return !npcIdsByWorld.containsKey(worldName);
    }

    public static List<UUID> getAllUuids() {
//        return npcsById.values().stream().map(Entity::getUUID).toList();
        return npcsById.keySet().stream().toList();
    }

    public static void unregisterAllNpcs() {
        List<NPC> allNpcs = new ArrayList<>(npcsById.values());

        for (NPC npc : allNpcs) {
            unregisterNpc(npc.getUUID());
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

    public static NPC getByIntId(int npcIntId, World world) {
       List<UUID> worldUuids = npcIdsByWorld.get(world.getName());
       for (UUID uuid : worldUuids) {
           if (npcsById.get(uuid).getId() == npcIntId) {
               return npcsById.get(uuid);
           }
       }
       return null;
    }

}
