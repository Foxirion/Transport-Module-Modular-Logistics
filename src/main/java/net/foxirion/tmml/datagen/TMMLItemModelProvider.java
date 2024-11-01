package net.foxirion.tmml.datagen;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLItemModelProvider extends ItemModelProvider {
    public TMMLItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TMMLID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Void Bottle
        basicItem(TMMLItems.VOID_BOTTLE.get());

        //Transport Modules
        createModule(TMMLItems.BLOCK_TRANSPORT_MODULE.get());
        createModule(TMMLItems.ITEM_TRANSPORT_MODULE.get());
        createModule(TMMLItems.ENTITY_TRANSPORT_MODULE.get());

    }

    //Methods
    private void createModule(Item item) {
        String name = getItemName(item);
        getBuilder(name)
                .parent(getExistingFile(modLoc("item/transport_module")))
                .texture("layer0", "item/" + name);

    }
    private String getItemName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString().replace(TMMLID + ":", "");
    }
}