package snownee.jade.compat.top;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.shim.top.TopShim;

/**
 * Registration logic for the TheOneProbe compatibility layer.
 * <p>
 * Registers the TOP bridge providers into Jade's registries so that TOP-annotated
 * mods' tooltips display through Jade's overlay.
 * <p>
 * 1.12.2 backport: this class moved out of Jade's own jar into the standalone TOP
 * shim. The {@code WailaClientRegistration.instance()}/{@code WailaCommonRegistration.instance()}
 * impl singletons were replaced by the public registration interfaces handed to
 * {@code snownee.jade.api.IWailaPlugin.register(...)}/{@code registerClient(...)}, and the
 * {@code Loader.isModLoaded("theoneprobe")} self-check was dropped because this shim IS
 * theoneprobe. The getTheOneProbe IMC handling lives in {@link TopShim#onIMC}.
 */
public final class TopCompat {

	private static final Logger LOGGER = LogManager.getLogger(TopShim.MODID);

	private TopCompat() {
	}

	/**
	 * Registers server-side data providers.
	 */
	public static void registerCommon(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(
				TopBlockBridge.INSTANCE, net.minecraft.block.Block.class);
		registration.registerEntityDataProvider(
				TopEntityBridge.INSTANCE, net.minecraft.entity.Entity.class);
	}

	/**
	 * Registers client-side tooltip providers separately from server data providers.
	 */
	public static void registerClient(IWailaClientRegistration registration) {
		// 1.12.2: register one boolean config toggle per TOP provider so each can be turned off
		// individually and gradually replaced by a native Jade provider. Runs here -- after IMC
		// delivery -- so every provider is already in the store.
		TopProviderConfig.registerAll(registration);

		registration.registerBlockComponent(
				TopBlockBridge.CLIENT, net.minecraft.block.Block.class);
		registration.registerEntityComponent(
				TopEntityBridge.CLIENT, net.minecraft.entity.Entity.class);
		LOGGER.info("TOP compat layer enabled");
	}
}
