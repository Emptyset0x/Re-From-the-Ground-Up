package rftgumod.api.technology.puzzle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import rftgumod.api.RFTGUAPI;
import rftgumod.api.technology.ITechnology;
import rftgumod.api.technology.recipe.IPuzzle;
import rftgumod.api.technology.recipe.IResearchRecipe;
import rftgumod.api.util.BlockSerializable;
import rftgumod.api.util.JsonContextPublic;
import rftgumod.api.util.predicate.ItemPredicate;

import java.util.List;

public class ResearchConnect implements IResearchRecipe {


    public final ItemPredicate left;
    public final ItemPredicate right;
    private ITechnology technology;

    public ResearchConnect(ItemPredicate left, ItemPredicate right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean inspect(BlockSerializable block, List<BlockSerializable> inspected) {
        return false;
    }

    @Override
    public IPuzzle createInstance() {
        return new PuzzleConnect(this);
    }

    @Override
    public ITechnology getTechnology() {
        return technology;
    }

    @Override
    public void setTechnology(ITechnology tech) {
        this.technology = tech;
    }

    public static class Factory implements IResearchRecipe.Factory<ResearchConnect> {

        private static ItemPredicate getStack(JsonElement json, String name, JsonContextPublic context) {
            JsonObject object;
            if (json.isJsonPrimitive()) {
                object = new JsonObject();
                object.addProperty("item", JsonUtils.getString(json, name));
                object.addProperty("data", 0);
            } else
                object = JsonUtils.getJsonObject(json, name);
            return RFTGUAPI.stackUtils.getItemPredicate(object, context);
        }

        @Override
        public ResearchConnect deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology) {
            JsonElement left = object.get("left");
            if (left == null)
                throw new JsonSyntaxException("Missing left, expected to find a string or a JsonObject");
            JsonElement right = object.get("right");
            if (right == null)
                throw new JsonSyntaxException("Missing right, expected to find a string or a JsonObject");
            return new ResearchConnect(getStack(left, "left", context), getStack(right, "right", context));
        }

    }
}
