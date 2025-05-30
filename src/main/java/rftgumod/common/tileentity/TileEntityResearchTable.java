package rftgumod.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import rftgumod.api.technology.recipe.IPuzzle;
import rftgumod.common.Content;
import rftgumod.common.inventory.ContainerResearchTable;
import rftgumod.common.technology.Technology;
import rftgumod.util.StackUtils;

import javax.annotation.Nullable;

public class TileEntityResearchTable extends TileEntityInventory {

    public IPuzzle puzzle;

    public TileEntityResearchTable() {
        super(3, Content.n_researchTable);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        if (puzzle != null)
            compound.setTag("Puzzle", puzzle.write());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (puzzle == null) {
            Technology tech = StackUtils.INSTANCE.getTechnology(getStackInSlot(1));
            if (tech != null && tech.hasResearchRecipe())
                puzzle = tech.getResearchRecipe().createInstance();
        }
        if (puzzle != null && compound.hasKey("Puzzle"))
            puzzle.read(compound.getTag("Puzzle"));
    }

    @Nullable
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
        return new ContainerResearchTable(this, inventory);
    }

}
