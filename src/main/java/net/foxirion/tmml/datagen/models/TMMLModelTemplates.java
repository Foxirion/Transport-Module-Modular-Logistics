package net.foxirion.tmml.datagen.models;

import net.foxirion.tmml.init.TMML;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class TMMLModelTemplates {
    public static final ModelTemplate TRANSPORT_MODULE_ITEM = createItem("transport_module", TextureSlot.LAYER0);
    public static ModelTemplate createItem(String name, TextureSlot... textureSlots) {
        return new ModelTemplate(Optional.of(TMML.rl("item/" + name)), Optional.empty(), textureSlots);
    }
}