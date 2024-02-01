package net.fexcraft.mod.fsmm;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.attach.FsmmAttachments;
import net.fexcraft.mod.fsmm.attach.PlayerAttachment;
import net.fexcraft.mod.fsmm.data.*;
import net.fexcraft.mod.fsmm.ui.*;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.fexcraft.mod.uni.UniReg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static net.fexcraft.mod.fsmm.util.Config.chat;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod(FSMM.MODID)
public class FSMM {

	public static final String MODID = "fsmm";
	public static final String PREFIX = "&0[&bFSMM&0]&7 ";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	public static final DeferredBlock<Block> ATM_BLOCK = BLOCKS.register("atm", () -> new AtmBlock());
	public static final DeferredItem<BlockItem> ATM_ITEM = ITEMS.registerSimpleBlockItem("atm", ATM_BLOCK);
	public static final DeferredItem<MobileAtm> MOBILE_ATM = ITEMS.register("mobile", () -> new MobileAtm());

	public static final String UI_ATM_MAIN = "fsmm:atm_main";
	public static final String UI_ATM_BANK_INFO = "fsmm:atm_bank_info";
	public static final String UI_ATM_BANK_SELECT = "fsmm:atm_bank_select";
	public static final String UI_ATM_ACC_SELECT = "fsmm:atm_account_select";
	public static final String UI_ATM_ACC_RECEIVER = "fsmm:atm_account_receiver";
	public static final String UI_ATM_ACC_WITHDRAW = "fsmm:atm_account_withdraw";
	public static final String UI_ATM_ACC_DEPOSIT = "fsmm:atm_account_deposit";
	public static final String UI_ATM_ACC_TRANSFER = "fsmm:atm_account_transfer";
	public static final String UI_ATM_TRANSFERS = "fsmm:atm_transfers";

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
		FsmmAttachments.register(modbus);
		//
		UniReg.registerUI(UI_ATM_MAIN, ATMMain.class);
		UniReg.registerMenu(UI_ATM_MAIN, "assets/fsmm/uis/atm_main", ATMContainer.class);
		UniReg.registerUI(UI_ATM_BANK_INFO, ATMBankInfo.class);
		UniReg.registerMenu(UI_ATM_BANK_INFO, "assets/fsmm/uis/atm_bank_info", ATMContainer.class);
		UniReg.registerUI(UI_ATM_BANK_SELECT, ATMBankSelect.class);
		UniReg.registerMenu(UI_ATM_BANK_SELECT, "assets/fsmm/uis/atm_bank_select", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_WITHDRAW, ATMWithdraw.class);
		UniReg.registerMenu(UI_ATM_ACC_WITHDRAW, "assets/fsmm/uis/atm_acc_withdraw", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_DEPOSIT, ATMDeposit.class);
		UniReg.registerMenu(UI_ATM_ACC_DEPOSIT, "assets/fsmm/uis/atm_acc_deposit", ATMContainer.class);
		UniReg.registerUI(UI_ATM_TRANSFERS, ATMViewTransfers.class);
		UniReg.registerMenu(UI_ATM_TRANSFERS, "assets/fsmm/uis/atm_transfers", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_TRANSFER, ATMTransfer.class);
		UniReg.registerMenu(UI_ATM_ACC_TRANSFER, "assets/fsmm/uis/atm_acc_transfer", ATMContainer.class);
		UniReg.registerUI(UI_ATM_ACC_RECEIVER, ATMSelectReceiver.class);
		UniReg.registerMenu(UI_ATM_ACC_RECEIVER, "assets/fsmm/uis/atm_select_receiver", ATMContainer.class);
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
		CURRENCY.values().forEach(val -> val.stackload());
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		//
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event){
		//
	}

	@SubscribeEvent
	public void onCmdReg(RegisterCommandsEvent event){
		event.getDispatcher().register(literal("fsmm")
			.then(literal("balance").executes(cmd -> {
				if(cmd.getSource().isPlayer()){
					long value = ItemManager.countInInventory(cmd.getSource().getPlayer());
					chat(cmd, "&bInventory&0: &a" + Config.getWorthAsString(value));
					PlayerAttachment attach = cmd.getSource().getPlayer().getData(FsmmAttachments.PLAYER);
					if(attach.getSelectedAccount() != null && !attach.getSelectedAccount().getTypeAndId().equals(attach.getAccount().getTypeAndId())){
						AccountPermission perm = attach.getSelectedAccount();
						chat(cmd, "&bPersonal Balance&0: &a" + Config.getWorthAsString(attach.getAccount().getBalance()));
						chat(cmd, "&bSelected Account&0: &a" + attach.getSelectedAccount().getTypeAndId());
						chat(cmd, "&bSelected Balance&0: &a" + Config.getWorthAsString(attach.getSelectedAccount().getAccount().getBalance()));
					}
					else{
						chat(cmd, "&bAccount Balance&0: &a" + Config.getWorthAsString(attach.getAccount().getBalance()));
					}
				}
				else{
    				Bank bank = DataManager.getDefaultBank();
					chat(cmd, "&bDefault Bank Balance&0: &a" + Config.getWorthAsString(bank.getBalance()));
				}
				return 0;
			}))
			.then(literal("uuid").executes(cmd -> {
				cmd.getSource().sendSystemMessage(Component.literal(cmd.getSource().getPlayerOrException().getGameProfile().getId().toString()));
				return 0;
			}))
			.then(literal("set").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
				.then(argument("acc-id", StringArgumentType.string())
				.then(argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
				.executes(cmd -> {
					try{
						process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
							long am = cmd.getArgument("amount", Integer.class);
							account.setBalance(am);
							chat(cmd, "&bNew Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
							if(!online) chat(cmd, "&7&oYou modified the balance of an Offline Account.");
						});
					}
					catch(Exception e){
						e.printStackTrace();
						chat(cmd, "&c&oErrors during command execution.");
					}
					return 0;
				}
			)))))
			.then(literal("add").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
				.then(argument("acc-id", StringArgumentType.string())
				.then(argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
				.executes(cmd -> {
					try{
						process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
							long am = cmd.getArgument("amount", Integer.class);
							account.setBalance((am += account.getBalance()) < 0 ? 0 : am);
							chat(cmd, "&bNew Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
							if(!online) chat(cmd, "&7&oYou modified the balance of an Offline Account.");
						});
					}
					catch(Exception e){
						e.printStackTrace();
						chat(cmd, "&c&oErrors during command execution.");
					}
					return 0;
				}
			)))))
			.then(literal("sub").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
				.then(argument("acc-id", StringArgumentType.string())
				.then(argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
				.executes(cmd -> {
					try{
						process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
							long am = cmd.getArgument("amount", Integer.class);
							account.setBalance((am -= account.getBalance()) < 0 ? 0 : am);
							chat(cmd, "&bNew Balance&0: &7" + Config.getWorthAsString(account.getBalance()));
							if(!online) chat(cmd, "&7&oYou modified the balance of an Offline Account.");
						});
					}
					catch(Exception e){
						e.printStackTrace();
						chat(cmd, "&c&oErrors during command execution.");
					}
					return 0;
				}
			)))))
			.then(literal("info").requires(pre -> isOp(pre))
				.then(argument("acc-type", StringArgumentType.string())
				.then(argument("acc-id", StringArgumentType.string())
				.executes(cmd -> {
					try{
						process(cmd.getSource().getPlayer(), cmd.getArgument("acc-type", String.class), cmd.getArgument("acc-id", String.class), (account, online) -> {
							chat(cmd, "&bAccount&0: &7" + account.getTypeAndId());
							chat(cmd, "&bBalance&0: &7" + Config.getWorthAsString(account.getBalance()));
							if(!online) chat(cmd, "&o&7Account Holder is currently offline.");
						});
					}
					catch(Exception e){
						e.printStackTrace();
						chat(cmd, "&c&oErrors during command execution.");
					}
					return 0;
				}
			))))
			.then(literal("status").requires(pre -> isOp(pre)).executes(cmd -> {
    			chat(cmd, "&bAccounts loaded (by type): &7");
    			long temp = 0;
    			for(String str : DataManager.getAccountTypes(false)){
    				Map<String, Account> map = DataManager.getAccountsOfType(str);
    				temp = map.values().stream().filter(pre -> pre.lastAccessed() >= 0).count();
    				chat(cmd, "&2> &b" + str + ": &7" + map.size() + (temp > 0 ? " &8(&a" + temp + "temp.&8)" : ""));
    			}
    			chat(cmd, "&bBanks active: &7" + DataManager.getBanks().size());
    			chat(cmd, "&aLast scheduled unload: &r&7" + Time.getAsString(DataManager.LAST_TIMERTASK));
				return 0;
			}))
			.executes(cmd -> {
				chat(cmd, PREFIX + "============");
				chat(cmd, "&bUser commands:");
				chat(cmd, "&7/fsmm balance");
				chat(cmd, "&7/fsmm uuid");
				chat(cmd, "&dAdmin commands:");
				chat(cmd, "&7/fsmm set <type:id/name> <amount>");
				chat(cmd, "&7/fsmm add <type:id/name> <amount>");
				chat(cmd, "&7/fsmm sub <type:id/name> <amount>");
				chat(cmd, "&7/fsmm info <type:id/name>");
				chat(cmd, "&7/fsmm status");
				return 0;
			})
		);
	}

	private static boolean isOp(CommandSourceStack css){
		if(css == null || !css.isPlayer()) return false;
		if(ServerLifecycleHooks.getCurrentServer().isSingleplayer()) return true;
		return ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(css.getPlayer().getGameProfile());
	}

	private void process(Player sender, String type, String acc, BiConsumer<Account, Boolean> cons){
		ResourceLocation rs = new ResourceLocation(type, acc.toLowerCase());
		if(rs.getNamespace().equals("player")){
			try{
				UUID.fromString(rs.getPath());
			}
			catch(Exception e){
				Optional<GameProfile> gp = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(rs.getPath());
				rs = new ResourceLocation(type, gp.get().getId().toString());
			}
		}
		Account account = DataManager.getAccount(rs.toString(), false, false);
		boolean online = account != null;
		if(!online) account = DataManager.getAccount(rs.toString(), true, false);
		if(account == null){
			chat(sender, "Account not found.");
			chat(sender, "Searched: " + rs.toString());
			return;
		}
		cons.accept(account, online);
		if(!online){
			DataManager.unloadAccount(account);
		}
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
