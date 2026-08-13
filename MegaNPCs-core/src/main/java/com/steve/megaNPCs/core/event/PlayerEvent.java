package com.steve.megaNPCs.core.event;

import com.steve.megaNPCs.core.network.PacketListenerInjector;
import com.steve.megaNPCs.core.npc.NpcManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerEvent implements Listener {

    private Plugin plugin;

    public PlayerEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PacketListenerInjector.inject(event.getPlayer(), plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PacketListenerInjector.eject(event.getPlayer());
        NpcManager.removePlayerFromAllNpcShownTo(event.getPlayer().getUniqueId());
    }
}
