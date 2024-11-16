package net.foxirion.tmml.item;

import net.minecraft.component.type.FoodComponent;

public class TMMLFoodComponents {
    public static final FoodComponent VOID_BOTTLE = new FoodComponent.Builder()
            .alwaysEdible()
            .nutrition(0)
            .saturationModifier(0)
            .build();
}
