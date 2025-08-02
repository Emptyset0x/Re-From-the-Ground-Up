package rftgumod.common.technology;

import com.google.gson.*;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import rftgumod.api.util.JsonContextPublic;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class Chapter {

    private final Set<Technology> contents = new HashSet<>();
    private final ResourceLocation id;

    DisplayInfo display;

    Chapter(ResourceLocation id, DisplayInfo display) {
        this.id = id;
        this.display = display;
    }


    public ResourceLocation getRegistryName() {
        return id;
    }

    public DisplayInfo getDisplayInfo() {
        return display;
    }

    public static class Builder {

        private final DisplayInfo display;

        private Builder(DisplayInfo display) {
            this.display = display;

        }

        public Chapter build(ResourceLocation location, JsonContextPublic context) {
            Chapter c = new Chapter(location, display);
            return c;
        }
    }

    public static class Deserializer implements JsonDeserializer<Chapter.Builder> {
        @Override
        public Chapter.Builder deserialize(JsonElement element, Type ignore,
                                              JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonObject())
                throw new JsonSyntaxException("Expected Chapter to be an object");
            JsonObject json = element.getAsJsonObject();


            JsonObject displayObject = JsonUtils.getJsonObject(json, "display");
            DisplayInfo display = DisplayInfo.deserialize(displayObject, context);

            return new Builder(display);
        }

    }

}


