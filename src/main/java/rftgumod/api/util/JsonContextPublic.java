package rftgumod.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import rftgumod.api.RFTGUAPI;
import rftgumod.api.util.predicate.ItemPredicate;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JsonContextPublic extends JsonContext {

    private final Map<String, ItemPredicate> constants = new HashMap<>();

    public JsonContextPublic(String modId) {
        super(modId);
    }

    @Nullable
    @Override
    public ItemPredicate getConstant(String name) {
        return constants.get(name);
    }

    public void loadConstants(JsonObject[] jsons) {
        for (JsonObject json : jsons) {
            if (json.has("conditions") && !CraftingHelper.processConditions(json.getAsJsonArray("conditions"), this))
                continue;
            if (!json.has("ingredient"))
                throw new JsonSyntaxException("Constant entry must contain 'ingredient' value");
            constants.put(JsonUtils.getString(json, "name"),
                    RFTGUAPI.stackUtils.getItemPredicate(json.get("ingredient"), this));
        }
    }

}

