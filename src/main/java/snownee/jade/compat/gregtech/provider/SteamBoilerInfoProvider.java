package snownee.jade.compat.gregtech.provider;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.steam.boiler.SteamBoiler;
import net.minecraft.nbt.NBTTagCompound;
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
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.compat.gregtech.GTIds;

public class SteamBoilerInfoProvider implements StreamServerDataProvider<BlockAccessor, SteamBoilerInfoProvider.Data> {
	public static final SteamBoilerInfoProvider INSTANCE = new SteamBoilerInfoProvider();

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity() instanceof SteamBoiler boiler) {
			if (boiler.isBurning()) {
				return new Data(boiler.getTotalSteamOutput(), boiler.hasWater());
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
		return GTIds.GT_STEAM_BOILER;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_STEAM_BOILER) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder holder) || !(holder.getMetaTileEntity() instanceof SteamBoiler)) return;
			Data data = SteamBoilerInfoProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (data != null) {
				int steamOutput = data.totalSteamOutput;

				// Creating steam
				if (steamOutput > 0 && data.hasWater) {
					tooltip.add(JadeUI.fluid(JadeFluidObject.of(Materials.Steam.getFluid())));
					tooltip.append(new TextComponentTranslation("gregtech.top.energy_production")
							.appendText(TextFormatting.AQUA + TextFormattingUtil.formatNumbers(steamOutput / 10) +
									TextFormatting.RESET + "L/s "));
					tooltip.append(new TextComponentTranslation(Materials.Steam.getUnlocalizedName()));
				}

				// Initial heat-up
				if (steamOutput <= 0) {
					tooltip.add(new TextComponentTranslation("gregtech.top.steam_heating_up").setStyle(new Style().setColor(TextFormatting.RED)));
				}

				// No water
				if (!data.hasWater) {
					tooltip.add(new TextComponentTranslation("gregtech.top.steam_no_water").setStyle(new Style().setColor(TextFormatting.YELLOW)));
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_STEAM_BOILER;
		}
	}

	public record Data(int totalSteamOutput, boolean hasWater) {
		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<Data>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				int totalSteamOutput = tag.getInteger("TotalSteamOutput");
				boolean hasWater = tag.getBoolean("HasWater");
				return new Data(totalSteamOutput, hasWater);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("TotalSteamOutput", value.totalSteamOutput);
				tag.setBoolean("HasWater", value.hasWater);
				buf.writeCompoundTag(tag);
			}
		};
	}

}
