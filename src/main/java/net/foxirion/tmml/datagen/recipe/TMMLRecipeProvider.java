package net.foxirion.tmml.datagen.recipe;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLRecipeProvider extends RecipeProvider {
    public final DataGenerator generator;
    public static String path = TMMLID + ":";

    public TMMLRecipeProvider(DataGenerator generator) {
        super(generator);
        this.generator = generator;
    }

    protected void buildRecipes(Consumer<FinishedRecipe> recipeOutput) {
        new TMMLCraftingRecipes(generator, recipeOutput).build();
    }
}