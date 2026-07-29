package com.steve.megaNPCs.core.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.steve.MegaNPCs.api.NpcInteractionEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class NPC extends ServerPlayer {

    private Consumer<Player> onInteract;
    private final String npcName;
    private final List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
    public final List<UUID> shownToPlayers = new ArrayList<>();
    public float trueHeadYRot = 0;

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

    public List<Pair<EquipmentSlot, ItemStack>> getEquipmentList() {
        return this.equipmentList;
    }

    public void equipItem(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        Optional<Pair<EquipmentSlot, ItemStack>> existingItemPair =
                equipmentList.stream().filter(itemPair -> itemPair.getFirst().equals(equipmentSlot)).findAny();
        existingItemPair.ifPresent(equipmentList::remove);
        equipmentList.add(Pair.of(equipmentSlot, itemStack));
    }

    public void cleanup() {
        this.onInteract = null;
        this.equipmentList.clear();
        shownToPlayers.clear();
    }
}
