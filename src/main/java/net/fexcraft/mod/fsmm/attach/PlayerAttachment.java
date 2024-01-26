package net.fexcraft.mod.fsmm.attach;

import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.ItemManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class PlayerAttachment {

	private Player player;
    private Account account;
    private AccountPermission atmacc;
    private Account selected;
    private Bank atmbank;

	public PlayerAttachment(IAttachmentHolder ply){
		player = (Player)ply;
	}

	public Account getAccount(){
		return account == null ? account = DataManager.getAccount("player:" + player.getGameProfile().getId().toString(), false, true).setName(player.getGameProfile().getName()) : account;
	}

	public Bank getBank(){
		return account.getBank();
	}

	/** Gets the worth of all (as money defined) Items in Inventory. **/
	public long getMoneyInInventory(){
		return ItemManager.countInInventory(player);
	}

	/** Tries to subtract the defined amount from Inventory, <s>returns amount which couldn't be subtracted. **/
	public long subMoneyFromInventory(long expected_amount){
		return ItemManager.removeFromInventory(player, expected_amount);
	}

	/** Tries to add the defined amount to Inventory, <s>returns amount which couldn't be added.</s> **/
	public long addMoneyToInventory(long expected_amount){
		return ItemManager.addToInventory(player, expected_amount);
	}

	/** Tries to add the defined amount to Inventory, <s>returns amount which couldn't be processed.</s> **/
	public long setMoneyInInventory(long expected_amount){
		return ItemManager.setInInventory(player, expected_amount);
	}

	/** Gets the currently/last selected Account in the ATM. */
	public AccountPermission getSelectedAccount(){
		return atmacc;
	}

	/** Sets the currently/last selected Account in the ATM. */
	public void setSelectedAccount(AccountPermission perm){
		atmacc = perm;
	}

	/** Gets the currently selected receiver Account in the ATM. */
	public Account getSelectedReceiver(){
		return selected;
	}

	/** Sets the currently selected receiver Account in the ATM. */
	public void setSelectedReceiver(Account account){
		selected = account;
	}

	/** Gets the currently looked at Bank in the ATM. */
	public Bank getSelectedBankInATM(){
		return atmbank;
	}

	/** Sets the currently looked at Bank in the ATM. */
	public void setSelectedBankInATM(Bank bank){
		atmbank = bank;
	}

}
