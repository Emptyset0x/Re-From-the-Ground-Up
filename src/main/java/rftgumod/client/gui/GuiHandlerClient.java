package rftgumod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rftgumod.common.technology.TechnologyManager;
import rftgumod.common.tileentity.TileEntityIdeaTable;
import rftgumod.common.tileentity.TileEntityResearchTable;

@SideOnly(Side.CLIENT)
public class GuiHandlerClient extends GuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity != null) {
            if (id == TechnologyManager.GUI.IDEATABLE.ordinal()) {
                return new GuiIdeaTable(player.inventory, (TileEntityIdeaTable) tileEntity);
            } else if (id == TechnologyManager.GUI.RESEARCHTABLE.ordinal()) {
                return new GuiResearchTable(player.inventory, (TileEntityResearchTable) tileEntity);
            }
        }

        return null;
    }

}

