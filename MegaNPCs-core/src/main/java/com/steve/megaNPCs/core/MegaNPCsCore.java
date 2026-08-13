package com.steve.megaNPCs.core;

import com.steve.MegaNPCs.api.NpcService;
import com.steve.megaNPCs.core.event.PlayerEvent;
import com.steve.megaNPCs.core.implementation.NpcServiceImpl;
import com.steve.megaNPCs.core.network.PacketListenerInjector;
import com.steve.megaNPCs.core.npc.NpcManager;
import com.steve.megaNPCs.core.npc.NpcSkinDataAccess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class MegaNPCsCore extends JavaPlugin {

    private static final long LISTENER_CHECK_INTERVAL = 1200L;

    @Override
    public void onEnable() {

        NpcServiceImpl serviceImpl = new NpcServiceImpl(this);
        Bukkit.getServicesManager().register(
                NpcService.class, serviceImpl, this, ServicePriority.Normal
        );

        getServer().getPluginManager().registerEvents(new PlayerEvent(this), this);

        NpcSkinDataAccess.load(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketListenerInjector.inject(player, this);
        }

//        scheduleListenerCheck(this);
    }

    @Override
    public void onDisable() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketListenerInjector.eject(player);
        }
        HandlerList.unregisterAll(this);

        NpcManager.unregisterAllNpcs();

        Bukkit.getServicesManager().unregisterAll(this);
    }

    private static void scheduleListenerCheck(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            System.out.println("Listener check running");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!PacketListenerInjector.isListenerInjected(player)) {
                    System.out.println("Warning: Player " + player.getName() + "'s NPC interaction packet listener was not injected! (re-injecting now)");
                    PacketListenerInjector.inject(player, plugin);
                }
            }
        }, LISTENER_CHECK_INTERVAL, LISTENER_CHECK_INTERVAL);
    }


}
