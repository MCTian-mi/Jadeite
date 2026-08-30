package snownee.jade.compat.top;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.shim.top.TopShim;

/**
 * Bridge that runs TOP {@link IProbeInfoProvider}s server-side into a
 * {@link CaptureProbeInfo}, serializes the resulting DTO list through
 * Jade's server-data NBT channel, and rebuilds as Jade elements client-side.
 * <p>
 * 1.12.2 backport: uses the shim's own logger; provider UIDs are plain
 * {@code "jade:top_*"} ResourceLocations (the shim must not depend on
 * {@code snownee.jade.api.JadeIds}).
 */
public class TopBlockBridge implements IServerDataProvider<BlockAccessor> {

	private static final Logger LOGGER = LogManager.getLogger(TopShim.MODID);

	public static final TopBlockBridge INSTANCE = new TopBlockBridge();
	public static final Client CLIENT = new Client();
	public static final String TOP_DATA_KEY = "jade:top";

	private TopBlockBridge() {
	}

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		// 1.12.2: when every TOP provider (and the block-accessor sentinel) is disabled, skip the
		// server round-trip entirely. Individual toggles are still enforced per-element on the
		// client (see appendTooltip); this is just the cheap all-off fast path.
		return TopProviderConfig.anyBlockEnabled();
	}

	// ---- Server side: capture TOP providers into DTO list ----

	@Override
	public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
		TopProviderStore store = TheOneProbeImpl.INSTANCE.getStore();
		IBlockState state = accessor.getBlockState();
		IProbeHitData hitData = new ProbeHitDataImpl(accessor);

		NBTTagList list = new NBTTagList();
		for (IProbeInfoProvider provider : store.getBlockProviders()) {
			CaptureProbeInfo capture = new CaptureProbeInfo();
			try {
				provider.addProbeInfo(ProbeMode.NORMAL, capture, accessor.getPlayer(),
						accessor.getLevel(), state, hitData);
			} catch (Throwable e) {
				LOGGER.error("TOP provider {} threw", provider.getID(), e);
			}
			for (ElementDto dto : capture.getElements()) {
				dto.stampProvider(provider.getID());
				list.appendTag(dto.toNbt());
			}
		}

		// Also handle IProbeInfoAccessor directly on the block
		if (accessor.getBlock() instanceof IProbeInfoAccessor probeAccessor) {
			CaptureProbeInfo capture = new CaptureProbeInfo();
			try {
				probeAccessor.addProbeInfo(ProbeMode.NORMAL, capture, accessor.getPlayer(),
						accessor.getLevel(), state, hitData);
			} catch (Throwable e) {
				LOGGER.error("IProbeInfoAccessor on {} threw", state.getBlock(), e);
			}
			for (ElementDto dto : capture.getElements()) {
				dto.stampProvider(TopProviderConfig.BLOCK_ACCESSOR_ID);
				list.appendTag(dto.toNbt());
			}
		}

		if (list.tagCount() > 0) {
			data.setTag(TOP_DATA_KEY, list);
		}
	}

	static void appendTooltip(ITooltip tooltip, NBTTagCompound serverData, String key) {
		if (!serverData.hasKey(key, Constants.NBT.TAG_LIST)) {
			return;
		}
		NBTTagList list = serverData.getTagList(key, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			ElementDto dto = ElementDto.fromNbt(list.getCompoundTagAt(i));
			// 1.12.2: per-provider toggles are enforced client-side (no client→server config
			// sync in this backport). Elements from a provider that has been turned off in the
			// config are dropped here, letting a TOP provider be gradually replaced by a native
			// Jade provider.
			if (!dto.providerId.isEmpty() && !TopProviderConfig.isEnabled(dto.providerId)) {
				continue;
			}
			Element element = ClientElementFactory.fromDto(dto);
			if (element != null) {
				tooltip.add(element);
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation("jade", "top_block");
	}

	public static class Client implements IBlockComponentProvider {

		private Client() {
		}

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			TopBlockBridge.appendTooltip(tooltip, accessor.getServerData(), TOP_DATA_KEY);
		}

		@Override
		public ResourceLocation getUid() {
			return INSTANCE.getUid();
		}
	}

	/**
	 * Minimal IProbeHitData implementation.
	 */
	private static class ProbeHitDataImpl implements IProbeHitData {
		private final net.minecraft.util.math.BlockPos pos;
		private final net.minecraft.util.EnumFacing sideHit;
		private final net.minecraft.util.math.Vec3d hitVec;
		private final net.minecraft.item.ItemStack pickBlock;

		ProbeHitDataImpl(BlockAccessor accessor) {
			this.pos = accessor.getPosition();
			this.sideHit = accessor.getSide();
			this.hitVec = accessor.getHitResult().hitVec;
			this.pickBlock = accessor.getPickedResult();
		}

		@Override
		public net.minecraft.util.math.BlockPos getPos() {
			return pos;
		}

		@Override
		public net.minecraft.util.EnumFacing getSideHit() {
			return sideHit;
		}

		@Override
		public net.minecraft.util.math.Vec3d getHitVec() {
			return hitVec;
		}

		@Override
		public net.minecraft.item.ItemStack getPickBlock() {
			return pickBlock.isEmpty() ? null : pickBlock;
		}
	}
}
