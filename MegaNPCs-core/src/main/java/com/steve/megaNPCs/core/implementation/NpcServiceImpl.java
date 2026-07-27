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
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class NpcServiceImpl implements NpcService {

    private Plugin plugin;
    private static final String TEXTURES_PROPERTY = "textures";

    private static final NpcSkin DEFAULT_SKIN = new NpcSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTc4NDI2OTI2NTE1NywKICAicHJvZmlsZUlkIiA6ICIzOGU1NmZlZjg4NjQ0NzBkYWZhNDM1OTExOTk3M2UyYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzdGV2ZWZhbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xYTRhZjcxODQ1NWQ0YWFiNTI4ZTdhNjFmODZmYTI1ZTZhMzY5ZDE3NjhkY2IxM2Y3ZGYzMTlhNzEzZWI4MTBiIgogICAgfQogIH0KfQ==",
            "EMn77YzJ/Du0dg9EnataOVERmCsUsIs1ImdzaQehe537vZYycY1WXlLdW4zBNk8Hep+FJPXWMaXNal84ZZk/2OhtDzT9OnWN1JDGzDG3qvLF7rgmGNifIQh6CUlNmjGajkwlhwcSfL+RZfET+IsQ/MeoyikzACb7SYR+hwtIHWEEl84rxLzYBj9gqTfJZfUleFHBAKE04w2Ld6WYGUuO9ZvEauqemD4C8K3WxJOtxQjkRaJHUJ7JOH74Ua9Egb0PcK+jblMd/CKhyBShh86HVdaTwwoClO956DQ98d23J7i+zSOKjBDRkaXXoX2vW/Yl+WJb95p8ABwdE+151fLPWmnu6LhtiXGdxHms6aEvRVD/fLMatpb3FkU0+r8T3SpYbVN03jF8s44eq78AhoSMHHuGgKSnjP7j+KfbhiwgGPL51eW3N6GGnIiE8ADECdjc9safP59w/wRK0PJS3jph02zqtIM1imeu/sY5xouQHdnjR8l0tG5T4lUjuEHKK4uNYaqQfEwVJHYdV7GRAjc58Wm6fRNrfa270mxFJ9xz0mhHpbIhI4mSyw1htcWftBYcm4Vfy4+7B/Ta2JSqZr/L2hGLIQGt5ipSITfa6pBynlyyCOwJqAzxTThrlBOz2A7R1THgaqBTUTl4UwLKB5uOLBa4W3qn1UHQL6Q8l1P2y0I="
    );

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
                        NpcSkin npcSkin = (skinDataResult.get(skinName) != null)
                                ? skinDataResult.get(skinName)
                                : DEFAULT_SKIN;

                        profile.getProperties().put(TEXTURES_PROPERTY, new Property(
                                TEXTURES_PROPERTY,
                                npcSkin.texture(),
                                npcSkin.signature()
                        ));
                    });
        }

        NpcManager.registerNpc(npc);

        return npc.getId();
    }

    @Override
    public void setNpcPosition(int npcId, double x, double y, double z) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error setting NPC position: NPC is null");
            return;
        }

        npc.setPos(x, y, z);

        sendLocationRotationUpdate(npc);
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

        sendLocationRotationUpdate(npc);
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

        sendEquipmentUpdate(npc);
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
        sendEquipmentUpdate(npc);
    }

    @Override
    public void showNpcTo(int npcId, Player viewer) {
        NPC npc = NpcManager.getById(npcId);
        if (npc == null) {
            System.err.println("Error sending packets to show NPC: NPC is null");
            return;
        }
        if (viewer == null) {
            System.err.println("Error sending packets to show NPC: viewing player is null");
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

            if (!npc.shownToPlayers.contains(viewer.getUniqueId())) {
                npc.shownToPlayers.add(viewer.getUniqueId());
            }

            sendLocationRotationUpdate(npc, serverPlayer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unShowNpcsTo(Player viewer, int... npcIds) {
        if (viewer == null) {
            System.err.println("Error sending packets to un-show NPC: viewing player is null");
            return;
        }
        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(npcIds));
        for (int npcid : npcIds) {
            NpcManager.getById(npcid).shownToPlayers.remove(viewer.getUniqueId());
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

    @Override
    public void removeAllNpcs() {
        NpcManager.unregisterAllNpcs();
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

    private static void sendEquipmentUpdate(NPC npc) {
        try {
            for (UUID uuid : npc.shownToPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
                    connection.send(new ClientboundSetEquipmentPacket(npc.getId(), npc.getEquipmentList()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendLocationRotationUpdate(NPC npc) {
        try {
            for (UUID uuid : npc.shownToPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
                    PositionMoveRotation positionMoveRotation = new PositionMoveRotation(npc.position(), Vec3.ZERO, npc.getYRot(), npc.getXRot());
                    ClientboundTeleportEntityPacket teleportPack = new ClientboundTeleportEntityPacket(
                            npc.getId(),
                            positionMoveRotation,
                            Collections.emptySet(),
                            true
                    );
                    connection.send(teleportPack);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendLocationRotationUpdate(NPC npc, @NotNull ServerPlayer serverPlayer) {
        ServerGamePacketListenerImpl connection = serverPlayer.connection;
        PositionMoveRotation positionMoveRotation = new PositionMoveRotation(npc.position(), Vec3.ZERO, npc.getYRot(), npc.getXRot());
        ClientboundTeleportEntityPacket teleportPack = new ClientboundTeleportEntityPacket(
                npc.getId(),
                positionMoveRotation,
                Collections.emptySet(),
                true
        );
        connection.send(teleportPack);
    }
}
