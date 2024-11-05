package net.foxirion.tmml.datagen.recipe;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLRecipeProvider extends RecipeProvider {
    public final PackOutput output;
    public static String path = TMMLID + ":";

    public TMMLRecipeProvider(PackOutput output) {
        super(output);
        this.output = output;
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeOutput) {
        new TMMLCraftingRecipes(output, recipeOutput).build();
    }
}
