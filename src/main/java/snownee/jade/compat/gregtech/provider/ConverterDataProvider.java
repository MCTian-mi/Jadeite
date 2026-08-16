package snownee.jade.compat.gregtech.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.converter.ConverterTrait;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
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

public class ConverterDataProvider implements StreamServerDataProvider<BlockAccessor, ConverterDataProvider.Data> {
	public static final ConverterDataProvider INSTANCE = new ConverterDataProvider();

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity().hasCapability(GregtechCapabilities.CAPABILITY_CONVERTER, null)) {
			ConverterTrait capability = holder.getMetaTileEntity().getCapability(GregtechCapabilities.CAPABILITY_CONVERTER, null);
			return new Data(capability.isFeToEu(), capability.getVoltage(), capability.getBaseAmps(), holder.getMetaTileEntity().getFrontFacing());
		}
		return null;
	}

	@Override
	public DataCodec<Data> streamCodec() {
		return Data.STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return GTIds.GT_CONVERTER;
	}

	public record Data(boolean isFeToEu, long voltage, int baseAmps, EnumFacing frontFacing) {
		public String voltageName() {
			return GTValues.VNF[GTUtility.getTierByVoltage(voltage)];
		}

		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				boolean isFeToEu = tag.getBoolean("IsFeToEu");
				long voltage = tag.getLong("Voltage");
				int baseAmps = tag.getInteger("BaseAmps");
				EnumFacing frontFacing = EnumFacing.byIndex(tag.getInteger("FrontFacing"));
				return new Data(isFeToEu, voltage, baseAmps, frontFacing);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean("IsFeToEu", value.isFeToEu);
				tag.setLong("Voltage", value.voltage);
				tag.setInteger("BaseAmps", value.baseAmps);
				tag.setInteger("FrontFacing", value.frontFacing.getIndex());
				buf.writeCompoundTag(tag);
			}
		};
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_CONVERTER) || accessor.getBlockEntity() == null) return;

			if (accessor.typedBlockEntity() instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity().hasCapability(GregtechCapabilities.CAPABILITY_CONVERTER, null)) {
				Data data = ConverterDataProvider.INSTANCE.decodeFromData(accessor).orElse(null);
				if (data != null) {
					if (data.isFeToEu) {
						tooltip.add(new TextComponentTranslation("gregtech.top.convert_fe"));
						if (accessor.getSide() == data.frontFacing) {
							tooltip.add(new TextComponentTranslation("gregtech.top.transform_output").appendText(" " + data.voltageName() +
									TextFormatting.RESET + " (" + TextFormattingUtil.formatNumbers(data.baseAmps) + ") "));
						} else {
							tooltip.add(new TextComponentTranslation("gregtech.top.transform_input").appendText(" " +
									TextFormattingUtil.formatNumbers(FeCompat.toFe(data.voltage * data.baseAmps, FeCompat.ratio(true)) +
											" FE")));
						}
					} else {
						tooltip.add(new TextComponentTranslation("gregtech.top.convert_eu"));
						if (accessor.getSide() == data.frontFacing) {
							tooltip.add(new TextComponentTranslation("gregtech.top.transform_output").appendText(" " +
									TextFormattingUtil.formatNumbers(FeCompat.toFe(data.voltage * data.baseAmps, FeCompat.ratio(false)) +
											" FE")));
						} else {
							tooltip.add(new TextComponentTranslation("gregtech.top.transform_input").appendText(" " + data.voltageName() +
									TextFormatting.RESET + " (" + TextFormattingUtil.formatNumbers(data.baseAmps) + ") "));
						}
					}
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_CONVERTER;
		}
	}

}
