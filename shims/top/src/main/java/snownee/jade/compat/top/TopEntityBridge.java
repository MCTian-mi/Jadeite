package snownee.jade.compat.top;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.shim.top.TopShim;

/**
 * Bridge that runs TOP {@link IProbeInfoEntityProvider}s server-side and
 * renders the captured DTO list as Jade elements client-side.
 * <p>
 * 1.12.2 backport: uses the shim's own logger; provider UIDs are plain
 * {@code "jade:top_*"} ResourceLocations.
 */
public class TopEntityBridge implements IServerDataProvider<EntityAccessor> {

	private static final Logger LOGGER = LogManager.getLogger(TopShim.MODID);

	public static final TopEntityBridge INSTANCE = new TopEntityBridge();
	public static final Client CLIENT = new Client();
	public static final String TOP_ENTITY_KEY = "jade:top_entity";

	private TopEntityBridge() {
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		// 1.12.2: when every TOP entity provider is disabled, skip the server round-trip entirely.
		// Individual toggles are still enforced per-element on the client (see appendTooltip).
		return TopProviderConfig.anyEntityEnabled();
	}

	@Override
	public void appendServerData(NBTTagCompound data, EntityAccessor accessor) {
		TopProviderStore store = TheOneProbeImpl.INSTANCE.getStore();
		IProbeHitEntityData hitData = new ProbeHitEntityDataImpl(accessor);

		NBTTagList list = new NBTTagList();
		for (IProbeInfoEntityProvider provider : store.getEntityProviders()) {
			CaptureProbeInfo capture = new CaptureProbeInfo();
			try {
				provider.addProbeEntityInfo(ProbeMode.NORMAL, capture, accessor.getPlayer(),
						accessor.getLevel(), accessor.getEntity(), hitData);
			} catch (Throwable e) {
				LOGGER.error("TOP entity provider {} threw", provider.getID(), e);
			}
			for (ElementDto dto : capture.getElements()) {
				dto.stampProvider(provider.getID());
				list.appendTag(dto.toNbt());
			}
		}

		if (list.tagCount() > 0) {
			data.setTag(TOP_ENTITY_KEY, list);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation("jade", "top_entity");
	}

	public static class Client implements IEntityComponentProvider {

		private Client() {
		}

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			TopBlockBridge.appendTooltip(tooltip, accessor.getServerData(), TOP_ENTITY_KEY);
		}

		@Override
		public ResourceLocation getUid() {
			return INSTANCE.getUid();
		}
	}

	private static class ProbeHitEntityDataImpl implements IProbeHitEntityData {
		private final net.minecraft.util.math.Vec3d hitVec;

		ProbeHitEntityDataImpl(EntityAccessor accessor) {
			this.hitVec = accessor.getHitResult().hitVec;
		}

		@Override
		public net.minecraft.util.math.Vec3d getHitVec() {
			return hitVec;
		}
	}
}
