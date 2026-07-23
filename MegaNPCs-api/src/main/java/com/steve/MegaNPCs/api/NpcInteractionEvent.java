package com.steve.MegaNPCs.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NpcInteractionEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final int npcId;
    private final String npcName;
    private final Player player;
    private boolean cancelled;

    public NpcInteractionEvent(int npcId, String npcName, Player player) {
        this.npcId = npcId;
        this.npcName = npcName;
        this.player = player;
    }

    public int getNpcId() {
        return npcId;
    }

    public String getNpcName() {
        return npcName;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
