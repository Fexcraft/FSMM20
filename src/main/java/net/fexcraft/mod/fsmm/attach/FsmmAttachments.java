package net.fexcraft.mod.fsmm.attach;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.fsmm.data.Account;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FsmmAttachments {

	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "fsmm");
	public static final Supplier<AttachmentType<PlayerAttachment>> PLAYER = ATTACHMENT_TYPES
		.register("handler", () -> AttachmentType.builder(iah -> new PlayerAttachment(iah)).build());

	public static void register(IEventBus modbus){
		ATTACHMENT_TYPES.register(modbus);
	}

}
