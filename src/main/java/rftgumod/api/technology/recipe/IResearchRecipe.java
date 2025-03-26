package rftgumod.api.technology.recipe;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import rftgumod.api.technology.ITechnology;
import rftgumod.api.util.BlockSerializable;
import rftgumod.api.util.JsonContextPublic;

import java.util.List;

public interface IResearchRecipe {
    /**
     * @param block     The new block that has been inspected
     * @param inspected The already inspected block listed on the magnifying glass
     * @return If the newly inspected block will help with researching this
     */
    boolean inspect(BlockSerializable block, List<BlockSerializable> inspected);

    IPuzzle createInstance();

    ITechnology getTechnology();

    void setTechnology(ITechnology tech);

    interface Factory<T extends IResearchRecipe> {

        T deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology);

    }
}
