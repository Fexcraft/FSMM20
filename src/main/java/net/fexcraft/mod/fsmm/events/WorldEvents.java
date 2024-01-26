package net.fexcraft.mod.fsmm.events;

import com.mojang.authlib.GameProfile;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "fsmm", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onWorldLoad(LevelEvent.Load event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		FSMM.loadDataManager();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(LevelEvent.Unload event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		FSMM.unloadDataManager();
	}
	
	@SubscribeEvent
	public static void onGatherAccounts(ATMEvent.GatherAccounts event){
		event.getAccountsList().add(new AccountPermission(event.getAccount(), true, true, true, true));
		if(ServerLifecycleHooks.getCurrentServer().isSingleplayer()){
			event.getAccountsList().add(new AccountPermission(event.getBank().getAccount(), true, true, true, true));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onSearchAccounts(ATMEvent.SearchAccounts event){
		if(!event.getSearchedType().equals("player")){
			if(!contains(event.getAccountsMap(), event.getSearchedType()) && DataManager.exists(event.getSearchedType(), event.getSearchedId())){
				put(event.getAccountsMap(), event.getSearchedType() + ":" + event.getSearchedId());
			}
			return;
		}
		for(Account account : DataManager.getAccountsOfType("player").values()){
			if(account.getId().contains(event.getSearchedId()) || account.getName().contains(event.getSearchedId())){
				event.getAccountsMap().put(account.getTypeAndId(), new AccountPermission(account));
			}
		}
		if(Config.partial_search){
			for(String str : ServerLifecycleHooks.getCurrentServer().getPlayerNames()){
				if(str.contains(event.getSearchedId()) && !event.getAccountsMap().containsKey("player:" + str)){
					Optional<GameProfile> gp = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(str);
					if(!gp.isPresent()) continue;
					putIn(event.getAccountsMap(), "player:" + gp.get().getId().toString());
				}
			}
			File folder = new File(DataManager.ACCOUNT_DIR, "player/");
			if(!folder.exists()) return;
			String str = null;
			for(File file : folder.listFiles()){
				if(file.isDirectory() || file.isHidden()) continue;
				if(file.getName().endsWith(".json") && (str = file.getName().substring(0, file.getName().length() - 5)).toLowerCase().contains(event.getSearchedId())){
					put(event.getAccountsMap(), "player:" + str);
				}
			}
		}
		else{
			Optional<GameProfile> gp = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(event.getSearchedId());
			if(gp.isPresent() && new File(DataManager.ACCOUNT_DIR, "player/" + gp.get().getId() + ".json").exists()){
				put(event.getAccountsMap(), "player:" + gp.get().getId());
			}
			else if(new File(DataManager.ACCOUNT_DIR, "player/" + event.getSearchedId() + ".json").exists()){
				put(event.getAccountsMap(), "player:" + event.getSearchedId());
			}
		}
	}

	private static void put(HashMap<String, AccountPermission> map, String id){
		if(map.containsKey(id)) return;
		map.put(id, new AccountPermission(id));
	}

	private static void putIn(HashMap<String, AccountPermission> map, String id){
		map.put(id, new AccountPermission(id));
	}

	/** Checking if another mod returned already anything for this type. */
	private static boolean contains(HashMap<String, AccountPermission> map, String type){
		for(AccountPermission perm : map.values()){
			if(perm.getType().equals(type)) return true;
		}
		return false;
	}
	
}
