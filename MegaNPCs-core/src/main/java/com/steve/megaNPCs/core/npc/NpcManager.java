package com.steve.megaNPCs.core.npc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        npcsByName.remove(npcsById.get(id).getNpcName());
        String worldName = npcsById.get(id).serverLevel().getWorld().getName();
        npcIdsByWorld.get(worldName).remove(Integer.valueOf(id));
        if (npcIdsByWorld.get(worldName).isEmpty()) {
            npcIdsByWorld.remove(worldName);
        }
        npcsById.remove(id);
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

}
