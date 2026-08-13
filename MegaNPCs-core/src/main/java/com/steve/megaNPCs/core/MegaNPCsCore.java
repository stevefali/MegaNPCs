package com.steve.megaNPCs.core;

import com.steve.MegaNPCs.api.NpcService;
import com.steve.megaNPCs.core.implementation.NpcServiceImpl;
import com.steve.megaNPCs.core.network.PacketListenerInjector;
import com.steve.megaNPCs.core.npc.NpcManager;
import com.steve.megaNPCs.core.npc.NpcSkinDataAccess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class MegaNPCsCore extends JavaPlugin {

    @Override
    public void onEnable() {

        NpcServiceImpl serviceImpl = new NpcServiceImpl(this);
        Bukkit.getServicesManager().register(
                NpcService.class, serviceImpl, this, ServicePriority.Normal
        );

        NpcSkinDataAccess.load(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketListenerInjector.inject(player, this);
        }

    }

    @Override
    public void onDisable() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketListenerInjector.eject(player);
        }
        HandlerList.unregisterAll();

        NpcManager.unregisterAllNpcs();

        try {

        } catch (Exception e) {
            System.out.println("Error removing NPCs: " + e.getMessage());
        }

        Bukkit.getServicesManager().unregisterAll(this);


    }
}
