package rftgumod.common.compat.jei;

import mezz.jei.Internal;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Config;
import mezz.jei.recipes.RecipeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import rftgumod.api.technology.unlock.IUnlock;
import rftgumod.common.Content;
import rftgumod.common.config.RFTGUConfig;
import rftgumod.common.technology.Technology;
import rftgumod.common.technology.TechnologyManager;
import rftgumod.api.util.predicate.ItemPredicate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JEIPlugin
public class CompatJEI implements IModPlugin {

    private static IRecipeRegistry recipeRegistry;
    private static IIngredientRegistry ingredientRegistry;

    private static boolean cheatItemsEnabled = Config.isCheatItemsEnabled();

    private static List<ItemStack> lockedLast = new LinkedList<>();
    private static List<IRecipeWrapper> wrappers;

    @Override
    public void register(IModRegistry registry) {
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(Content.i_parchmentIdea));
        blacklist.addIngredientToBlacklist(new ItemStack(Content.i_parchmentResearch));

        ingredientRegistry = registry.getIngredientRegistry();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        recipeRegistry = runtime.getRecipeRegistry();
        wrappers = recipeRegistry.getRecipeWrappers(recipeRegistry.getRecipeCategory(VanillaRecipeCategoryUid.CRAFTING));
    }

    public static boolean refreshHiddenItems(boolean refreshCheatItems) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            Minecraft.getMinecraft().addScheduledTask(() -> refreshHiddenItems(refreshCheatItems));
            return true;
        }

        if (Minecraft.getMinecraft().player == null)
            return true;

        if (refreshCheatItems) {
            if (cheatItemsEnabled != Config.isCheatItemsEnabled())
                cheatItemsEnabled = Config.isCheatItemsEnabled();
            else
                return true;
        }

        // Get locked stacks
        List<ItemStack> stacks = new LinkedList<>();

        for (Technology tech : TechnologyManager.INSTANCE)
            if (!tech.isResearched(Minecraft.getMinecraft().player))
                for (IUnlock unlock : tech.getUnlock())
                    if (unlock.isDisplayed())
                        Collections.addAll(stacks, unlock.getIcon().getMatchingStacks());

        // Lock those inside JEI
        if (!lockedLast.isEmpty()) {
            ingredientRegistry.addIngredientsAtRuntime(VanillaTypes.ITEM, lockedLast);
        }
        if (!cheatItemsEnabled && !stacks.isEmpty() && RFTGUConfig.jeiHide == RFTGUConfig.HideJeiItems.LOCKED_RECIPES_AND_ITEMS) {
            ingredientRegistry.removeIngredientsAtRuntime(VanillaTypes.ITEM, stacks);
        }

        // Lock every recipe with those outputs
        Ingredient locked = new ItemPredicate(stacks.toArray(new ItemStack[0]));

        for (IRecipeWrapper wrapper : wrappers) {
            IIngredients ingredients = ((RecipeRegistry) recipeRegistry).getIngredients(wrapper);
            List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

            if (RFTGUConfig.jeiHide != RFTGUConfig.HideJeiItems.NOTHING && outputs.size() > 0
                    && outputs.get(0).size() > 0 && locked.apply(outputs.get(0).get(0))) {
                recipeRegistry.hideRecipe(wrapper, VanillaRecipeCategoryUid.CRAFTING);
            } else {
                recipeRegistry.unhideRecipe(wrapper, VanillaRecipeCategoryUid.CRAFTING);
            }
        }

        // Refresh ingredient list
        Internal.getRuntime().getIngredientListOverlay().rebuildItemFilter();
        lockedLast = stacks;
        return true;
	}

}
