package net.foxirion.tmml.datagen.recipe;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLRecipeProvider extends RecipeProvider {
    public final RecipeOutput recipeOutput;
    public final HolderLookup.Provider lookupProvider;
    public static String path = TMMLID + ":";
    public static HolderGetter<Item> items;

    public TMMLRecipeProvider(HolderLookup.Provider lookupProvider, RecipeOutput recipeOutput) {
        super(lookupProvider, recipeOutput);
        this.lookupProvider = lookupProvider;
        this.recipeOutput = recipeOutput;
        items = lookupProvider.lookupOrThrow(Registries.ITEM);
    }

    @Override
    protected void buildRecipes() {
        new TMMLCraftingRecipes(lookupProvider, recipeOutput).build();
    }

    @Override
    protected void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked, String pUnpackedName, String pUnpackedGroup) {
        this.nineBlockStorageRecipes(pUnpackedCategory, pUnpacked, pPackedCategory, pPacked, path + getSimpleRecipeName(pPacked), null, path + pUnpackedName, pUnpackedGroup);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new TMMLRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "Big Swords R Recipes";
        }
    }
}
