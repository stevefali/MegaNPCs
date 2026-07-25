package com.steve.megaNPCs.core;

import com.steve.MegaNPCs.api.NpcService;
import com.steve.megaNPCs.core.implementation.NpcServiceImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class MegaNPCsCore extends JavaPlugin {

    @Override
    public void onEnable() {

        NpcServiceImpl serviceImpl = new NpcServiceImpl(this);
        Bukkit.getServicesManager().register(
                NpcService.class, serviceImpl, this, ServicePriority.Normal
        );

    }

    @Override
    public void onDisable() {

        Bukkit.getServicesManager().unregisterAll(this);
    }
}
