package rftgumod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import rftgumod.common.inventory.ContainerIdeaTable;
import rftgumod.common.inventory.ContainerResearchTable;
import rftgumod.common.tileentity.TileEntityIdeaTable;
import rftgumod.common.tileentity.TileEntityResearchTable;
import rftgumod.common.technology.TechnologyManager;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity != null) {
            if (id == TechnologyManager.GUI.IDEATABLE.ordinal()) {
                return new ContainerIdeaTable((TileEntityIdeaTable) tileEntity, player.inventory);
            } else if (id == TechnologyManager.GUI.RESEARCHTABLE.ordinal()) {
                return new ContainerResearchTable((TileEntityResearchTable) tileEntity, player.inventory);
            }
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

}
