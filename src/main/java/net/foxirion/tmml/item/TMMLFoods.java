package net.foxirion.tmml.item;

import net.minecraft.world.food.FoodProperties;

public class TMMLFoods {
    public static final FoodProperties VOID_BOTTLE = new FoodProperties.Builder()
            .nutrition(0)
            .saturationMod(0.0F)
            .alwaysEat()
            .build();
}
