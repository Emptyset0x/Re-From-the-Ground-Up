package rftgumod.common.item;

import net.minecraft.item.Item;
import rftgumod.common.Content;

public class ItemParchmentIdea extends Item {

    public ItemParchmentIdea(String name) {
        setTranslationKey(name);
        setMaxStackSize(1);
        setContainerItem(Content.i_parchmentEmpty);
    }

}