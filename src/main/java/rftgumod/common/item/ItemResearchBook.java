package rftgumod.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rftgumod.RFTGU;
import rftgumod.common.packet.PacketDispatcher;
import rftgumod.common.packet.client.TechnologyMessage;

public class ItemResearchBook extends Item {

    public ItemResearchBook(String name) {
        setTranslationKey(name);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote)
            RFTGU.PROXY.openResearchBook(player);
        else
            PacketDispatcher.sendTo(new TechnologyMessage(player, false), (EntityPlayerMP) player);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

}
