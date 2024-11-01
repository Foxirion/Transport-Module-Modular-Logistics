package net.foxirion.tmml.init;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.LoggerFactory;

@Mod(TMML.TMMLID)
public class TMML {
    public static final String TMMLID = "tmml";
    public static final Logger logger = LoggerFactory.getLogger(TMML.class);

    public TMML(IEventBus bus) {
        TMMLItems.ITEMS.register(bus);
        TMMLCreativeModeTabs.CREATIVE_TAB.register(bus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(TMMLID, path);
    }
}