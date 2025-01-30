package net.foxirion.tmml.datagen.recipe;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class TMMLCraftingRecipes extends TMMLRecipeProvider {
    public TMMLCraftingRecipes(HolderLookup.Provider lookupProvider, RecipeOutput recipeOutput) {
        super(lookupProvider, recipeOutput);
    }

    public void build() {
        ShapedRecipeBuilder.shaped(items ,RecipeCategory.MISC, TMMLItems.BLOCK_TRANSPORT_MODULE.get())
                .define('V', TMMLItems.VOID_BOTTLE.get())
                .define('I', Items.IRON_INGOT)
                .define('D', Items.DIAMOND)
                .pattern("DID")
                .pattern("IVI")
                .pattern("DID")
                .unlockedBy(getHasName(TMMLItems.VOID_BOTTLE.get()), has(TMMLItems.VOID_BOTTLE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, TMMLItems.FLUID_TRANSPORT_MODULE.get())
                .define('V', TMMLItems.VOID_BOTTLE.get())
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .pattern("RIR")
                .pattern("IVI")
                .pattern("RIR")
                .unlockedBy(getHasName(TMMLItems.VOID_BOTTLE.get()), has(TMMLItems.VOID_BOTTLE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, TMMLItems.ENTITY_TRANSPORT_MODULE.get())
                .define('V', TMMLItems.VOID_BOTTLE.get())
                .define('I', Items.IRON_INGOT)
                .define('E', Items.EMERALD)
                .pattern("EIE")
                .pattern("IVI")
                .pattern("EIE")
                .unlockedBy(getHasName(TMMLItems.VOID_BOTTLE.get()), has(TMMLItems.VOID_BOTTLE.get()))
                .save(recipeOutput);

    }
}
