package com.steve.megaNPCs.core.network;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class PacketListenerInjector {
    private static final String HANDLER_NAME_PREFIX = "packet_listener_";

    public static void inject(Player player, Plugin plugin) {
        String handlerName = HANDLER_NAME_PREFIX + player.getUniqueId();
        try {
            Channel channel = getChannel(player);
            if (channel == null) {
                return;
            }

            if (channel.pipeline().get(handlerName) != null) {
                return;
            }

            channel.pipeline().addBefore("packet_handler", handlerName, new PacketHandler(player, plugin));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void eject(Player player) {
        String handlerName = HANDLER_NAME_PREFIX + player.getUniqueId();
        Channel channel = getChannel(player);
        if (channel == null) {
            return;
        }

        if (channel.pipeline().get(handlerName) != null) {
            channel.eventLoop().execute(() -> {
                if (channel.pipeline().get(handlerName) != null) {
                    channel.pipeline().remove(handlerName);
                }
            });
        }

    }


    private static Channel getChannel(Player player) {
        try {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerGamePacketListenerImpl listenerImpl = serverPlayer.connection;

            Field field = ServerCommonPacketListenerImpl.class.getDeclaredField("e");
            field.setAccessible(true);
            Connection connection = (Connection) field.get(listenerImpl);

            return connection.channel;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
