package rftgumod.api.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class InventoryCraftingPersistent extends InventoryCrafting {

    private final IInventory parent;
    private final int offset;
    private final int size;

    public InventoryCraftingPersistent(IInventory parent, int offset, int width, int height) {
        super(null, width, height);

        this.parent = parent;
        this.offset = offset;
        this.size = width * height;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index) {
        return index < 0 || index >= size ? ItemStack.EMPTY : parent.getStackInSlot(index + offset);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return getStackInSlot(index + offset).splitStack(count);
    }

}
