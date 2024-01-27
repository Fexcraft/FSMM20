package net.fexcraft.mod.fsmm.events;

import net.fexcraft.mod.fsmm.attach.FsmmAttachments;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ATMEvent extends Event {
	
	private final Player player;
	private final Account account;
	
	private ATMEvent(Player player){
		this.player = player;
		this.account = player.getData(FsmmAttachments.PLAYER).getAccount();
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public Account getAccount(){
		return account;
	}
	
	public Bank getBank(){
		return account.getBank();
	}
	
	/** Event so other mods can add into this list accounts manageable by this player. */
	public static class GatherAccounts extends ATMEvent {
		
		private ArrayList<AccountPermission> accounts = new ArrayList<>();
		
		public GatherAccounts(Player player){
			super(player);
		}
		
		public ArrayList<AccountPermission> getAccountsList(){
			return accounts;
		}
		
	}
	
	/** Event so other mods can add search results. */
	public static class SearchAccounts extends ATMEvent {
		
		private HashMap<String, AccountPermission> accounts = new HashMap<>();
		private String type, id;
		
		public SearchAccounts(Player player, String type, String id){
			super(player);
			this.type = type;
			this.id = id;
		}
		
		public HashMap<String, AccountPermission> getAccountsMap(){
			return accounts;
		}
		
		public String getSearchedType(){
			return type;
		}
		
		public String getSearchedId(){
			return id;
		}
		
	}

}
