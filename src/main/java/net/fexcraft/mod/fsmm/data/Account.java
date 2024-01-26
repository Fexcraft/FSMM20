package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.fsmm.events.AccountEvent;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Account Object.
 * 
 * @author Ferdinand Calo' (FEX___96)
 */
public class Account extends Removable implements Manageable /*, net.NeoForge.common.capabilities.ICapabilitySerializable<NBTTagCompound>*/ {

	private final String id, type;
	private String name;
	private Bank bank;
	private long balance;
	private JsonMap additionaldata;
	private ArrayList<Transfer> transfers = new ArrayList<Transfer>();
	
	/** From JSON Constructor */
	public Account(JsonMap map){
		id = map.get("id").string_value();
		type = map.get("type").string_value();
		bank = DataManager.getBank(map.getString("bank", Config.default_bank));
		balance = map.get("balance").long_value();
		additionaldata = map.has("data") ? map.getMap("data") : null;
		name = map.has("name") ? map.get("name").string_value() : null;
		if(map.has("transfers")){
			for(JsonValue<?> elm : map.getArray("transfers").value){
				transfers.add(new Transfer(elm.asMap()));
			}
		}
		updateLastAccess();
	}
	
	/** Manual Constructor */
	public Account(String id, String type, long bal, Bank bank_, JsonMap data){
		this.id = id;
		this.type = type;
		balance = bal;
		bank = bank_;
		additionaldata = data;
		updateLastAccess();
	}
	
	/** Unique ID of this Account. */
	public String getId(){ return id; }
	
	/** Current balance on this Account (1000 = 1 currency unit, usually) */
	public long getBalance(){
		//updateLastAccess();
		return balance;
	}
	
	/** Method to set the balance (1000 = 1 currency unit, usually)
	 * @param rpl new balance for this account
	 * @return new balance */
	public long setBalance(long rpl){
		NeoForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, rpl));
		updateLastAccess();
		return balance = rpl;
	}
	
	/** Bank of this Account. */
	public Bank getBank(){
		return bank;
	}

	public void setBank(Bank nbank){
		updateLastAccess();
		bank = nbank;
	}
	
	/** Type of this Account, as not only players can hold Accounts. */
	public String getType(){
		return type;
	}
	
	public ResourceLocation getAsResourceLocation(){
		return new ResourceLocation(this.getType(), this.getId());
	}
	
	public String getTypeAndId(){
		return this.getType() + ":" + this.getId();
	}

	public JsonMap getData(){
		return additionaldata;
	}
	
	public void setData(JsonMap obj){
		updateLastAccess();
		additionaldata = obj;
	}

	public String getName(){
		return name == null ? id : name;
	}
	
	public Account setName(String nname){
		name = nname;
		return this;
	}

	/** Mainly used for saving. */
	public JsonMap toJson(boolean withtransfers){
		updateLastAccess();
		JsonMap obj = new JsonMap();
		obj.add("id", id);
		obj.add("type", type);
		obj.add("bank", bank.id);
		obj.add("balance", balance);
		if(additionaldata != null){
			obj.add("data", additionaldata);
		}
		if(name != null) obj.add("name", name);
		if(withtransfers){
			JsonArray array = new JsonArray();
			for(Transfer transfer : transfers) array.add(transfer.toJson());
			if(array.size() > 0) obj.add("transfers", array);
		}
		return obj;
	}

	@Override
	/** Mainly used for saving. */
	public JsonMap toJson(){
		return toJson(true);
	}

	@Override
	public void modifyBalance(Manageable.Action action, long amount, CommandSource log){
		switch(action){
			case SET :{
				NeoForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, amount));
				balance = amount;
				return;
			}
			case SUB :{
				if(balance - amount >= 0){
					NeoForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, balance -= amount));
				}
				else{
					log.sendSystemMessage(Component.literal("Not enough money to subtract this amount! (B:" + (balance / 1000) + " - S:" + (amount / 1000) + ")"));
				}
				return;
			}
			case ADD:{
				if(balance + amount >= Long.MAX_VALUE){
					log.sendSystemMessage(Component.literal("Max Value reached."));
				}
				else{
					NeoForge.EVENT_BUS.post(new AccountEvent.BalanceUpdated(this, balance, balance += amount));
				}
			}
			default: return;
		}
	}

	public void addTransfer(Transfer transfer){
		transfers.add(0, transfer);
		while(transfers.size() > Config.transfer_cache){
			transfers.remove(Config.transfer_cache);
		}
	}

	public List<Transfer> getTransfers(){
		return transfers;
	}

}