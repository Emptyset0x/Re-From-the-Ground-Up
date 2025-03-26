package rftgumod.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import rftgumod.common.Content;
import rftgumod.common.inventory.ContainerIdeaTable;

public class TileEntityIdeaTable extends TileEntityInventory {

    public TileEntityIdeaTable() {
        super(5, Content.n_ideaTable);
    }

    @Override
    public Container createContainer(InventoryPlayer arg0, EntityPlayer arg1) {
        return new ContainerIdeaTable(this, arg0);
    }

}

