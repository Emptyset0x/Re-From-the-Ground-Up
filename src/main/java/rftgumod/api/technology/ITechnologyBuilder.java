package rftgumod.api.technology;

import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import rftgumod.api.technology.recipe.IIdeaRecipe;
import rftgumod.api.technology.recipe.IResearchRecipe;
import rftgumod.api.technology.unlock.IUnlock;

import javax.annotation.Nullable;
import java.util.Map;

public interface ITechnologyBuilder {

    ITechnologyBuilder setParent(@Nullable ResourceLocation parent);

    ITechnologyBuilder setDisplayInfo(DisplayInfo display);

    ITechnologyBuilder setRewards(AdvancementRewards rewards);

    ITechnologyBuilder setCriteria(Map<String, Criterion> criteria, String[][] requirements);

    ITechnologyBuilder setResearchedAtStart(boolean start);

    ITechnologyBuilder setCanCopy(boolean copy);

    ITechnologyBuilder addUnlock(IUnlock... ingredients);

    ITechnologyBuilder setIdeaRecipe(IIdeaRecipe idea);

    ITechnologyBuilder setResearchRecipe(IResearchRecipe research);

    ITechnologyBuilder setGameStage(String stage);

    ITechnologyBuilder setChapter(ResourceLocation chapter);

    ITechnologyBuilder setSubtilte(ITextComponent subtilte);

    /**
     * If this {@code TechnologyBuilder} was built from an existing
     * {@code Technology}, that {@code Technology} will be modified.
     *
     * @throws NullPointerException If this {@code TechnologyBuilder} is not a copy
     *                              or if the parent does not exist
     * @see #build()
     */
    void save();

    /**
     * Builds a new {@code Technology}. All saves afterwards will then change it.
     *
     * @return A new {@code Technology}
     * @throws NullPointerException If the parent does not exist
     * @see #save()
     */
    ITechnology build();

}
