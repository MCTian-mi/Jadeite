package snownee.jade.compat.top;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import net.minecraft.util.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IWailaConfig;

/**
 * Per-provider config toggles for the TOP bridge providers.
 * <p>
 * Each registered {@link IProbeInfoProvider} / {@link IProbeInfoEntityProvider} gets its own
 * boolean config key under the {@code theoneprobe} namespace, defaulting to on. Turning one off
 * stops its captured elements from being rendered client-side, so a TOP provider can be gradually
 * replaced by a native Jade provider without the two overlapping in the tooltip.
 * <p>
 * 1.12.2 backport: this backport has no client→server plugin-config sync (the handshake only
 * pushes server overrides to the client), so the server cannot know per-provider toggles. The
 * server therefore always captures every provider's elements, tagging each with the TOP provider
 * ID; {@link #isEnabled} is consulted on the client during {@code appendTooltip}. This is the one
 * place the backport deliberately diverges from a hypothetical server-side gate -- a consistent
 * client-side filter behaves identically in single-player and on dedicated servers.
 */
public final class TopProviderConfig {

	/**
	 * Sentinel TOP "provider" for blocks that implement {@code IProbeInfoAccessor} directly (they
	 * carry no provider ID). Toggleable like any other provider.
	 */
	public static final String BLOCK_ACCESSOR_ID = "theoneprobe:block_accessor";

	/** Config key path prefix; keys read {@code theoneprobe:provider/<slug>}. */
	private static final String KEY_PREFIX = "provider/";

	/** TOP provider ID (or the block-accessor sentinel) → its registered config key. */
	private static final Map<String, ResourceLocation> KEY_BY_ID = new HashMap<>();
	private static final Set<String> blockIds = new HashSet<>();
	private static final Set<String> entityIds = new HashSet<>();

	private TopProviderConfig() {
	}

	/**
	 * Registers one boolean config key per unique TOP provider ID (deduplicated across the block
	 * and entity stores) plus the block-accessor sentinel. Called from {@code TopCompat.registerClient},
	 * which runs after IMC has delivered every provider.
	 */
	public static void registerAll(IWailaClientRegistration registration) {
		TopProviderStore store = TheOneProbeImpl.INSTANCE.getStore();
		Set<String> usedSlugs = new HashSet<>();

		for (IProbeInfoProvider provider : store.getBlockProviders()) {
			if (blockIds.add(provider.getID())) {
				registration.addConfig(uniqueKey(provider.getID(), usedSlugs), true);
			}
		}
		for (IProbeInfoEntityProvider provider : store.getEntityProviders()) {
			if (entityIds.add(provider.getID())) {
				registration.addConfig(uniqueKey(provider.getID(), usedSlugs), true);
			}
		}

		blockIds.add(BLOCK_ACCESSOR_ID);
		registration.addConfig(uniqueKey(BLOCK_ACCESSOR_ID, usedSlugs), true);
	}

	private static ResourceLocation uniqueKey(String id, Set<String> usedSlugs) {
		String base = slug(id);
		String path = base;
		int n = 2;
		while (!usedSlugs.add(path)) {
			path = base + "_" + n++;
		}
		ResourceLocation key = new ResourceLocation("theoneprobe", KEY_PREFIX + path);
		KEY_BY_ID.put(id, key);
		return key;
	}

	/**
	 * Whether a TOP provider's elements should be rendered (default on). Unknown providers -- those
	 * not registered this session -- stay enabled so a stale server data tag never silently hides.
	 */
	public static boolean isEnabled(String providerId) {
		ResourceLocation key = KEY_BY_ID.get(providerId);
		if (key == null) {
			return true;
		}
		return IWailaConfig.get().plugin().get(key);
	}

	/**
	 * Whether any block provider toggle is on (used by {@code TopBlockBridge.shouldRequestData}).
	 */
	public static boolean anyBlockEnabled() {
		return anyEnabled(blockIds);
	}

	/**
	 * Whether any entity provider toggle is on (used by {@code TopEntityBridge.shouldRequestData}).
	 */
	public static boolean anyEntityEnabled() {
		return anyEnabled(entityIds);
	}

	private static boolean anyEnabled(Set<String> ids) {
		for (String id : ids) {
			if (isEnabled(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Slugs an arbitrary TOP provider ID (e.g. {@code "GTCEu:Ore"}) into a valid ResourceLocation
	 * path segment ({@code "gtceu_ore"}). 1.12.2's ResourceLocation performs no charset validation,
	 * but the slug keeps config files and GUI rows readable and stable.
	 */
	static String slug(String id) {
		String s = id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
		s = s.replaceAll("^_+|_+$", "");
		return s.isEmpty() ? "unknown" : s;
	}
}
