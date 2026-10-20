# MegaNPCs

## An NPC Plugin + API for my Mega Randomizer Spigot Server

### I plan to write a proper README later, but for now, I have pasted my project overview notes:

Fairly early in the development process of Skyblock (which was the first game I made for my server) I made the decision to try and build as much of the server's functionality myself as possible, rather than relying on a bunch of other 3rd party plugins. In fact, WorldEdit is the only 3rd party plugin that my Skyblock plugin/game actually requires (aside from what WorldEdit itself requires).
While I initially used a 3rd party NPC plugin, I chose to remove it and create all the NPC functionality myself.

This turned out to be a much bigger rabbit hole than I expected…
(See some of my notes in the "Problems and Challenges" section)

It became clear early on that the NPCs ideally should not be normal "Player" objects that are actually in the server, but rather, it's better to just send packets to the users.

Since there are a number of different packets that are required for this, I decided it would be better to have it all managed by its own standalone plugin, along with an API that my other plugins can import and use. I call this plugin MegaNPCs.

As mentioned elsewhere in my notes, this was my first attempt at making a plugin/api combination, so I built most of it partly "blind"; I didn't directly test any of it until it was pretty much completed, because I hadn't fully figured out yet how to host the API publicly so I could import it in my other plugins.

- Instead, for each piece of it, I built "mockup" code in another plugin that I would trigger with things like block break events to test out the different pieces.

One of the features of MegaNPCs is that it detects user interactions with NPCs by attaching packet listeners to each player as they join the server (and removing them when they leave) that filter for `ServerboundInteractPacket`s and match up the packet's id field with the registered NPCs.

--> This is one of the things that is nice to have managed by a standalone plugin/API because consuming plugins don't have to worry about this logic at all. They just define the interaction behavior (with a callback method) and/or listen for the NpcInteract Event.

My `NpcManager` class maintains a registry of NPCs and what Minecraft Worlds they are in.

- That way, all of the NPC objects can live in in one place, and MegaNPCs takes care of loading, unloading, maintaining any state, and cleaning up.
    - On the API side,  I have an `NpcService` interface that exposes methods for consuming plugins to create, manipulate, and remove NPCs.
    - The `NPC` class is not exposed publicly; consuming plugins just reference the NPCs by their UUID. For example, `setNpcPosition(UUID npcId, double x, double y, double z)`, etc.

As I alluded to earlier, some of the features of MegaNPCs were pretty tedious and fiddly to figure out. For example, working out how to consistently get the NPCs to face the direction I want was a bit tricky, especially when the NPC is placed in the center of a block instead of the origin (corner).

For displaying lines of text above the NPCs' heads, I settled on a system that allows the use of TextDisplays, invisible ArmorStands, or a combination of both. TextDisplays are nice for displaying larger font, but ArmorStands are the more traditional approach, so I wanted to make sure I had a good grasp of how to do either.

For the skins of the NPCs, I created a few custom skins and set them to my Minecraft username, then used Mojang's API to get the JSON of the skin data (the "textures" and "signature" values). I then copied them into a JSON file that I keep in the parent/common directory of MegaRandomizer Server so all the servers using MegaNPCs can access it. The JSON file's values are loaded at server startup so that they are ready to go when any NPC needs them.

One of the problems I encountered was that when the server doesn't get shut down properly, it can leave behind extra ArmorStands and TextDisplays in the world that are no longer tied to an NPC instance. This means that if you were to move the NPC, or remove it completely, the text lines will still be there! I call these left-behind ArmorStands/TextDisplays "orphans".

-->  I realized that even if I took care of some edge cases that were causing them to get orphaned, I would still inevitably end up with some orphans, especially from cases where a world or server crashed or didn't get shut down correctly.

- So I decided to add an "Orphan cleaner" system to MegaNPCs that would find and remove orphans for me. I created a command to run this manually, but more importantly, I added it to the API so that consuming plugins can trigger it at certain times, such as world loading, or even schedule it to run automatically if necessary.

In the end, I am very pleased with MegaNPCs. I feel like I made it in a way that gives plugins that use it all the control they need, while still abstracting away all the stuff they shouldn't have to worry about.
As time has gone on, I have slowly found and fixed little bugs with it, so it's something I come back to from time to time, but mostly just does all its magic in the background without me having to worry about it :)
