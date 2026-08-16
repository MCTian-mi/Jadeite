package snownee.jade.compat.gregtech.provider;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IPrimitivePump;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

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

public class PrimitivePumpInfoProvider implements StreamServerDataProvider<BlockAccessor, Integer> {
	public static final PrimitivePumpInfoProvider INSTANCE = new PrimitivePumpInfoProvider();

	@Override
	public @Nullable Integer streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity() instanceof IPrimitivePump pump) {
			return pump.getFluidProduction();
		}
		return null;
	}

	@Override
	public DataCodec<Integer> streamCodec() {
		return new DataCodec<>() {
			@Override
			public Integer decode(PacketBuffer buf) {
				return buf.readInt();
			}

			@Override
			public void encode(PacketBuffer buf, Integer value) {
				buf.writeInt(value);
			}
		};
	}

	@Override
	public ResourceLocation getUid() {
		return GTIds.GT_PRIMITIVE_PUMP;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_PRIMITIVE_PUMP) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder holder) || !(holder.getMetaTileEntity() instanceof IPrimitivePump)) return;
			Integer fluidProduction = PrimitivePumpInfoProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (fluidProduction != null) {
				tooltip.add(new TextComponentTranslation("gregtech.top.primitive_pump_production")
						.appendText(TextFormatting.AQUA + TextFormattingUtil.formatNumbers(fluidProduction) + TextFormatting.RESET + "L/s"));
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_PRIMITIVE_PUMP;
		}
	}
}
