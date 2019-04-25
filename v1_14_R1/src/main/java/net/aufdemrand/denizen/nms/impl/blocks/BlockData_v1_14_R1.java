package net.aufdemrand.denizen.nms.impl.blocks;

import net.aufdemrand.denizen.nms.abstracts.ModernBlockData;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_14_R1;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTagBuilder;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.TileEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

public class BlockData_v1_14_R1 implements BlockData {

    private org.bukkit.block.data.BlockData blockData;

    public BlockData_v1_14_R1() {
    }

    public BlockData_v1_14_R1(Material mat, byte dat) {
        if (!mat.isLegacy()) {
            if (dat == 0) {
                blockData = Bukkit.createBlockData(mat);
                return;
            }
            mat = Bukkit.getUnsafe().toLegacy(mat);
        }
        blockData = Bukkit.getUnsafe().fromLegacy(mat, dat);
    }

    public BlockData_v1_14_R1(org.bukkit.block.data.BlockData data) {
        this.blockData = data;
        // TODO: ctag?
    }

    public BlockData_v1_14_R1(Block block) {
        blockData = block.getBlockData();
        TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (te != null) {
            NBTTagCompound compound = new NBTTagCompound();
            te.save(compound);
            ctag = (CompoundTag_v1_14_R1) CompoundTag_v1_14_R1.fromNMSTag(compound);
        }
    }

    public void setBlock(Block block, boolean physics) {
        block.setBlockData(blockData, physics);
        if (ctag != null) {
            CompoundTagBuilder builder = ctag.createBuilder();
            builder.putInt("x", block.getX());
            builder.putInt("y", block.getY());
            builder.putInt("z", block.getZ());
            ctag = (CompoundTag_v1_14_R1) builder.build();
            // TODO: make this work!
            BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
            TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(blockPos);
            te.load(ctag.toNMSTag());
        }
    }

    public String toCompressedFormat() {
        return "{" + blockData.getMaterial().name() + "}";
    }

    public static BlockData fromCompressedString(String str) {
        BlockData_v1_14_R1 data = new BlockData_v1_14_R1();
        data.blockData = Bukkit.createBlockData(Material.getMaterial(str));
        return data;
    }

    CompoundTag_v1_14_R1 ctag = null;

    @Override
    public CompoundTag getCompoundTag() {
        return ctag;
    }

    @Override
    public void setCompoundTag(CompoundTag tag) {
        ctag = (CompoundTag_v1_14_R1) tag;
    }

    @Override
    public Material getMaterial() {
        return blockData.getMaterial();
    }

    @Override
    public void setMaterial(Material material) {
        blockData = Bukkit.createBlockData(material);
    }

    @Override
    public byte getData() {
        return 0;
    }

    @Override
    public void setData(byte data) {
        // no
    }

    @Override
    public ModernBlockData modern() {
        return new ModernBlockData(blockData);
    }
}
