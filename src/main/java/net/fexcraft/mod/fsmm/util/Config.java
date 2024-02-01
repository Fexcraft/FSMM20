package net.fexcraft.mod.fsmm.util;

import com.mojang.brigadier.context.CommandContext;
import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Money;
import net.fexcraft.mod.fsmm.data.MoneyItem;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "fsmm", bus = Mod.EventBusSubscriber.Bus.MOD)
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
	private static final ModConfigSpec.BooleanValue PARTIAL_ACC_SEARCH = BUILDER
		.comment("If true, accounts can be searched by inputting only part of the name, otherwise on false, the full ID/Name is required.")
		.define("partial_account_name_search", true);
	private static final ModConfigSpec.ConfigValue<String> THOUSAND_SEPARATOR = BUILDER
		.comment("Custom thousand separator sign, leave as 'null' for default behaviour.")
		.define("thousand_separator", "null");
	private static final ModConfigSpec.BooleanValue SHOW_DECIMALS = BUILDER
		.comment("Should decimals be shown when zero? e.g. '234.00'")
		.define("show_decimals", true);
	private static final ModConfigSpec.IntValue MIN_SEARCH_CHARS = BUILDER
		.comment("Minimum characters to enter in the 'Name/ID' search bar for search to work.")
		.defineInRange("min_search_chars", 3, 1, 1000);
	public static final ModConfigSpec.IntValue TRANSFER_CACHE = BUILDER
		.comment("Amount of executed transfer data to be cached per account.")
		.defineInRange("transfer_cache", 50, 10, 1000);
	//
	private static final ModConfigSpec SPEC = BUILDER.build();

	public static String DOT;
	public static String COMMA;
	public static int min_search_chars;
	public static int transfer_cache;
	public static long starting_balance;
	public static long unload_frequency;
	public static String default_bank;
	public static String currency_sign;
	public static String thousand_separator;
	public static boolean notify_on_join;
	public static boolean invert_comma;
	public static boolean show_cents;
	public static boolean show_itemworth;
	public static boolean partial_search;
	public static boolean show_decimals;

	public static ArrayList<String> DEFAULT_BANKS;
	private static final TreeMap<String, Long> DEFAULT = new TreeMap<String, Long>();
	static {
		DEFAULT.put("1cent", 10l);
		DEFAULT.put("2cent", 20l);
		DEFAULT.put("5cent", 50l);
		DEFAULT.put("10cent", 100l);
		DEFAULT.put("20cent", 200l);
		DEFAULT.put("50cent", 500l);
		DEFAULT.put("1foney", 1000l);
		DEFAULT.put("2foney", 2000l);
		DEFAULT.put("5foney", 5000l);
		DEFAULT.put("10foney", 10000l);
		DEFAULT.put("20foney", 20000l);
		DEFAULT.put("50foney", 50000l);
		DEFAULT.put("100foney", 100000l);
		DEFAULT.put("200foney", 200000l);
		DEFAULT.put("500foney", 500000l);
		DEFAULT.put("1000foney", 1000000l);
		DEFAULT.put("2000foney", 2000000l);
		DEFAULT.put("5000foney", 5000000l);
		DEFAULT.put("10000foney", 10000000l);
		DEFAULT.put("20000foney", 20000000l);
		DEFAULT.put("50000foney", 50000000l);
		DEFAULT.put("100kfoney", 100000000l);
		DEFAULT.put("200kfoney", 200000000l);
		DEFAULT.put("500kfoney", 500000000l);
	}
	private static TreeMap<ResourceLocation, Long> EXTERNAL_ITEMS = new TreeMap<>();
	private static TreeMap<String, Long> EXTERNAL_ITEMS_METAWORTH = new TreeMap<>();

	@SubscribeEvent
	static void onLoad(final ModConfigEvent.Loading event){
		reload();
	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent.Reloading event){
		reload();
	}

	public static void reload(){
		starting_balance = STARTING_BALANCE.get();
		default_bank = DEFAULT_BANK.get();
		notify_on_join = NOTIFY_ON_JOIN.get();
		currency_sign = CURRENCY_SIGN.get();
		invert_comma = INVERT_COMMA.get();
		show_cents = SHOW_CENTESIMALS.get();
		show_itemworth = SHOW_ITEM_WORTH.get();
		unload_frequency = UNLOAD_FREQUENCY.get();
		partial_search = PARTIAL_ACC_SEARCH.get();
		thousand_separator = THOUSAND_SEPARATOR.get();
		if(thousand_separator.equals("null")) thousand_separator = null;
		show_decimals = SHOW_DECIMALS.get();
		min_search_chars = MIN_SEARCH_CHARS.get();
		transfer_cache = TRANSFER_CACHE.get();
		COMMA = invert_comma ? "." : ",";
		DOT = invert_comma ? "," : ".";
	}

	public static void register(){
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
		File file = new File(FMLPaths.CONFIGDIR.get().toFile(), "/fsmm/configuration.json");
		if(!file.exists()){
			if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
			JsonHandler.print(file, getDefaultContent(), JsonHandler.PrintOption.SPACED);
		}
		JsonMap map = JsonHandler.parse(file);
		if(map.has("Items")){
			map.getArray("Items").value.forEach((elm) -> {
				Money money = new Money(elm.asMap(), true);
				FSMM.CURRENCY.put(money.regname, money);
				FSMM.ITEMS.register(money.regname.getPath(), () -> new MoneyItem(money));
			});
			MoneyItem.sort();
		}
		//
		if(map.has("DefaultBanks")){
			DEFAULT_BANKS = map.getArray("DefaultBanks").toStringList();
		}
	}

	public static void regExternal(){
		File file = new File(FMLPaths.CONFIGDIR.get().toFile(), "/fsmm/configuration.json");
		if(!file.exists()) return;
		JsonMap map = JsonHandler.parse(file);
		if(map.has("ExternalItems")){
			map.getArray("ExternalItems").value.forEach(elm -> {
				JsonMap jsn = elm.asMap();
				ResourceLocation rs = new ResourceLocation(jsn.get("id").string_value());
				long worth = jsn.get("worth").long_value();
				int meta = jsn.getInteger("meta", -1);
				//
				if(meta >= 0){
					EXTERNAL_ITEMS_METAWORTH.put(rs.toString() + ":" + meta, worth);
					if(!EXTERNAL_ITEMS.containsKey(rs)){
						EXTERNAL_ITEMS.put(rs, 0l);
					}
				}
				else{
					EXTERNAL_ITEMS.put(rs, worth);
				}
				if(jsn.has("register") && jsn.get("register").bool()){
					Money money = new Money(jsn, false);
					FSMM.CURRENCY.put(money.regname, money);
				}
			});
		}
	}

	private static JsonMap getDefaultContent(){
		JsonMap map = new JsonMap();
		JsonArray items = new JsonArray();
		DEFAULT.forEach((id, worth) -> {
			JsonMap jsn = new JsonMap();
			jsn.add("id", id);
			jsn.add("worth", worth);
			items.add(jsn);
		});
		map.add("Items", items);
		//
		JsonArray banks = new JsonArray();
		JsonMap def = new JsonMap();
		def.add("uuid", "default");
		def.add("name", "Default Server Bank");
		def.add("data", new JsonMap());
		banks.add(def);
		map.add("Banks", banks);
		//
		JsonMap extexp = new JsonMap();
		JsonArray ext = new JsonArray();
		extexp.add("id", "minecraft:nether_star");
		extexp.add("worth", 100000);
		extexp.add("register", false);
		ext.add(extexp);
		map.add("ExternalItems", ext);
		//
		return map;
	}

	public static final String getWorthAsString(long value){
		return getWorthAsString(value, true, false);
	}

	public static final String getWorthAsString(long value, boolean append){
		return getWorthAsString(value, append, false);
	}

	public static final String getWorthAsString(long value, boolean append, boolean ignore){
		String str = value + "";
		if(value < 1000){
			if(!show_decimals && (value == 0 || (!show_cents && !ignore && value < 100))) return "0" + (append ? currency_sign : "");
			str = value + "";
			str = str.length() == 1 ? "00" + str : str.length() == 2 ? "0" + str : str;
			return ((str = "0" + COMMA + str).length() == 5 && (ignore ? false : !show_cents) ? str.substring(0, 4) : str) + (append ? currency_sign : "");
		}
		else{
			try{
				str = new StringBuilder(str).reverse().toString();
				String[] arr = str.split("(?<=\\G...)");
				str = arr[0] + COMMA;
				for(int i = 1; i < arr.length; i++){
					str += arr[i] + ((i >= arr.length - 1) ? "" : thousand_separator == null ? DOT : thousand_separator);
				}
				str = new StringBuilder(str).reverse().toString();
				return (str = show_decimals ? show_cents || ignore ? str : str.substring(0, str.length() - 1) : str.substring(0, str.lastIndexOf(COMMA))) + (append ? currency_sign : "");
			}
			catch(Exception e){
				e.printStackTrace();
				return value + "ERR";
			}
		}
	}

	public static final long getItemStackWorth(ItemStack stack){
		if(stack.getItem() instanceof Money.Item){
			return ((Money.Item)stack.getItem()).getWorth(stack);
		}
		if(EXTERNAL_ITEMS_METAWORTH.containsKey(getId(stack) + ":" + stack.getDamageValue())){
			return EXTERNAL_ITEMS_METAWORTH.get(getId(stack) + ":" + stack.getDamageValue());
		}
		if(EXTERNAL_ITEMS.containsKey(getId(stack))){
			return EXTERNAL_ITEMS.get(getId(stack));
		}
		return 0;
	}

	public static boolean containsAsExternalItemStack(ItemStack stack){
		try{
			return EXTERNAL_ITEMS.containsKey(getId(stack))
				|| EXTERNAL_ITEMS_METAWORTH.containsKey(getId(stack) + ":" + stack.getDamageValue());
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public static ResourceLocation getId(Item item){
		return BuiltInRegistries.ITEM.getKey(item);
	}

	public static ResourceLocation getId(ItemStack item){
		return BuiltInRegistries.ITEM.getKey(item.getItem());
	}

	public static Component getFormatted(String str){
		return Component.literal(Formatter.format(str));
	}

	public static void chat(CommandSource src, String str){
		src.sendSystemMessage(getFormatted(str));
	}

	public static void log(String str){
		FSMM.LOGGER.info(str);
	}

	public static void chat(CommandContext<CommandSourceStack> cmd, String str){
		chat(cmd.getSource().source, str);
	}

	public static void chat(EntityW entity, String str){
		chat((CommandSource)entity.local(), str);
	}

}
