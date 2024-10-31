package net.foxirion.tmml.datagen;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLItemModelProvider extends ItemModelProvider {
    public TMMLItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TMMLID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(TMMLItems.VOID_BOTTLE.get());
    }
}
