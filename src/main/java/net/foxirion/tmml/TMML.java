package net.foxirion.tmml;

import net.fabricmc.api.ModInitializer;

import net.foxirion.tmml.event.ModEvent;
import net.foxirion.tmml.item.TMMLItemGroups;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMML implements ModInitializer {
	public static final String TMMLID = "tmml";
	public static final Logger LOGGER = LoggerFactory.getLogger(TMMLID);

	@Override
	public void onInitialize() {
		TMMLItems.registerTMMLItems();
		TMMLItemGroups.registerTMMLItemGroups();
		ModEvent.registerEvents();
	}
	public static Identifier rl(String path) {
		return Identifier.of(TMMLID, path);
	}
}