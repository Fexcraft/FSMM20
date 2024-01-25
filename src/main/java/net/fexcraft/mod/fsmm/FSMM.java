package net.fexcraft.mod.fsmm;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FSMM.MODID)
public class FSMM {

	public static final String MODID = "fsmm";
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	public static final DeferredBlock<Block> ATM_BLOCK = BLOCKS.registerSimpleBlock("atm", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
	public static final DeferredItem<BlockItem> ATM_ITEM = ITEMS.registerSimpleBlockItem("atm", ATM_BLOCK);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FSMM_TAB =
		CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.fsmm"))
			.withTabsBefore(CreativeModeTabs.COMBAT)
			.icon(() -> ATM_ITEM.get().getDefaultInstance())
			.displayItems((parameters, output) -> {
				output.accept(ATM_ITEM.get());
			}).build());

	public FSMM(IEventBus modbus){
		modbus.addListener(this::commonSetup);
		BLOCKS.register(modbus);
		ITEMS.register(modbus);
		CREATIVE_MODE_TABS.register(modbus);
		//
		NeoForge.EVENT_BUS.register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		//
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		//
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event){
		//
	}


	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event){
			//
		}

	}

}
