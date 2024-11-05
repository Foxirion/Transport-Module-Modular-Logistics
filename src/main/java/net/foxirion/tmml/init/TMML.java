package net.foxirion.tmml.init;

import com.mojang.logging.LogUtils;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static net.foxirion.tmml.init.TMML.TMMLID;

@Mod(TMMLID)
public class TMML {
    public static final String TMMLID = "tmml";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TMML(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        IEventBus modEventBus = context.getModEventBus();

        TMMLCreativeModeTabs.CREATIVE_MODE_TABS.register(bus);
        TMMLItems.ITEMS.register(bus);

//        bus.addListener(DataGenerators::gatherData);
    }

    public static ResourceLocation rl(String path) {
    return new ResourceLocation(TMMLID, path);
    }
}
