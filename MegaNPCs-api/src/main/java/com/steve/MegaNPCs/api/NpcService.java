package com.steve.MegaNPCs.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface NpcService {

    /**
     * Create a new NPC. Once created, the NPC can be shown to players using {@link #showNpcTo(UUID, Player)}.
     *
     * @param location    The NPC's location.
     * @param displayName The name that will be displayed above the NPC's head.
     * @param skinName    The name associated with the skin data in {@code npc-skin.json}.
     * @param onInteract  The callback method that will run when a player interacts with the NPC.
     *                    **Note: Interacting with the NPC also fires an {@link com.steve.MegaNPCs.api.NpcInteractionEvent} event. If that event is cancelled, the {@code onInteract} callback method will NOT run.**
     * @return The ID of the newly created NPC.
     */
    UUID createNpc(Location location, String displayName, @Nullable String skinName, @Nullable Consumer<Player> onInteract);

    /**
     * Set/update the position of the NPC in the world
     *
     * @param npcId The id of the NPC.
     * @param x     The X-position of the NPC
     * @param y     The Y-position of the NPC
     * @param z     The Z-position of the NPC
     */
    void setNpcPosition(UUID npcId, double x, double y, double z);

    /**
     * Set/update the head and body rotation of the NPC.
     *
     * @param npcId    The id of the NPC.
     * @param xRot     The vertical rotation of the NPC's head.
     * @param yRot     The rotation of the NPC's body.
     * @param yHeadRot The horizontal rotation of the NPC's head.
     */
    void setNpcRotation(UUID npcId, float xRot, float yRot, float yHeadRot);

    /**
     * Set the NPC to look at the given location.
     * **Note: The NPC will look at where the eyes of a player would be
     * if they were standing at the given location.
     *
     * @param npcId          The id of the NPC.
     * @param playerLocation The location where the NPC will look (as if there were a player standing there)
     */
    void npcLookAt(UUID npcId, Location playerLocation);

    /**
     * Set the equipment slots (armor, items in hand, etc.) of the NPC.
     *
     * @param npcId         The id of the NPC.
     * @param equipmentSlot Where on the NPC to equip the item.
     * @param item          The item to equip.
     */
    void npcEquipItem(UUID npcId, EquipmentSlot equipmentSlot, Material item);

    /**
     * Remove an equipped item (armor, items in hand, etc.) from the NPC
     *
     * @param npcId         The id of the NPC.
     * @param equipmentSlot Where on the NPC to remove the item.
     */
    void npcUnequipItem(UUID npcId, EquipmentSlot equipmentSlot);

    /**
     * Show an NPC to a player.
     *
     * @param npcId  The id of the NPC.
     * @param viewer The player the NPC will be shown to.
     */
    void showNpcTo(UUID npcId, Player viewer);

    /**
     * Make NPCs disappear for a player. **Note that this does not remove the NPCs from the {@code NpcManager}.**
     *
     * @param viewer The player for whom the NPCs will disappear.
     * @param npcIds The IDs of the NPCs.
     */
    void unShowNpcsTo(Player viewer, UUID... npcIds);

    /**
     * Get the ID of the NPC with the provided name.
     *
     * @param npcName The internal name of the NPC. (Note: {@code npcName = displayName + "_" + worldName})
     * @return The NPC's internal name.
     */
    UUID getNpcId(String npcName);

    /**
     * Get the internal name of the NPC with the provided ID.
     *
     * @param npcId The id of the NPC.
     * @return The NPC's ID.
     */
    String getNpcName(UUID npcId);

    /**
     * Get a list of ID's of all the NPC's in the provided world.
     *
     * @param worldName The name of the world that the NPC's are in.
     * @return A list of NPC ID's.
     */
    List<UUID> getNpcsInWorld(String worldName);

    /**
     * Remove the NPC and no longer show it to any players.
     *
     * @param npcId The id of the NPC.
     * @return {@code true} if the NPC was successfully removed, {@code false} if the NPC was not successfully removed.
     */
    boolean removeNpc(UUID npcId);

    /**
     * Remove all the NPC's in the provided world.
     *
     * @param worldName The name of the world to remove the NPC's from.
     * @return {@code true} if all NPC's have been removed from the world; {@code false} if not.
     */
    boolean removeAllNpcsInWorld(String worldName);

    /**
     * Set the callback method that will run when a player interacts with the NPC.
     * **Note: Interacting with the NPC also fires an {@link com.steve.MegaNPCs.api.NpcInteractionEvent} event. If that event is cancelled, the callback method will NOT run.**
     *
     * @param npcId      The id of the NPC
     * @param onInteract The callback method for the interaction.
     *                   <p>
     *                   Example format of callback method: {@code player -> { player.sendMessage("Hey there!") }}
     */
    void setNpcInteraction(UUID npcId, Consumer<Player> onInteract);

    /**
     * Remove all registered NPC's. They will also be un-shown to any online players.
     *  ***Use this with CAUTION! This will remove the NPC's from all worlds on the server!***
     */
    void removeAllNpcs();

    /**
     * Set whether to show the NPC's default name
     *
     * @param npcId     The id of the NPC
     * @param isVisible {@code true} to show the name; {@code false} to hide the name.
     */
    void setDefaultNameVisible(UUID npcId, boolean isVisible);

    /**
     * Set the lines of text displayed above the NPC.
     *
     * @param npcId     The id of the NPC
     * @param nameLines One or more strings to display above the NPC (from lowest to highest)
     */
    void setNameLines(UUID npcId, String... nameLines);

    /**
     * Remove the lines of text displayed above the NPC.
     *
     * @param npcId The id of the NPC
     */
    void removeNameLines(UUID npcId);

    /**
     * Get the lines of text displayed above the NPC.
     *
     * @param npcId The id of the NPC
     * @return An array of the current name line strings
     */
    String[] getCurrentNameLines(UUID npcId);

    /**
     * Set a scalable title text for the NPC. The title can be multiline ('\n').
     * If the NPC already has a title, this will replace it.
     *
     * @param npcId     The id of the NPC
     * @param titleText The text to be displayed. Can be multiline using '\n'
     * @param scale     The scale of the text.
     */
    void setTitle(UUID npcId, String titleText, double scale);

    /**
     * Remove the title text of the NPC.
     *
     * @param npcId The id of the NPC
     */
    void removeTitle(UUID npcId);

}
