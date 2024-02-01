package net.fexcraft.mod.fsmm.data;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.util.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Money {

	public ResourceLocation regname;
	private ItemStack stack;
	private JsonMap temp;
	private boolean internal;
	private long worth;

	public Money(JsonMap map, boolean inter){
		regname = new ResourceLocation((inter ? FSMM.MODID + ":" : "") + map.getString("id", "invalid_" + map + "_" + Time.getDate()));
		worth = map.getLong("worth", -1);
		int meta = map.getInteger("meta", -1);
		if(meta >= 0 && !inter) regname = new ResourceLocation(regname.toString() + "_" + meta);
		temp = map;
		internal = inter;
	}

	public void stackload(){
		String id = temp.getString("id", "invalid_" + temp + "_" + Time.getDate());
		net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(internal ? FSMM.MODID + ":" + id : id));
		if(item == null){
			Config.log("[FSMM] ERROR - Item with ID '" + regname.toString() + "' couldn't be found! This is bad!");
			ServerLifecycleHooks.handleExit(1);
		}
		CompoundTag compound = null;
		if(temp.has("nbt")){
			try{
				//TODO compound = JsonToNBT.getTagFromJson(map.get("nbt").string_value());
			}
			catch(Exception e){
				Config.log("[FSMM] ERROR - Could not load NBT from config of '" + regname.toString() + "'! This is bad!");
				ServerLifecycleHooks.handleExit(2);
			}
		}
		//
		stack = new ItemStack(item);
		stack.setCount(1);
		stack.setDamageValue(temp.getInteger("meta", -1));
		if(compound != null) stack.setTag(compound);
		temp = null;
	}

	@Override
	public String toString(){
		return super.toString() + "#" + this.getWorth();
	}

	public long getWorth(){
		return worth;
	}

	public ItemStack getItemStack(){
		return stack;
	}
	
	//
	
	public static interface Item {
		
		public Money getType();
		
		/** Singular worth, do not multiply by count! **/
		public long getWorth(ItemStack stack);
		
	}

}
