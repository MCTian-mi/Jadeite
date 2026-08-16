package snownee.jade.compat.gregtech.provider;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.metatileentity.MetaTileEntityHolder;
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
import snownee.jade.compat.gregtech.GTIds;

public class MultiblockInfoProvider implements StreamServerDataProvider<BlockAccessor, MultiblockInfoProvider.Data> {
	public static final MultiblockInfoProvider INSTANCE = new MultiblockInfoProvider();

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity().hasCapability(GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER, null)) {
			IMultiblockController capability = holder.getMetaTileEntity().getCapability(GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER, null);
			if (capability != null) {
				return new Data(capability.isStructureFormed(), capability.isStructureObstructed());
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
		return GTIds.GT_MULTIBLOCK_INFO;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_MULTIBLOCK_INFO) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder holder) || !(holder.getMetaTileEntity().hasCapability(GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER, null))) return;
			Data data = MultiblockInfoProvider.INSTANCE.decodeFromData(accessor).orElse(null);
			if (data != null) {
				if (data.isStructureFormed) {
					tooltip.add(new TextComponentTranslation("gregtech.top.valid_structure").setStyle(new Style().setColor(TextFormatting.GREEN)));
					if (data.isStructureObstructed) {
						tooltip.add(new TextComponentTranslation("gregtech.top.obstructed_structure").setStyle(new Style().setColor(TextFormatting.RED)));
					}
				} else {
					tooltip.add(new TextComponentTranslation("gregtech.top.invalid_structure").setStyle(new Style().setColor(TextFormatting.RED)));
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_MULTIBLOCK_INFO;
		}
	}

	public record Data(boolean isStructureFormed, boolean isStructureObstructed) {
		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				boolean formed = tag.getBoolean("IsStructureFormed");
				boolean obstructed = tag.getBoolean("IsStructureObstructed");
				return new Data(formed, obstructed);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean("IsStructureFormed", value.isStructureFormed);
				tag.setBoolean("IsStructureObstructed", value.isStructureObstructed);
				buf.writeCompoundTag(tag);
			}
		};
	}

}
