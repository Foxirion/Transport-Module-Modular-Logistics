package net.foxirion.tmml.datagen.recipe;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class TMMLCraftingRecipes extends TMMLRecipeProvider{
    public final RecipeOutput recipeOutput;
    public TMMLCraftingRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, RecipeOutput recipeOutput) {
        super(output, lookupProvider);
        this.recipeOutput = recipeOutput;
    }

    public void build() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TMMLItems.BLOCK_TRANSPORT_MODULE.get())
                .define('V', TMMLItems.VOID_BOTTLE.get())
                .define('I', Items.IRON_INGOT)
                .define('D', Items.DIAMOND)
                .pattern("DID")
                .pattern("IVI")
                .pattern("DID")
                .unlockedBy(getHasName(TMMLItems.VOID_BOTTLE.get()), has(TMMLItems.VOID_BOTTLE.get()))
                .save(recipeOutput);

//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TMMLItems.LIQUID_TRANSPORT_MODULE.get())
//                .define('V', TMMLItems.VOID_BOTTLE.get())
//                .define('I', Items.IRON_INGOT)
//                .define('D', Items.REDSTONE)
//                .pattern("DID")
//                .pattern("IVI")
//                .pattern("DID")
//                .unlockedBy(getHasName(TMMLItems.VOID_BOTTLE.get()), has(TMMLItems.VOID_BOTTLE.get()))
//                .save(recipeOutput);
//
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TMMLItems.ENTITY_TRANSPORT_MODULE.get())
//                .define('V', TMMLItems.VOID_BOTTLE.get())
//                .define('I', Items.IRON_INGOT)
//                .define('D', Items.EMERALD)
//                .pattern("DID")
//                .pattern("IVI")
//                .pattern("DID")
//                .unlockedBy(getHasName(TMMLItems.VOID_BOTTLE.get()), has(TMMLItems.VOID_BOTTLE.get()))
//                .save(recipeOutput);

    }
}
