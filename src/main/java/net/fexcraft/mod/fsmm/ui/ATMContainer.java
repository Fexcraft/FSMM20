package net.fexcraft.mod.fsmm.ui;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.AtmBlock;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.attach.FsmmAttachments;
import net.fexcraft.mod.fsmm.attach.PlayerAttachment;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.data.Manageable;
import net.fexcraft.mod.fsmm.events.ATMEvent;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static net.fexcraft.mod.fsmm.util.Config.chat;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMContainer extends ContainerInterface {

	protected ArrayList<Map.Entry<String, String>> banks;
	protected ArrayList<AccountPermission> accounts;
	protected PlayerAttachment pass;
	protected AccountPermission perm;
	protected Account account, receiver;
	protected long inventory;
	protected Bank bank;

	public ATMContainer(JsonMap map, EntityW ply, V3I pos){
		super(map, ply, pos);
		pass = ((Player)ply.direct()).getData(FsmmAttachments.PLAYER);
		perm = pass.getSelectedAccount() == null ? AccountPermission.FULL : pass.getSelectedAccount();
		account = pass.getSelectedAccount() == null ? pass.getAccount() : perm.getAccount();
		receiver = pass.getSelectedReceiver();
		bank = pass.getSelectedBankInATM() == null ? account.getBank() : pass.getSelectedBankInATM();
		pass.setSelectedBankInATM(null);
	}

	public void init(){
		if(uiid.equals(FSMM.UI_ATM_ACC_DEPOSIT) || uiid.equals(FSMM.UI_ATM_ACC_WITHDRAW)){
			BlockState state = ((Player)player.direct()).level().getBlockState(new BlockPos(pos.x, pos.y, pos.z));
			if(state.getBlock() instanceof AtmBlock == false){
				((Player)player.direct()).closeContainer();
				if(!player.isOnClient()) chat(player, I18n.get("ui.fsmm.atm_only"));
			}
		}
	}

	@Override
	public Object get(String key, Object... objs){
		return null;
	}

	@Override
	public void packet(TagCW com, boolean client){
		if(client){
			switch(com.getString("cargo")){
				case "sync":{
					if(com.has("account")){
						account = new Account(JsonHandler.parse(com.getString("account"), true).asMap());
					}
					if(com.has("receiver")){
						receiver = new Account(JsonHandler.parse(com.getString("receiver"), true).asMap());
					}
					if(com.has("bank")){
						bank = new Bank(JsonHandler.parse(com.getString("bank"), true).asMap());
					}
					if(com.has("bank_list")){
						TreeMap<String, String> banks = new TreeMap<>();
						TagLW list = com.getList("bank_list");
						for(int i = 0; i < list.size(); i++){
							String[] str = list.getString(i).split(":");
							if(bank != null && str[0].equals(bank.id)) continue;
							banks.put(str[0], str[1]);
						}
						this.banks = new ArrayList<>();
						this.banks.addAll(banks.entrySet());
					}
					if(com.has("account_list")){
						accounts = new ArrayList<>();
						TagLW list = com.getList("account_list");
						for(int i = 0; i < list.size(); i++){
							accounts.add(new AccountPermission((CompoundTag)list.getCompound(i).direct()));
						}
					}
					if(com.has("inventory")){
						inventory = com.getLong("inventory");
					}
					break;
				}
			}
			return;
		}
		switch(com.getString("cargo")){
			case "bank":{
				player.openUI(FSMM.UI_ATM_BANK_SELECT, pos);
				break;
			}
			case "transfers":{
				player.openUI(FSMM.UI_ATM_TRANSFERS, pos);
				break;
			}
			case "select":{
				player.openUI(FSMM.UI_ATM_ACC_SELECT, pos);
				break;
			}
			case "receiver":{
				player.openUI(FSMM.UI_ATM_ACC_RECEIVER, pos);
				break;
			}
			case "withdraw":{
				player.openUI(FSMM.UI_ATM_ACC_WITHDRAW, pos);
				break;
			}
			case "deposit":{
				player.openUI(FSMM.UI_ATM_ACC_DEPOSIT, pos);
				break;
			}
			case "transfer":{
				player.openUI(FSMM.UI_ATM_ACC_TRANSFER, pos);
				break;
			}
			case "bank_info":{
				pass.setSelectedBankInATM(DataManager.getBank(com.getString("bank")));
				player.openUI(FSMM.UI_ATM_BANK_INFO, pos);
				break;
			}
			case "bank_select":{
				if(!perm.manage){
					chat((Player)player.direct(), "&cYou do not have permission to manage this account.");
					((Player)player.direct()).closeContainer();
					break;
				}
				Bank bank = DataManager.getBank(com.getString("bank"));
				String feeid = account.getType() + ":setup_account";
				long fee = bank.hasFee(feeid) ? Long.parseLong(bank.getFees().get(feeid).replace("%", "")) : 0;
				if(account.getBalance() < fee){
					chat((Player)player.direct(), "&eNot enough money on account to pay the move/setup fee.");
					((Player)player.direct()).closeContainer();
				}
				else{
					if(fee > 0) account.modifyBalance(Manageable.Action.SUB, fee, player.local());
					account.setBank(bank);
					player.openUI(FSMM.UI_ATM_MAIN, pos);
				}
				break;
			}
			case "sync":{
				TagCW compound = TagCW.create();
				if(com.getBoolean("account")){
					compound.set("account", account.toJson(false).toString());
				}
				if(com.getBoolean("account_transfers")){
					compound.set("account", account.toJson(true).toString());
				}
				if(com.getBoolean("receiver") && receiver != null){
					compound.set("receiver", receiver.toJson(false).toString());
				}
				if(com.getBoolean("bank")){
					compound.set("bank", bank.toJson().toString());
				}
				if(com.getBoolean("bank_list")){
					compound.set("bank_list", getBankList());
				}
				if(com.getBoolean("account_list")){
					ATMEvent.GatherAccounts event = new ATMEvent.GatherAccounts(player.local());
					NeoForge.EVENT_BUS.post(event);
					accounts = event.getAccountsList();
					TagLW list = TagLW.create();
					accounts.forEach(account -> {
						list.add(TagCW.wrap(account.toNBT()));
					});
					compound.set("account_list", list);
				}
				if(com.getBoolean("inventory")){
					compound.set("inventory", ItemManager.countInInventory(player.local()));
				}
				compound.set("cargo", "sync");
				SEND_TO_CLIENT.accept(compound);
				break;
			}
			case "action_deposit":
			case "action_withdraw":{
				boolean deposit = com.getString("cargo").endsWith("deposit");
				if(!(deposit ? perm.deposit : perm.withdraw)){
					chat(player, "&cNo permission to " + (deposit ? "deposit to" : "withdraw from") + " this account.");
					return;
				}
				if(processSelfAction(com.getLong("amount"), deposit)){
					((Player)player.direct()).closeContainer();
				}
				break;
			}
			case "action_transfer":{
				if(!perm.transfer){
					chat(player, "&cNo permission to transfer from this account.");
					return;
				}
				long amount = com.getLong("amount");
				if(amount <= 0) return;
				if(receiver == null){
					chat(player, "&cPlease select a receiver!");
					return;
				}
				if(account.getBank().processAction(Bank.Action.TRANSFER, player.local(), account, amount, receiver, false)){
					chat(player, "&bTransfer &7of &e" + Config.getWorthAsString(amount, false) + " &7processed.");
					((Player)player.direct()).closeContainer();
				}
				else{
					chat(player, "&bTransfer &cfailed&7.");
				}
				break;
			}
			case "account_search":{
				String type = com.getString("type").toLowerCase();
				String id = com.getString("id").toLowerCase();
				if(type.trim().length() == 0 || id.trim().length() == 0 || id.length() < Config.min_search_chars) break;
				TagCW compound = TagCW.create();
				ATMEvent.SearchAccounts event = new ATMEvent.SearchAccounts(player.local(), type, id);
				NeoForge.EVENT_BUS.post(event);
				accounts = new ArrayList<>();
				accounts.addAll(event.getAccountsMap().values());
				TagLW list = TagLW.create();
				accounts.forEach(account -> {
					list.add(TagCW.wrap(account.toNBT()));
				});
				compound.set("account_list", list);
				compound.set("cargo", "sync");
				SEND_TO_CLIENT.accept(compound);
				break;
			}
			case "account_select":{
				AccountPermission acc = null;
				String type = com.getString("type"), id = com.getString("id");
				boolean mode = com.getBoolean("mode");
				for(AccountPermission perm : accounts){
					if(perm.getAccount().getType().equals(type) && perm.getAccount().getId().equals(id)){
						acc = perm;
						break;
					}
				}
				if(acc != null){
					if(mode){
						pass.setSelectedAccount(acc);
						player.openUI(FSMM.UI_ATM_MAIN, pos);
					}
					else{
						pass.setSelectedReceiver(acc.getAccount());
						player.openUI(FSMM.UI_ATM_ACC_TRANSFER, pos);
					}
				}
				else{
					chat(player, "&cERROR: Account not found server side.");
					((Player)player.direct()).closeContainer();
				}
				break;
			}
		}
	}

	private boolean processSelfAction(long amount, boolean deposit){
		if(amount <= 0) return false;
		String dep = deposit ? "&eDeposit" : "&aWithdraw";
		if(account.getBank().processAction(deposit ? Bank.Action.DEPOSIT : Bank.Action.WITHDRAW, player.local(), account, amount, account, false)){
			chat(player, dep + " &7of &e" + Config.getWorthAsString(amount, false) + " &7processed.");
			return true;
		}
		else{
			chat(player, dep + " &cfailed&7.");
			return false;
		}
	}

	private TagLW getBankList(){
		TagLW list = TagLW.create();
		DataManager.getBanks().forEach((key, val) -> {
			list.add(key + ":" + val.getName());
		});
		return list;
	}

	public void sync(String... types){
		TagCW compound = TagCW.create();
		compound.set("cargo", "sync");
		for(String str : types){
			compound.set(str, true);
		}
		SEND_TO_SERVER.accept(compound);
	}

	@Override
	public void onClosed(){
		//
	}

}
