package net.foxirion.tmml.datagen;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLItemModelProvider extends ItemModelProvider {
    public TMMLItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, TMMLID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Void Bottle
        basicItem(TMMLItems.VOID_BOTTLE.get());

        //Transport Modules
        createModule(TMMLItems.BLOCK_TRANSPORT_MODULE.get());
        createModule(TMMLItems.LIQUID_TRANSPORT_MODULE.get());
        createModule(TMMLItems.ENTITY_TRANSPORT_MODULE.get());
    }

    //Methods
    private void createModule(Item item) {
        String name = getItemName(item);
        getBuilder(name)
                .parent(getExistingFile(modLoc("item/transport_module")))
                .texture("layer0", "item/" + name);

    }
    public String getItemName(Item item) {
        return ForgeRegistries.ITEMS.getKey(item).toString().replace(TMMLID + ":", "");
    }
}