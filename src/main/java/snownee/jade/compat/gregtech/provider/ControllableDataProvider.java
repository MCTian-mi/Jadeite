package snownee.jade.compat.gregtech.provider;

import gregtech.api.capability.IControllable;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.DataCodec;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.compat.gregtech.GTIds;

public class ControllableDataProvider implements StreamServerDataProvider<BlockAccessor, Boolean> {
	public static final ControllableDataProvider INSTANCE = new ControllableDataProvider();

	@Override
	public @Nullable Boolean streamData(BlockAccessor accessor) {
		MetaTileEntityHolder te = accessor.typedBlockEntity();
		if (te.getMetaTileEntity() instanceof IControllable controllable) {
			return controllable.isWorkingEnabled();
		}
		return null;
	}

	@Override
	public DataCodec<Boolean> streamCodec() {
		return new DataCodec<>() {
			@Override
			public Boolean decode(PacketBuffer buf) {
				return buf.readBoolean();
			}

			@Override
			public void encode(PacketBuffer buf, Boolean value) {
				buf.writeBoolean(value);
			}
		};
	}

	@Override
	public ResourceLocation getUid() {
		return GTIds.GT_CONTROLLABLE;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_CONTROLLABLE) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder)) return;
			if (accessor.getBlockEntity() instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity() instanceof IControllable) {
				boolean isWorkingEnabled = ControllableDataProvider.INSTANCE.decodeFromData(accessor).orElse(true);
				if (!isWorkingEnabled) {
					tooltip.add(new TextComponentTranslation("gregtech.top.working_disabled").setStyle(new Style().setColor(TextFormatting.RED)));
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_CONTROLLABLE;
		}
	}

}
