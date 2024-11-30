package net.foxirion.tmml.datagen;

import net.foxirion.tmml.TMML;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.TextureKey;

import java.util.Optional;

public class TMMLModels {
    public static final Model TRANSPORT_MODULE;

    private static Model item(String parent, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(TMML.rl("item/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static {
        TRANSPORT_MODULE = item("transport_module", TextureKey.LAYER0);
    }
}
