package com.steve.megaNPCs.core.npc;

import com.mojang.authlib.GameProfile;
import com.steve.MegaNPCs.api.NpcInteractionEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class NPC extends ServerPlayer {

    private Consumer<Player> onInteract;
    private final String npcName;

    public NPC(
            MinecraftServer server,
            ServerLevel level,
            GameProfile gameProfile,
            ClientInformation clientInformation,
            @Nullable Consumer<Player> onInteract
    ) {
        super(server, level, gameProfile, clientInformation);
        this.onInteract = onInteract;

        String worldName = level.getWorld().getName();
        this.npcName = this.displayName + "_" + worldName;
    }

    public void setOnInteract(Consumer<Player> interactionCallback) {
        this.onInteract = interactionCallback;
    }

    public void interact(Player player) {
        NpcInteractionEvent event = new NpcInteractionEvent(this.getId(), npcName, player);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled() && onInteract != null) {
            onInteract.accept(player);
        }
    }

    public String getNpcName() {
        return this.npcName;
    }
}
