package com.boydti.fawe.bukkit.listener;

import com.boydti.fawe.FaweCache;
import com.boydti.fawe.command.CFICommands;
import com.boydti.fawe.object.*;
import com.boydti.fawe.object.brush.visualization.VirtualWorld;
import com.boydti.fawe.util.SetQueue;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.Interaction;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The CFIPacketListener handles packets for editing the VirtualWorld
 * The generator is a virtual world which only the creator can see
 *  - The virtual world is displayed inside the current world
 *  - Block/Chunk/Movement packets need to be handled properly
 */
public class CFIPacketListener implements Listener, PacketListener {

    private final PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();

    private final RunnableVal3<Player, VirtualWorld, Vector> runnableBlockDig = new RunnableVal3<Player, VirtualWorld, Vector>() {
        @Override
        public void run(Player player, VirtualWorld gen, Vector pt) {
            try {
                if (!sendBlockChange(player, gen, pt, Interaction.HIT)) {
                    gen.setBlock(pt, EditSession.nullBlock);
                }
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
    };
    private final RunnableVal5<Player, InteractionHand, Vector3i, VirtualWorld, Vector> runnableBlockPlace = new RunnableVal5<Player, InteractionHand, Vector3i, VirtualWorld, Vector>() {
        @Override
        public void run(Player player, InteractionHand interactionHand, Vector3i pos, VirtualWorld gen, Vector pt) {
            try {
                PlayerInventory inv = player.getInventory();
                ItemStack hand = interactionHand == InteractionHand.MAIN_HAND ? inv.getItemInHand() : inv.getItemInOffHand();
                if (hand != null && hand.getType().isBlock() && hand.getTypeId() != 0) {
                    BaseBlock block = FaweCache.getBlock(hand.getTypeId(), hand.getDurability());
                    gen.setBlock(pt, block);
                } else {
                    pt = getRelPos(pos, gen);
                    sendBlockChange(player, gen, pt, Interaction.OPEN);
                }
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
    };
    private final Runnable runnableDoNothing = () -> {};

    public CFIPacketListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        VirtualWorld gen = getGenerator(player);
        if (gen != null) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to.getWorld().equals(from.getWorld()) && to.distanceSquared(from) < 8) {
                event.setTo(player.getLocation());
                event.setCancelled(true);
                player.setVelocity(player.getVelocity());
            }
        }
    }

    private boolean sendBlockChange(Player plr, VirtualWorld gen, Vector pt, Interaction action) {
        PlatformManager platform = WorldEdit.getInstance().getPlatformManager();
        com.sk89q.worldedit.entity.Player actor = FawePlayer.wrap(plr).getPlayer();
        com.sk89q.worldedit.util.Location location = new com.sk89q.worldedit.util.Location(actor.getWorld(), pt);
        BlockInteractEvent toCall = new BlockInteractEvent(actor, location, action);
        platform.handleBlockInteract(toCall);
        if (toCall.isCancelled() || action == Interaction.OPEN) {
            Vector realPos = pt.add(gen.getOrigin());
            BaseBlock block = gen.getBlock(pt);
            sendBlockChange(plr, realPos, block);
            return true;
        }
        return false;
    }

    private void sendBlockChange(Player plr, Vector pt, BaseBlock block) {
        WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(new Vector3i(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()), block.getId());
        // Block position
        // block combined id
        this.playerManager.sendPacket(plr, wrapper);
    }

    private VirtualWorld getGenerator(Player player) {
        FawePlayer<Object> fp = FawePlayer.wrap(player);
        VirtualWorld vw = fp.getSession().getVirtualWorld();
        if (vw != null) return vw;
        CFICommands.CFISettings settings = fp.getMeta("CFISettings");
        if (settings != null && settings.hasGenerator() && settings.getGenerator().hasPacketViewer()) {
            return settings.getGenerator();
        }
        return null;
    }

    private Vector getRelPos(Vector3i position, VirtualWorld generator) {
        if (position == null) return null;
        Vector origin = generator.getOrigin();
        return new Vector(position.getX() - origin.getBlockX(), position.getY() - origin.getBlockY(), position.getZ() - origin.getBlockZ());
    }

    private boolean handleBlockEvent(Player player, InteractionHand hand, Vector3i position, BlockFace face, boolean relative, Runnable task) {
        VirtualWorld gen = getGenerator(player);
        if (gen != null && gen.isMutable()) {
            Vector pt = getRelPos(position, gen);
            if (pt != null) {
                if (relative) pt = getRelative(face, pt);
                if (gen.contains(pt)) {
                    if (task instanceof RunnableVal3<?,?,?>) {
                        RunnableVal3<Player, VirtualWorld, Vector> runnable = (RunnableVal3<Player, VirtualWorld, Vector>) task;
                        runnable.run(player, gen, pt);
                    } else if (task instanceof RunnableVal5<?,?,?,?,?>) {
                        RunnableVal5<Player, InteractionHand, Vector3i, VirtualWorld, Vector> runnable = (RunnableVal5<Player, InteractionHand, Vector3i, VirtualWorld, Vector>) task;
                        runnable.run(player, hand, position, gen, pt);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onPacketReceive(@NotNull PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            if (!this.handleBlockEvent(event.getPlayer(), null, wrapper.getBlockPosition(), wrapper.getBlockFace(), false, this.runnableBlockDig)) {
                event.setCancelled(true);
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
            if (!this.handleBlockEvent(event.getPlayer(), wrapper.getHand(), wrapper.getBlockPosition(), wrapper.getFace(), true, this.runnableBlockPlace)) {
                event.setCancelled(true);
            }
        } else if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            WrapperPlayClientUseItem wrapper = new WrapperPlayClientUseItem(event);
            if (!this.handleBlockEvent(event.getPlayer(), wrapper.getHand(), wrapper.readBlockPosition(), BlockFace.OTHER, true, this.runnableBlockPlace)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
            if (!this.handleBlockEvent(event.getPlayer(), null, wrapper.getBlockPosition(), BlockFace.OTHER, false, this.runnableDoNothing)) {
                event.setCancelled(true);
            }
        } else if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK_BULK) {
            WrapperPlayServerChunkDataBulk wrapper = new WrapperPlayServerChunkDataBulk(event);
            int[] chunksX = wrapper.getX();
            int[] chunksZ = wrapper.getZ();
            for (int i = 0; i < chunksX.length; i++) {
                VirtualWorld gen = this.getGenerator(event.getPlayer());
                if (gen != null) {
                    Vector origin = gen.getOrigin();
                    int cx = chunksX[i];
                    int cz = chunksZ[i];

                    int ocx = origin.getBlockX() >> 4;
                    int ocz = origin.getBlockZ() >> 4;

                    if (gen.contains(new Vector((cx - ocx) << 4, 0, (cz - ocz) << 4))) {
                        event.setCancelled(true);

                        Player plr = event.getPlayer();

                        FaweQueue queue = SetQueue.IMP.getNewQueue(plr.getWorld().getName(), true, false);

                        FaweChunk<?> toSend = gen.getSnapshot(cx - ocx, cz - ocz);
                        toSend.setLoc(gen, cx, cz);
                        queue.sendChunkUpdate(toSend, FawePlayer.wrap(plr));
                    }
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            Player player = event.getPlayer();
            VirtualWorld gen = this.getGenerator(event.getPlayer());
            if (gen != null) {
                WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
                Location pos = player.getLocation();
                Vector origin = gen.getOrigin();
                Vector pt = new Vector(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
                Vector3i velocity = wrapper.getVelocity().toVector3i();
                if (gen.contains(pt.subtract(origin)) && velocity.getX() == 0 && velocity.getY() == 0 && velocity.getZ() == 0) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            Player player = event.getPlayer();
            VirtualWorld gen = getGenerator(player);
            if (gen != null) {
                WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);
                Location pos = player.getLocation();
                Vector origin = gen.getOrigin();
                Vector from = new Vector(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
                Vector to = new Vector(wrapper.getX(), wrapper.getY(), wrapper.getZ());
                if (gen.contains(to.subtract(origin)) && from.distanceSq(to) < 8) {
                    WrapperPlayClientTeleportConfirm reply = new WrapperPlayClientTeleportConfirm(player.getEntityId());
                    this.playerManager.receivePacket(player, reply);
                    event.setCancelled(true);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            VirtualWorld gen = getGenerator(event.getPlayer());
            if (gen != null) {
                WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(event);
                Vector3i chunk = wrapper.getChunkPosition();
                Vector origin = gen.getOrigin();
                int cx = chunk.getX() - (origin.getBlockX() >> 4);
                int cz = chunk.getZ() - (origin.getBlockX() >> 4);
                if (gen.contains(new Vector(cx << 4, 0, cz << 4))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private Vector getRelative(BlockFace face, Vector pt) {
        if (face == null) return pt;
        switch (face.ordinal()) {
            case 0: return pt.add(0, -1, 0);
            case 1: return pt.add(0, 1, 0);
            case 2: return pt.add(0, 0, -1);
            case 3: return pt.add(0, 0, 1);
            case 4: return pt.add(-1, 0, 0);
            case 5: return pt.add(1, 0, 0);
            default: return pt;
        }
    }
}
