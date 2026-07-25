package com.steve.MegaNPCs.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public interface NpcService {

    /**
     * Create a new NPC. Once created, the NPC can be shown to players using {@link #showNpcTo(int, Player)}.
     *
     * @param location    The NPC's location.
     * @param displayName The name that will be displayed above the NPC's head.
     * @param skinName    The name associated with the skin data in {@code npc-skin.json}.
     * @param onInteract  The callback method that will run when a player interacts with the NPC.
     *                    **Note: Interacting with the NPC also fires an {@link com.steve.MegaNPCs.api.NpcInteractionEvent} event. If that event is cancelled, the {@code onInteract} callback method will NOT run.**
     * @return The ID of the newly created NPC.
     */
    int createNpc(Location location, String displayName, @Nullable String skinName, @Nullable Consumer<Player> onInteract);

    /**
     * Set the head and body rotation of the NPC.
     *  **Note: If the NPC has already been shown to players, you will have to call {@link #showNpcTo(int, Player)} again.**
     *
     * @param npcId    The id of the NPC.
     * @param xRot     The vertical rotation of the NPC's head.
     * @param yRot     The rotation of the NPC's body.
     * @param yHeadRot The horizontal rotation of the NPC's head.
     */
    void setNpcRotation(int npcId, float xRot, float yRot, float yHeadRot);

    /**
     * Set the equipment slots (armor, items in hand, etc.) of the NPC.
     *  **Note: If the NPC has already been shown to players, you will have to call {@link #showNpcTo(int, Player)} again.**
     * @param npcId         The id of the NPC.
     * @param equipmentSlot Where on the NPC to equip the item.
     * @param item          The item to equip.
     */
    void npcEquipItem(int npcId, EquipmentSlot equipmentSlot, Material item);

    /**
     * Remove an equipped item (armor, items in hand, etc.) from the NPC
     *  **Note: If the NPC has already been shown to players, you will have to call {@link #showNpcTo(int, Player)} again.**
     * @param npcId         The id of the NPC.
     * @param equipmentSlot Where on the NPC to remove the item.
     */
    void npcUnequipItem(int npcId, EquipmentSlot equipmentSlot);

    /**
     * Show an NPC to a player.
     *
     * @param npcId  The id of the NPC.
     * @param viewer The player the NPC will be shown to.
    */
    void showNpcTo(int npcId, Player viewer);

    /**
     * Get the ID of the NPC with the provided name.
     *
     * @param npcName The internal name of the NPC. (Note: {@code npcName = displayName + "_" + worldName})
     * @return The NPC's internal name.
     */
    int getNpcId(String npcName);

    /**
     * Get the internal name of the NPC with the provided ID.
     *
     * @param npcId The id of the NPC.
     * @return The NPC's ID.
     */
    String getNpcName(int npcId);

    /**
     * Get a list of ID's of all the NPC's in the provided world.
     *
     * @param worldName The name of the world that the NPC's are in.
     * @return A list of NPC ID's.
     */
    List<Integer> getNpcsInWorld(String worldName);

    /**
     * Remove the NPC and no longer show it to any players.
     *
     * @param npcId  The id of the NPC.
     * @return {@code true} if the NPC was successfully removed, {@code false} if the NPC was not successfully removed.
     */
    boolean removeNpc(int npcId);

    /**
     * Remove all the NPC's in the provided world.
     *
     * @param worldName The name of the world to remove the NPC's from.
     * @return {@code true} if all NPC's have been removed from the world; {@code false} if not.
     */
    boolean removeAllNpcsInWorld(String worldName);

    /**
     * Set the callback method that will run when a player interacts with the NPC.
     *  **Note: Interacting with the NPC also fires an {@link com.steve.MegaNPCs.api.NpcInteractionEvent} event. If that event is cancelled, the callback method will NOT run.**
     *
     * @param npcId The id of the NPC
     * @param onInteract The callback method for the interaction.
     *
     * Example format of callback method: {@code player -> { player.sendMessage("Hey there!") }}
     */
    void setNpcInteraction(int npcId, Consumer<Player> onInteract);

}
