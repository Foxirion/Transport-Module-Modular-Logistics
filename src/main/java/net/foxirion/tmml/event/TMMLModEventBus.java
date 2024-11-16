package net.foxirion.tmml.event;

import net.foxirion.tmml.init.TMMLDataComponents;
import net.foxirion.tmml.item.TMMLItems;
import net.foxirion.tmml.item.custom.FluidTransportModule;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import static net.foxirion.tmml.init.TMML.TMMLID;

@EventBusSubscriber(modid = TMMLID, bus = EventBusSubscriber.Bus.MOD)
public class TMMLModEventBus {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.FluidHandler.ITEM, (itemStack, context) ->
                        new FluidHandlerItemStack(TMMLDataComponents.FLUID_CONTENT, itemStack, FluidTransportModule.MAX_FLUID_CAPACITY),
                TMMLItems.BLOCK_TRANSPORT_MODULE.get()
        );
    }
}
