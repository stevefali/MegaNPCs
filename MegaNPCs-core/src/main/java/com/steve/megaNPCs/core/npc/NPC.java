package com.steve.megaNPCs.core.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.steve.MegaNPCs.api.NpcInteractionEvent;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftTextDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class NPC extends ServerPlayer {

    private Consumer<Player> onInteract;
    private final String npcName;
    private final List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
    public final List<UUID> shownToPlayers = new ArrayList<>();
    public float trueHeadYRot = 0;
    private final List<ArmorStand> nameLineArmorStands = new ArrayList<>();
    private Display.TextDisplay title;

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
        NpcInteractionEvent event = new NpcInteractionEvent(this.getUUID(), npcName, player);
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
        removeNameLineArmorStands();
        this.onInteract = null;
        this.equipmentList.clear();
        this.shownToPlayers.clear();
        this.removeNameLineArmorStands();
        this.removeTitle();
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 target) {
        Vec3 vec3 = anchor.apply(this);
        double d = target.x - vec3.x;
        double d1 = target.y - vec3.y;
        double d2 = target.z - vec3.z;
        double squareRoot = Math.sqrt(d * d + d2 * d2);
        this.setXRot(Mth.wrapDegrees((float) (-(Mth.atan2(d1, squareRoot) * (double) 180.0F / (double) (float) Math.PI))));
        this.setYRot(Mth.wrapDegrees((float) (Mth.atan2(d2, d) * (double) 180.0F / (double) (float) Math.PI) - 90.0F));
        this.trueHeadYRot = this.getYRot();
        this.setYHeadRot(trueHeadYRot);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public void removeNameLineArmorStands() {
        for (ArmorStand armorStand : nameLineArmorStands) {
            if (armorStand != null) {
                armorStand.remove(RemovalReason.DISCARDED);
            }
        }
        this.nameLineArmorStands.clear();
    }

    public void addNameLineArmorStand(ArmorStand armorStand) {
        this.nameLineArmorStands.add(armorStand);
    }

    public String[] getCurrentNameLinesText() {
        return nameLineArmorStands.stream().map(stand ->
                stand.getCustomName().getString()).toArray(String[]::new);

    }

    public void updateNameLineArmorStandsLocation() {
        Vec3 npcPos = new Vec3(this.getX(), this.getY() - 0.2, this.getZ());
        for (int i = 0; i < nameLineArmorStands.size(); i++) {
            nameLineArmorStands.get(i).setPos(
                    npcPos.x(),
                    npcPos.y() + (i * 0.3),
                    npcPos.z()
            );
        }
    }

    public void updateTitleLocation() {
        if (this.title == null) {
            return;
        }
        Vec3 pos = new Vec3(
                this.getX(),
                this.getY() - 0.2 + 2 + ((nameLineArmorStands.size() + 1) * 0.3),
                this.getZ());
        this.title.setPos(pos);
    }

    public void setTitle(String titleText, double scale) {
        if (this.title != null) {
            this.title.remove(RemovalReason.DISCARDED);
            this.title = null;
        }
        World world = this.getBukkitEntity().getWorld();
        TextDisplay textDisplay = (TextDisplay) world.spawnEntity(
                new Location(
                        world,
                        this.getX(),
                        this.getY() - 0.2 + 2 + ((nameLineArmorStands.size() + 1) * 0.3),
                        this.getZ()
                ),
                EntityType.TEXT_DISPLAY
        );
        ((CraftTextDisplay) textDisplay).setText(titleText);
        Transformation transformation = textDisplay.getTransformation();
        transformation.getScale().set(scale);
        textDisplay.setTransformation(transformation);

        textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
//        textDisplay.setSeeThrough(true);
        this.title = ((CraftTextDisplay) textDisplay).getHandle();
    }

    public void removeTitle() {
        if (this.title != null) {
            this.title.remove(RemovalReason.DISCARDED);
            this.title = null;
        }
    }

}
