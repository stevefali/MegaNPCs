package com.steve.megaNPCs.core.npc;

import com.google.gson.Gson;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NpcSkinDataAccess {
    private static final String NPC_SKIN_FILE_PATH = "../npc-skins.json";

    public static Map<String, NpcSkin> NPC_SKINS = null;

    public static CompletableFuture<Map<String, NpcSkin>> load(Plugin plugin) {
        CompletableFuture<Map<String, NpcSkin>> future = new CompletableFuture<>();

        if (NPC_SKINS != null && !NPC_SKINS.isEmpty()) {
            future.complete(NPC_SKINS);
            return future;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Gson gson = new Gson();
            File file = new File(NPC_SKIN_FILE_PATH);
            if (!file.exists()) {
                future.complete(new HashMap<>());
                return;
            }
            try (FileReader fileReader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, NpcSkin>>() {
                }.getType();
                Map<String, NpcSkin> result = gson.fromJson(fileReader, type);
                if (result != null) {
                    future.complete(result);
                    NPC_SKINS = result;
                } else {
                    future.complete(new HashMap<>());
                }
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

}
