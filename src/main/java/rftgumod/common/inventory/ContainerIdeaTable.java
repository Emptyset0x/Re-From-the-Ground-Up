package rftgumod.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import rftgumod.api.inventory.InventoryCraftingPersistent;
import rftgumod.api.inventory.SlotSpecial;
import rftgumod.api.util.IStackUtils;
import rftgumod.common.Content;
import rftgumod.common.packet.PacketDispatcher;
import rftgumod.common.packet.client.TechnologyMessage;
import rftgumod.common.technology.Technology;
import rftgumod.common.technology.TechnologyManager;
import rftgumod.common.tileentity.TileEntityInventory;
import rftgumod.util.StackUtils;

import java.util.List;
import java.util.function.Predicate;

public class ContainerIdeaTable extends Container {

    private final TileEntityInventory invInput;

    private final InventoryCrafting craftMatrix;
    private final InventoryPlayer invPlayer;

    private final int sizeInventory;

    private int featherSlot;
    private int parchmentSlot;
    private int combinedSlots;
    private int outputSlot;

    private NonNullList<ItemStack> remaining;

    public ContainerIdeaTable(TileEntityInventory tileEntity, InventoryPlayer invPlayer) {
        this.invInput = tileEntity;
        this.invPlayer = invPlayer;
        if (!invPlayer.player.world.isRemote)
            PacketDispatcher.sendTo(new TechnologyMessage(invPlayer.player, false), (EntityPlayerMP) invPlayer.player);

        sizeInventory = addSlots(tileEntity);

        for (int slotx = 0; slotx < 3; slotx++) {
            for (int sloty = 0; sloty < 9; sloty++) {
                addSlotToContainer(new Slot(invPlayer, sloty + slotx * 9 + 9, 8 + sloty * 18, 84 + slotx * 18));
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            addSlotToContainer(new Slot(invPlayer, slot, 8 + slot * 18, 142));
        }

        craftMatrix = new InventoryCraftingPersistent(tileEntity, combinedSlots, 3, 1);
        onCraftMatrixChanged(invInput);
    }

    @Override
    public void setAll(List<ItemStack> stacks) {
        super.setAll(stacks);
        onCraftMatrixChanged(invInput);
    }

    private int addSlots(TileEntityInventory tileEntity) {
        int index = 0;

        addSlotToContainer(new SlotSpecial(tileEntity, index, 37, 23, 1, OreDictionary.getOres("feather")));
        featherSlot = index;
        index++;

        addSlotToContainer(new SlotSpecial(tileEntity, index, 59, 23, 64, new ItemStack(Content.i_parchmentEmpty)));
        parchmentSlot = index;
        index++;

        combinedSlots = index;
        for (int slot = 0; slot < 3; slot++) {
            addSlotToContainer(new SlotSpecial(tileEntity, index, 30 + slot * 18, 45, 1, (Predicate<ItemStack>) null));
            index++;
        }

        addSlotToContainer(new Slot(new InventoryCraftResult(), index, 124, 35));
        outputSlot = index;
        index++;

        return index;
    }

    private Technology hasRecipe() {
        for (Technology tech : TechnologyManager.INSTANCE) {
            if (tech.hasIdeaRecipe() && tech.canResearch(invPlayer.player)) {
                remaining = tech.getIdeaRecipe().test(craftMatrix);
                if (remaining != null)
                    return tech;
            }
        }
        return null;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv) {
        if (inv == invInput) {
            if (inventorySlots.get(featherSlot).getHasStack() && inventorySlots.get(parchmentSlot).getHasStack()) {
                Technology tech = hasRecipe();

                if (tech != null) {
                    inventorySlots.get(outputSlot).putStack(StackUtils.INSTANCE.getParchment(tech,
                            tech.hasResearchRecipe() ? IStackUtils.Parchment.IDEA : IStackUtils.Parchment.RESEARCH));
                    return;
                }
            }
            inventorySlots.get(outputSlot).putStack(ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack slotClick(int index, int mouse, ClickType mode, EntityPlayer player) {
        if (index == outputSlot && inventorySlots.get(outputSlot).getHasStack()) {
            inventorySlots.get(parchmentSlot).decrStackSize(1);
            for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
                craftMatrix.setInventorySlotContents(i, remaining.get(i));
        }

        ItemStack clickItemStack = super.slotClick(index, mouse, mode, player);
        onCraftMatrixChanged(invInput);
        return clickItemStack;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
        ItemStack itemStack1 = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack1 = itemStack2.copy();

            if (slotIndex == outputSlot) {
                if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, true))
                    return ItemStack.EMPTY;
            } else if (slotIndex > outputSlot) {
                if (itemStack2.getItem() == Content.i_parchmentEmpty) {
                    if (!mergeItemStack(itemStack2, parchmentSlot, parchmentSlot + 1, false))
                        return ItemStack.EMPTY;
                } else if (ArrayUtils.contains(OreDictionary.getOreIDs(itemStack2),
                        OreDictionary.getOreID("feather"))) {
                    if (!mergeItemStack(itemStack2, featherSlot, featherSlot + 1, false))
                        return ItemStack.EMPTY;

                } else if (!mergeItemStack(itemStack2, combinedSlots, combinedSlots + 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, false))
                return ItemStack.EMPTY;

            if (itemStack2.getCount() != 0)
                slot.onSlotChanged();

            if (itemStack2.getCount() == itemStack1.getCount())
                return ItemStack.EMPTY;

            slot.onTake(playerIn, itemStack2);
        }

        return itemStack1;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

}