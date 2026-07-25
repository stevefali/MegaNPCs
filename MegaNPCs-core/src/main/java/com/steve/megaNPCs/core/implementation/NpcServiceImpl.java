package com.steve.megaNPCs.core.implementation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.steve.MegaNPCs.api.NpcService;
import com.steve.megaNPCs.core.npc.NPC;
import com.steve.megaNPCs.core.npc.NpcManager;
import com.steve.megaNPCs.core.npc.NpcSkin;
import com.steve.megaNPCs.core.npc.NpcSkinDataAccess;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class NpcServiceImpl implements NpcService {

    private Plugin plugin;
    private static final String TEXTURES_PROPERTY = "textures";

    public NpcServiceImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int createNpc(Location location, String displayName, @Nullable String skinName, @Nullable Consumer<Player> onInteract) {
        GameProfile profile = new GameProfile(createUniqueUUID(), displayName);

        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();

        NPC npc = new NPC(
                minecraftServer,
                serverLevel,
                profile,
                ClientInformation.createDefault(),
                onInteract
        );

        npc.setPos(location.getX(), location.getY(), location.getZ());

        if (skinName != null) {
            NpcSkinDataAccess.load(plugin)
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return new HashMap<>();
                    })
                    .thenAccept(skinDataResult -> {
                        NpcSkin npcSkin = skinDataResult.get(skinName);
                        if (npcSkin != null) {
                            profile.getProperties().put(TEXTURES_PROPERTY, new Property(
                                    TEXTURES_PROPERTY,
                                    npcSkin.texture(),
                                    npcSkin.signature()
                            ));
                        }
                    });
        }

        NpcManager.registerNpc(npc);

        return npc.getId();
    }

    @Override
    public void setNpcRotation(int npcId, float xRot, float yRot, float yHeadRot) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error setting NPC rotation: NPC is null");
            return;
        }

        npc.setXRot(xRot);
        npc.setYRot(yRot);
        npc.setYHeadRot(yHeadRot);
    }

    @Override
    public void npcEquipItem(int npcId, EquipmentSlot equipmentSlot, Material item) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error equipping item to NPC: NPC is null");
            return;
        }

        net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(equipmentSlot);
        ItemStack nmsItemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(item));
        npc.equipItem(nmsSlot, nmsItemStack);
    }

    @Override
    public void npcUnequipItem(int npcId, EquipmentSlot equipmentSlot) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error removing item from NPC: NPC is null");
            return;
        }

        net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(equipmentSlot);
        npc.equipItem(nmsSlot, ItemStack.EMPTY);
    }

    @Override
    public void showNpcTo(int npcId, Player viewer) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error sending packets to show NPC: NPC is null");
            return;
        }
        try {

            List<SynchedEntityData.DataValue<?>> dataValuesCopy = new ArrayList<>(npc.getEntityData().getNonDefaultValues());
            SynchedEntityData.DataValue<Byte> outerSkinLayersValue =
                    new SynchedEntityData.DataValue<>(17, EntityDataSerializers.BYTE, (byte) 0x7F);
            dataValuesCopy.add(outerSkinLayersValue);

            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(
                    ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY
            );

            ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(
                    npc.getUUID(),
                    npc.getGameProfile(),
                    true,
                    0,
                    GameType.SURVIVAL,
                    null,
                    true,
                    0,
                    null
            );

            ServerPlayer serverPlayer = ((CraftPlayer) viewer).getHandle();
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            ClientboundPlayerInfoUpdatePacket infoUpdatePacket = makePlayerInfoUpdatePacket(actions, List.of(entry));

            connection.send(infoUpdatePacket);
            connection.send(new ClientboundAddEntityPacket(npc, 0, npc.blockPosition()));
            connection.send(new ClientboundSetEntityDataPacket(npcId, dataValuesCopy));

            if (!npc.getEquipmentList().isEmpty()) {
                connection.send(new ClientboundSetEquipmentPacket(npcId, npc.getEquipmentList()));
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                connection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(npc.getUUID())));
            }, 20L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getNpcId(String npcName) {
        NPC npc = NpcManager.getByNpcName(npcName);
        if (npc == null) {
            System.err.println("Error retrieving NPC ID: NPC is null");
            return 0;
        }
        return npc.getId();
    }

    @Override
    public String getNpcName(int npcId) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error getting NPC name: NPC is null");
            return null;
        }
        return npc.getNpcName();
    }

    @Override
    public List<Integer> getNpcsInWorld(String worldName) {
        return NpcManager.getNpcsByWorld(worldName);
    }

    @Override
    public boolean removeNpc(int npcId) {
        NpcManager.unregisterNpc(npcId);
        NPC npc = NpcManager.getById(npcId);
        return (npc == null);
    }

    @Override
    public boolean removeAllNpcsInWorld(String worldName) {
        return NpcManager.unregisterNpcsByWorld(worldName);
    }

    @Override
    public void setNpcInteraction(int npcId, Consumer<Player> onInteract) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error setting interaction for NPC: NPC is null");
            return;
        }
        npc.setOnInteract(onInteract);
    }

    private UUID createUniqueUUID() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (NpcManager.getAllUuids().contains(uuid) || Bukkit.getPlayer(uuid) != null);

        return uuid;
    }

    private ClientboundPlayerInfoUpdatePacket makePlayerInfoUpdatePacket(
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions,
            List<ClientboundPlayerInfoUpdatePacket.Entry> entries
    ) throws Exception {

        Unsafe unsafe = getUnsafe();
        ClientboundPlayerInfoUpdatePacket packet =
                (ClientboundPlayerInfoUpdatePacket) unsafe.allocateInstance(ClientboundPlayerInfoUpdatePacket.class);

        Field actionsField = null;
        Field entriesField = null;
        for (Field field : ClientboundPlayerInfoUpdatePacket.class.getDeclaredFields()) {
            if (field.getType() == EnumSet.class) {
                actionsField = field;
            }
            if (field.getType() == List.class) {
                entriesField = field;
            }
        }

        actionsField.setAccessible(true);
        entriesField.setAccessible(true);

        actionsField.set(packet, actions);
        entriesField.set(packet, entries);

        return packet;
    }

    private static Unsafe getUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
