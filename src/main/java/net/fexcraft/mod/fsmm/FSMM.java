package net.fexcraft.mod.fsmm;

import com.mojang.logging.LogUtils;
import net.fexcraft.mod.fsmm.data.MobileAtm;
import net.fexcraft.mod.fsmm.data.Money;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod(FSMM.MODID)
public class FSMM {

	public static final String MODID = "fsmm";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	public static final DeferredBlock<Block> ATM_BLOCK = BLOCKS.registerSimpleBlock("atm", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
	public static final DeferredItem<BlockItem> ATM_ITEM = ITEMS.registerSimpleBlockItem("atm", ATM_BLOCK);
	public static final DeferredItem<MobileAtm> MOBILE_ATM = ITEMS.register("mobile", () -> new MobileAtm());

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FSMM_TAB =
		CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.fsmm"))
			.withTabsBefore(CreativeModeTabs.COMBAT)
			.icon(() -> ATM_ITEM.get().getDefaultInstance())
			.displayItems((parameters, output) -> {
				output.accept(ATM_ITEM.get());
				ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
			}).build());

	public static final LinkedHashMap<ResourceLocation, Money> CURRENCY = new LinkedHashMap<>();
	public static DataManager CACHE;

	public FSMM(IEventBus modbus){
		Config.register();
		modbus.addListener(this::commonSetup);
		BLOCKS.register(modbus);
		ITEMS.register(modbus);
		CREATIVE_MODE_TABS.register(modbus);
		//
		NeoForge.EVENT_BUS.register(this);
	}

	public static List<Money> getSortedMoneyList(){
		return CURRENCY.values().stream().sorted(new Comparator<Money>(){
			@Override public int compare(Money o1, Money o2){ return o1.getWorth() < o2.getWorth() ? 1 : -1; }
		}).collect(Collectors.toList());
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		Config.regExternal();
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		//
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event){
		//
	}


	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ClientEvents {

		@SubscribeEvent
		public static void onItemTooltip(ItemTooltipEvent event){
			if(!Config.show_itemworth) return;
			long worth = Config.getItemStackWorth(event.getItemStack());
			if(worth <= 0) return;
			String str = "&9" + Config.getWorthAsString(worth, true, worth < 10);
			if(event.getItemStack().getCount() > 1){
				str += " &8(&7" + Config.getWorthAsString(worth * event.getItemStack().getCount(), true, worth < 10) + "&8)";
			}
			event.getToolTip().add(Config.getFormatted(str));
		}

	}

	public static void loadDataManager(){
		if(isDataManagerLoaded()) return;
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll(); FSMM.CACHE.clearAll();
		}
		FSMM.CACHE = new DataManager(ServerLifecycleHooks.getCurrentServer().getServerDirectory());
		FSMM.CACHE.schedule();
	}

	public static void unloadDataManager(){
		if(FSMM.CACHE != null){
			FSMM.CACHE.saveAll();
			FSMM.CACHE.clearAll();
			FSMM.CACHE = null;
		}
	}

	public static boolean isDataManagerLoaded(){
		return CACHE != null;
	}

}
