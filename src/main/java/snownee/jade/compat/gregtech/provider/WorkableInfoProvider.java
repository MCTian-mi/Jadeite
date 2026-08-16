package snownee.jade.compat.gregtech.provider;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.ComputationRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.DataCodec;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.view.ProgressView;
import snownee.jade.compat.gregtech.GTIds;

public class WorkableInfoProvider implements StreamServerDataProvider<BlockAccessor, WorkableInfoProvider.Data> {
	public static final WorkableInfoProvider INSTANCE = new WorkableInfoProvider();

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity().hasCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null)) {
			IWorkable capability = holder.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
			if (capability != null) {
				return new Data(
						capability.isActive(),
						capability.isWorkingEnabled(),
						capability.getProgress(),
						capability.getMaxProgress(),
						(capability instanceof ComputationRecipeLogic logic && !logic.shouldShowDuration())
				);
			}
		}
		return null;
	}

	@Override
	public DataCodec<Data> streamCodec() {
		return Data.STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return GTIds.GT_WORKABLE;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(getUid()) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder holder) || !(holder.getMetaTileEntity().hasCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null))) return;
			Data data = WorkableInfoProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (data != null && data.isActive) {
				int currentProgress = data.progress();
				int maxProgress = data.maxProgress();

				if (data.shouldShowDuration) {
					// show as total computation instead
					int color = data.isWorkingEnabled() ? 0xFF00D4CE : 0xFFBB1C28;
					tooltip.add(JadeUI.progress(ProgressView.read(new ProgressView.Data((float) currentProgress / maxProgress))));
					//TODO
					/*
					probeInfo.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
							.suffix(" / " + maxProgress + " CWU")
							.filledColor(color)
							.alternateFilledColor(color)
							.borderColor(0xFF555555));
					 */
					return;
				}

				String text;
				if (maxProgress < 20) {
					text = " / " + maxProgress + " t";
				} else {
					currentProgress = Math.round(currentProgress / 20.0F);
					maxProgress = Math.round(maxProgress / 20.0F);
					//text = " / " + TextFormattingUtil.formatNumbers(maxProgress) + " s";
				}

				if (maxProgress > 0) {
					/* TODO
					int color = capability.isWorkingEnabled() ? 0xFF4CBB17 : 0xFFBB1C28;
					probeInfo.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
							.suffix(text)
							.filledColor(color)
							.alternateFilledColor(color)
							.borderColor(0xFF555555).numberFormat(NumberFormat.COMMAS));

					 */
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_WORKABLE;
		}
	}

	public record Data(boolean isActive, boolean isWorkingEnabled, int progress, int maxProgress, boolean shouldShowDuration) {
		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				boolean isActive = tag.getBoolean("IsActive");
				boolean isWorkingEnabled = tag.getBoolean("IsWorkingEnabled");
				int progress = tag.getInteger("Progress");
				int maxProgress = tag.getInteger("MaxProgress");
				boolean shouldShowDuration = tag.getBoolean("ShouldShowDuration");
				return new Data(isActive, isWorkingEnabled, progress, maxProgress, shouldShowDuration);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean("IsActive", value.isActive);
				tag.setBoolean("IsWorkingEnabled", value.isWorkingEnabled);
				tag.setInteger("Progress", value.progress);
				tag.setInteger("MaxProgress", value.maxProgress);
				tag.setBoolean("ShouldShowDuration", value.shouldShowDuration);
				buf.writeCompoundTag(tag);
			}
		};
	}

}
