package net.foxirion.tmml.datagen.recipe;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class TMMLCraftingRecipes extends TMMLRecipeProvider {
    public final Consumer<FinishedRecipe> recipeOutput;

    public TMMLCraftingRecipes(DataGenerator generator, Consumer<FinishedRecipe> recipeOutput) {
        super(generator);
        this.recipeOutput = recipeOutput;
    }

    public void build() {
        ShapedRecipeBuilder.shaped(TMMLItems.BLOCK_TRANSPORT_MODULE.get())
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
