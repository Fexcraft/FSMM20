package net.fexcraft.mod.fsmm;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = FSMM.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	//
	private static final ModConfigSpec.LongValue STARTING_BALANCE = BUILDER
		.comment("Starting balance for a new player. (1000 == 1F$)")
		.defineInRange("starting_balance", 100000l, 0, Long.MAX_VALUE);
	private static final ModConfigSpec.ConfigValue<String> DEFAULT_BANK = BUILDER
		.comment("Default Bank of the Server.")
		.define("default_bank", "default");
	private static final ModConfigSpec.BooleanValue NOTIFY_ON_JOIN = BUILDER
		.comment("Should the player be notified about his current balance when joining the world?")
		.define("notify_balance_on_join", true);
	private static final ModConfigSpec.ConfigValue<String> CURRENCY_SIGN = BUILDER
		.comment("Custom Currency Sign.")
		.define("currency_sign", "F$");
	private static final ModConfigSpec.BooleanValue INVERT_COMMA = BUILDER
		.comment("Invert ',' and '.' display.")
		.define("invert_comma", false);
	private static final ModConfigSpec.BooleanValue SHOW_CENTESIMALS = BUILDER
		.comment("Should centesimals be shown? E.g. '29,503' instead of '29,50'.")
		.define("show_centesimals", false);
	private static final ModConfigSpec.BooleanValue SHOW_ITEM_WORTH = BUILDER
		.comment("Should the Item's Worth be shown in the tooltip?")
		.define("show_item_worth", true);
	private static final ModConfigSpec.LongValue UNLOAD_FREQUENCY = BUILDER
		.comment("Frequency of how often it should be checked if (temporarily loaded) accounts should be unloaded. Time in milliseconds.")
		.defineInRange("unload_frequency", 600000l, 60000, 86400000 / 2);
	//
	protected static final ModConfigSpec SPEC = BUILDER.build();

	public static long starting_balance;
	public static long unload_frequency;
	public static String default_bank;
	public static String currency_sign;
	public static boolean notify_on_join;
	public static boolean invert_comma;
	public static boolean show_cents;
	public static boolean show_itemworth;

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event){
		starting_balance = STARTING_BALANCE.get();
		default_bank = DEFAULT_BANK.get();
		notify_on_join = NOTIFY_ON_JOIN.get();
		currency_sign = CURRENCY_SIGN.get();
		invert_comma = INVERT_COMMA.get();
		show_cents = SHOW_CENTESIMALS.get();
		show_itemworth = SHOW_ITEM_WORTH.get();
		unload_frequency = UNLOAD_FREQUENCY.get();
	}

}
