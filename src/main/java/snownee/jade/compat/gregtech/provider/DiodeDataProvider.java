package snownee.jade.compat.gregtech.provider;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.electric.MetaTileEntityDiode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.text.TextComponentTranslation;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.DataCodec;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.compat.gregtech.GTIds;

public class DiodeDataProvider implements StreamServerDataProvider<BlockAccessor, DiodeDataProvider.Data> {
	public static final DiodeDataProvider INSTANCE = new DiodeDataProvider();

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity() instanceof MetaTileEntityDiode diode) {
			IEnergyContainer capability = diode.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
			if (capability != null) {
				return new Data(capability.getInputAmperage(), capability.getOutputAmperage(), diode.getFrontFacing());
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
		return GTIds.GT_DIODE;
	}

	public record Data(long inputAmp, long outputAmp, EnumFacing frontFacing) {
		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				long input = tag.getLong("InputAmp");
				long output = tag.getLong("OutputAmp");
				EnumFacing frontFacing = EnumFacing.byIndex(tag.getInteger("FrontFacing"));
				return new Data(input, output, frontFacing);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setLong("InputAmp", value.inputAmp);
				tag.setLong("OutputAmp", value.outputAmp);
				tag.setInteger("FrontFacing", value.frontFacing.getIndex());
				buf.writeCompoundTag(tag);
			}
		};
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_DIODE) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder te) || !(te.getMetaTileEntity() instanceof MetaTileEntityDiode)) return;

			if (accessor.getBlockEntity() instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity() instanceof MetaTileEntityDiode) {
				Data data = DiodeDataProvider.INSTANCE.decodeFromData(accessor).orElse(null);
				if (data != null) {
					if (accessor.getSide() == data.frontFacing) { // output side
						tooltip.add(new TextComponentTranslation("gregtech.top.transform_output")
								.appendText(" " + TextFormattingUtil.formatNumbers(data.outputAmp) + " A"));
					} else {
						tooltip.add(new TextComponentTranslation("gregtech.top.transform_input")
								.appendText(" " + TextFormattingUtil.formatNumbers(data.inputAmp) + " A"));
					}
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_DIODE;
		}
	}

}
