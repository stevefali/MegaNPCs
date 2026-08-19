package com.steve.megaNPCs.core.event;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ArmorStandNamingEvent implements Listener {

    private static final String ADMIN_PERMISSION = "meganpcs.admin";

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandInteraction(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ItemStack item = event.getHand() == EquipmentSlot.HAND
                ? event.getPlayer().getInventory().getItemInMainHand()
                : event.getPlayer().getInventory().getItemInOffHand();

        if (item != null && item.getType() == Material.NAME_TAG) {
            if (!event.getPlayer().hasPermission(ADMIN_PERMISSION)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Sorry, naming Armor Stands is not allowed");
            }
        }
    }

}
