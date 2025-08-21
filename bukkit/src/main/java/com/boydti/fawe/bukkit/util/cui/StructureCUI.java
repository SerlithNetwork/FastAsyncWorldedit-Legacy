package com.boydti.fawe.bukkit.util.cui;

import com.boydti.fawe.FaweCache;
import com.boydti.fawe.object.FawePlayer;
import com.boydti.fawe.util.cui.CUI;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class StructureCUI extends CUI {
    private boolean cuboid = true;

    private Vector pos1;
    private Vector pos2;

    private Vector remove;
    private NBTCompound removeTag;
    private int combined;

    private final PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();

    public StructureCUI(FawePlayer player) {
        super(player);
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        if (event instanceof SelectionShapeEvent) {
            clear();
            this.cuboid = event.getParameters()[0].equalsIgnoreCase("cuboid");
        } else if (cuboid && event instanceof SelectionPointEvent) {
            SelectionPointEvent spe = (SelectionPointEvent) event;
            String[] param = spe.getParameters();
            int id = Integer.parseInt(param[0]);
            int x = Integer.parseInt(param[1]);
            int y = Integer.parseInt(param[2]);
            int z = Integer.parseInt(param[3]);
            Vector pos = new Vector(x, y, z);
            if (id == 0) {
                pos1 = pos;
            } else {
                pos2 = pos;
            }
            update();
        }
    }

    private int viewDistance() {
        Player player = this.<Player>getPlayer().parent;
        if (Bukkit.getVersion().contains("paper")) {
            return player.getViewDistance();
        } else {
            return Bukkit.getViewDistance();
        }
    }

    public void clear() {
        pos1 = null;
        pos2 = null;
        update();
    }

    private NBTCompound constructStructureNbt(int x, int y, int z, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ) {
        NBTCompound tag = new NBTCompound();
        tag.setTag("name", new NBTString(UUID.randomUUID().toString()));
        tag.setTag("author", new NBTString("Empire92")); // :D
        tag.setTag("metadata", new NBTString(""));
        tag.setTag("x", new NBTInt(x));
        tag.setTag("y", new NBTInt(y));
        tag.setTag("z", new NBTInt(z));
        tag.setTag("posX", new NBTInt(posX));
        tag.setTag("posY", new NBTInt(posY));
        tag.setTag("posZ", new NBTInt(posZ));
        tag.setTag("sizeX", new NBTInt(sizeX));
        tag.setTag("sizeY", new NBTInt(sizeY));
        tag.setTag("sizeZ", new NBTInt(sizeZ));
        tag.setTag("rotation", new NBTString("NONE"));
        tag.setTag("mirror", new NBTString("NONE"));
        tag.setTag("mode", new NBTString("SAVE"));
        tag.setTag("ignoreEntities", new NBTByte(true));
        tag.setTag("powered", new NBTByte(false));
        tag.setTag("showair", new NBTByte(false));
        tag.setTag("showboundingbox", new NBTByte(true));
        tag.setTag("integrity", new NBTFloat(1.0f));
        tag.setTag("seed", new NBTInt(0));
        tag.setTag("id", new NBTString("minecraft:structure_block"));
        return tag;
    }

    private void sendOp() {
        Player player = this.<Player>getPlayer().parent;
        WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus(player.getEntityId(), (byte) 28);
        this.playerManager.sendPacket(player, packet);
    }

    private void sendNbt(Vector pos, NBTCompound compound) {
        Player player = this.<Player>getPlayer().parent;
        WrapperPlayServerBlockEntityData packet = new WrapperPlayServerBlockEntityData(new Vector3i(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()), BlockEntityTypes.BED, compound);
        this.playerManager.sendPacket(player, packet);
    }

    public synchronized void update() {
        Player player = this.<Player>getPlayer().parent;
        Location playerLoc = player.getLocation();
        boolean setOp = remove == null && !player.isOp();
        if (remove != null) {
            int cx = playerLoc.getBlockX() >> 4;
            int cz = playerLoc.getBlockZ() >> 4;
            int viewDistance = viewDistance();
            if (Math.abs(cx - (remove.getBlockX() >> 4)) <= viewDistance && Math.abs(cz - (remove.getBlockZ() >> 4)) <= viewDistance) {
                removeTag.setTag("sizeX", new NBTInt(0));
                sendNbt(remove, removeTag);
                Location removeLoc = new Location(player.getWorld(), remove.getX(), remove.getY(), remove.getZ());
                player.sendBlockChange(removeLoc, FaweCache.getId(combined), (byte) FaweCache.getData(combined));
            }
            remove = null;
        }
        if (pos1 == null || pos2 == null) return;
        Vector min = Vector.getMinimum(pos1, pos2);
        Vector max = Vector.getMaximum(pos1, pos2);

        // Position
        double rotX = playerLoc.getYaw();
        double rotY = playerLoc.getPitch();
        double xz = Math.cos(Math.toRadians(rotY));
        int x = (int) (playerLoc.getX() - (-xz * Math.sin(Math.toRadians(rotX))) * 12);
        int z = (int) (playerLoc.getZ() - (xz * Math.cos(Math.toRadians(rotX))) * 12);
        int y = Math.max(0, Math.min(Math.min(255, max.getBlockY() + 32), playerLoc.getBlockY() + 3));
        int minX = Math.max(Math.min(32, min.getBlockX() - x), -32);
        int maxX = Math.max(Math.min(32, max.getBlockX() - x + 1), -32);
        int minY = Math.max(Math.min(32, min.getBlockY() - y), -32);
        int maxY = Math.max(Math.min(32, max.getBlockY() - y + 1), -32);
        int minZ = Math.max(Math.min(32, min.getBlockZ() - z), -32);
        int maxZ = Math.max(Math.min(32, max.getBlockZ() - z + 1), -32);
        int sizeX = Math.min(32, maxX - minX);
        int sizeY = Math.min(32, maxY - minY);
        int sizeZ = Math.min(32, maxZ - minZ);
        if (sizeX == 0 || sizeY == 0 || sizeZ == 0) return;
        // maxX - 32;
        int posX = Math.max(minX, Math.min(16, maxX) - 32);
        int posY = Math.max(minY, Math.min(16, maxY) - 32);
        int posZ = Math.max(minZ, Math.min(16, maxZ) - 32);

        // NBT
        NBTCompound compound = constructStructureNbt(x, y, z, posX, posY, posZ, sizeX, sizeY, sizeZ);

        Block block = player.getWorld().getBlockAt(x, y, z);
        remove = new Vector(x, y, z);
        combined = FaweCache.getCombined(block.getTypeId(), block.getData());
        removeTag = compound;

        Location blockLoc = new Location(player.getWorld(), x, y, z);
        player.sendBlockChange(blockLoc, Material.STRUCTURE_BLOCK, (byte) 0);
        if (setOp) sendOp();
        sendNbt(remove, compound);
    }
}
