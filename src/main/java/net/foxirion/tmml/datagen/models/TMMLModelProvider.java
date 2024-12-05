package net.foxirion.tmml.datagen.models;

import com.google.gson.JsonElement;
import net.foxirion.tmml.init.TMML;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class TMMLModelProvider implements DataProvider {
    public final PackOutput.PathProvider blockStatePathProvider;
    public final PackOutput.PathProvider itemInfoPathProvider;
    public final PackOutput.PathProvider modelPathProvider;
    public TMMLModelProvider(PackOutput output) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        ItemInfoCollector itemInfoCollector = new ItemInfoCollector();
        SimpleModelCollector simpleModelCollector = new SimpleModelCollector();
        new TMMLItemModelGenerator(itemInfoCollector, simpleModelCollector).run();
        //itemInfoCollector.finalizeAndValidate();
        return CompletableFuture.allOf(
                simpleModelCollector.save(output, this.modelPathProvider),
                itemInfoCollector.save(output, this.itemInfoPathProvider)
        );
    }
    @Override
    public final String getName() {
        return "Big Swords R Model Definitions";
    }
    static <T> CompletableFuture<?> saveAll(CachedOutput p_387084_, Function<T, Path> p_386455_, Map<T, ? extends Supplier<JsonElement>> p_386585_) {
        return DataProvider.saveAll(p_387084_, Supplier::get, p_386455_, p_386585_);
    }
    @OnlyIn(Dist.CLIENT)
    public static class ItemInfoCollector implements ItemModelOutput {
        private final Map<Item, ClientItem> itemInfos = new HashMap<>();
        private final Map<Item, Item> copies = new HashMap<>();
        @Override
        public void accept(Item item, ItemModel.Unbaked unbakedModel) {
            register(item, new ClientItem(unbakedModel, ClientItem.Properties.DEFAULT));
        }
        private void register(Item item, ClientItem clientItem) {
            ClientItem clientitem = itemInfos.put(item, clientItem);
            if (clientitem != null) {
                throw new IllegalStateException("Duplicate item model definition for " + item);
            }
        }
        @Override
        public void copy(Item source, Item target) {
            copies.put(target, source);
        }
        public void finalizeAndValidate() {
            BuiltInRegistries.ITEM.forEach(item -> {
                if (item.builtInRegistryHolder().key().location().getNamespace().equals(TMML.TMMLID)) {
                    if (!this.copies.containsKey(item)) {
                        if (item instanceof BlockItem blockitem && !this.itemInfos.containsKey(blockitem)) {
                            ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(blockitem.getBlock());
                            this.accept(blockitem, ItemModelUtils.plainModel(resourcelocation));
                        }
                    }
                }
            });
            Map<Item, Item> modCopies = this.copies.entrySet().stream()
                    .filter(entry ->
                            entry.getKey().builtInRegistryHolder().key().location().getNamespace().equals(TMML.TMMLID) &&
                                    entry.getValue().builtInRegistryHolder().key().location().getNamespace().equals(TMML.TMMLID)
                    )
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            modCopies.forEach((target, source) -> {
                ClientItem clientitem = this.itemInfos.get(source);
                if (clientitem == null) {
                    throw new IllegalStateException("Missing donor: " + source + " -> " + target);
                } else {
                    this.register(target, clientitem);
                }
            });
            List<ResourceLocation> missingModelItems = BuiltInRegistries.ITEM
                    .listElements()
                    .filter(registryEntry ->
                            registryEntry.key().location().getNamespace().equals(TMML.TMMLID) &&
                                    !this.itemInfos.containsKey(registryEntry.value())
                    )
                    .map(p_388278_ -> p_388278_.key().location())
                    .toList();
            if (!missingModelItems.isEmpty()) {
                throw new IllegalStateException("Missing item model definitions for: " + missingModelItems);
            }
        }
        public CompletableFuture<?> save(CachedOutput p_387552_, PackOutput.PathProvider p_388501_) {
            return DataProvider.saveAll(
                    p_387552_, ClientItem.CODEC, p_388594_ -> p_388501_.json(p_388594_.builtInRegistryHolder().key().location()), this.itemInfos
            );
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static class SimpleModelCollector implements BiConsumer<ResourceLocation, ModelInstance> {
        private final Map<ResourceLocation, ModelInstance> models = new HashMap<>();
        @Override
        public void accept(ResourceLocation resourceLocation, ModelInstance modelInstance) {
            Supplier<JsonElement> supplier = this.models.put(resourceLocation, modelInstance);
            if (supplier != null) {
                throw new IllegalStateException("Duplicate model definition for " + resourceLocation);
            }
        }
        public CompletableFuture<?> save(CachedOutput p_386795_, PackOutput.PathProvider p_388673_) {
            return TMMLModelProvider.saveAll(p_386795_, p_388673_::json, this.models);
        }
    }
}