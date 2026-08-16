package snownee.jade.compat.gregtech.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.electric.MetaTileEntityTransformer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
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

public class TransformerDataProvider implements StreamServerDataProvider<BlockAccessor, TransformerDataProvider.Data> {
	public static final TransformerDataProvider INSTANCE = new TransformerDataProvider();

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity() instanceof MetaTileEntityTransformer transformer) {
			IEnergyContainer capability = transformer.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
			if (capability != null) {
				EnumFacing input = null, output = null;
				for (EnumFacing facing : EnumFacing.values()) {
					if (capability.inputsEnergy(facing)) input = facing;
					if (capability.outputsEnergy(facing)) output = facing;
				}
				if (input != null && output != null) {
					return new Data(transformer.isInverted(), capability.getInputVoltage(), capability.getInputAmperage(),
							capability.getOutputVoltage(), capability.getOutputAmperage(), input, output);
				}
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
		return GTIds.GT_TRANSFORMER;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(getUid()) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder holder) || !(holder.getMetaTileEntity() instanceof MetaTileEntityTransformer)) return;
			Data data = TransformerDataProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (data != null) {
				String input = GTValues.VNF[GTUtility.getTierByVoltage(data.inputVoltage())] +
						TextFormatting.GREEN +
						" (" +
						TextFormattingUtil.formatNumbers(data.inputAmp()) +
						"A)";

				String output = GTValues.VNF[GTUtility.getTierByVoltage(data.outputVoltage())] +
						TextFormatting.GREEN +
						" (" +
						TextFormattingUtil.formatNumbers(data.outputAmp()) +
						"A)";

				tooltip.add(data.isInverted ?
						new TextComponentTranslation("gregtech.top.transform_up").setStyle(new Style().setColor(TextFormatting.RED)) :
						new TextComponentTranslation("gregtech.top.transform_down").setStyle(new Style().setColor(TextFormatting.GREEN)));
				tooltip.append(new TextComponentString(TextFormatting.RESET + input + "->" + output));

				if (accessor.getSide() == data.input) {
					tooltip.add(new TextComponentTranslation("gregtech.top.transform_input").setStyle(new Style().setColor(TextFormatting.GOLD))
							.appendText(TextFormatting.RESET + input));
				} else if (accessor.getSide() == data.output) {
					tooltip.add(new TextComponentTranslation("gregtech.top.transform_output").setStyle(new Style().setColor(TextFormatting.BLUE))
							.appendText(TextFormatting.RESET + output));
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_TRANSFORMER;
		}
	}

	public record Data(boolean isInverted, long inputVoltage, long inputAmp, long outputVoltage, long outputAmp, EnumFacing input, EnumFacing output) {
		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				boolean isInverted = tag.getBoolean("IsInverted");
				long inputVoltage = tag.getLong("InputVoltage");
				long outputVoltage = tag.getLong("OutputVoltage");
				long inputAmperage = tag.getLong("InputAmp");
				long outputAmperage = tag.getLong("OutputAmp");
				EnumFacing input = EnumFacing.byIndex(tag.getInteger("Input"));
				EnumFacing output = EnumFacing.byIndex(tag.getInteger("Output"));
				return new Data(isInverted, inputVoltage, inputAmperage, outputVoltage, outputAmperage, input, output);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean("IsInverted", value.isInverted);
				tag.setLong("InputVoltage", value.inputVoltage);
				tag.setLong("OutputVoltage", value.outputVoltage);
				tag.setLong("InputAmperage", value.inputAmp);
				tag.setLong("OutputAmperage", value.outputAmp);
				tag.setInteger("Input", value.input.getIndex());
				tag.setInteger("Output", value.output.getIndex());
				buf.writeCompoundTag(tag);
			}
		};
	}

}
