package com.steve.megaNPCs.core.network;

import com.steve.megaNPCs.core.npc.NPC;
import com.steve.megaNPCs.core.npc.NpcManager;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class PacketHandler extends ChannelDuplexHandler {

    private Plugin plugin;
    private long lastCallTime = 0;
    private static final long COOLDOWN_MILLIS = 200;
    private final Player player;
    private static final Field ID_FIELD;

    static {
        try {
            Field idField = null;
            for (Field field : ServerboundInteractPacket.class.getDeclaredFields()) {
                if (field.getType() == int.class) {
                    idField = field;
                }
            }
            if (idField == null) {
                throw new NoSuchFieldException("No field of type 'int' found in ServerboundInteractPacket");
            }
            idField.setAccessible(true);
            ID_FIELD = idField;
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public PacketHandler(Player player, Plugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {

        if (msg instanceof ServerboundInteractPacket packet) {
            int id = (int) ID_FIELD.get(packet);
            NPC npc = NpcManager.getById(id);
            if (npc != null) {
                long now = System.currentTimeMillis();
                if (now - lastCallTime >= COOLDOWN_MILLIS) {
                    lastCallTime = now;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        npc.interact(player);
                    });
                }
            }
        }
        super.channelRead(context, msg);
    }

    @Override
    public void write(ChannelHandlerContext context, Object msg, ChannelPromise promise) throws Exception {
        super.write(context, msg, promise);
    }

}
