package snownee.jade.compat.gregtech.provider;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

public class LDPipeDataProvider implements StreamServerDataProvider<BlockAccessor, LDPipeDataProvider.Data> {
	public static final LDPipeDataProvider INSTANCE = new LDPipeDataProvider();
	private static final BlockPos INVALID_POS = new BlockPos(0, 0, 0);

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		MetaTileEntityHolder holder = accessor.typedBlockEntity();
		if (holder.getMetaTileEntity() instanceof ILDEndpoint endpoint) {
			LongDistanceNetwork network = LongDistanceNetwork.get(accessor.getLevel(), accessor.getPosition());
			if (network == null) {
				return new Data(false, false, IOMode.INVALID, -1, INVALID_POS);
			} else {
				ILDEndpoint other = endpoint.getLink();
				if (other == null) {
					return new Data(true, false, IOMode.INVALID, -1, INVALID_POS);
				} else {
					return new Data(true, true, IOMode.getByEndpoint(endpoint), network.getTotalSize(), other.pos());
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
		return GTIds.GT_LD_PIPE;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_LD_PIPE) || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder mteh) || !(mteh.getMetaTileEntity() instanceof ILDEndpoint)) return;

			if (accessor.getBlockEntity() instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity() instanceof ILDEndpoint) {
				Data data = LDPipeDataProvider.INSTANCE.decodeFromData(accessor).orElse(null);
				if (data != null) {
					if (!data.validNetwork) {
						tooltip.add(new TextComponentTranslation("gregtech.top.ld_pipe_no_network").setStyle(new Style().setColor(
								TextFormatting.RED)));
					} else {
						if (!data.connecting) {
							tooltip.add(new TextComponentTranslation("gregtech.top.ld_pipe_incomplete").setStyle(new Style().setColor(
									TextFormatting.RED)));
							data.addIOText(tooltip);
						} else {
							tooltip.add(new TextComponentTranslation("gregtech.top.ld_pipe_connected").setStyle(new Style().setColor(TextFormatting.GREEN)));
							tooltip.add(new TextComponentTranslation("gregtech.top.ld_pipe_length").appendText(" " + data.totalSize));
							data.addIOText(tooltip);

							if (Minecraft.getMinecraft().player.isSneaking()) {
								TextComponentTranslation prefix = null;
								if (data.ioMode == IOMode.OUTPUT) {
									prefix = new TextComponentTranslation("gregtech.top.ld_pipe_input_endpoint");
								} else if (data.ioMode == IOMode.INPUT) {
									prefix = new TextComponentTranslation("gregtech.top.ld_pipe_output_endpoint");
								}

								if (prefix != null) {
									tooltip.add(prefix.appendText(" x: %s y: %s z: %s".formatted(data.targetPos.getX(), data.targetPos.getY(), data.targetPos.getZ())));
								}
							}
						}
					}
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_LD_PIPE;
		}
	}

	public record Data(boolean validNetwork, boolean connecting, IOMode ioMode, int totalSize, BlockPos targetPos) {
		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				boolean validNetwork = tag.getBoolean("ValidNetwork");
				boolean connecting = tag.getBoolean("Connecting");
				IOMode ioMode = IOMode.values()[tag.getInteger("IOMode")];
				int totalSize = tag.getInteger("TotalSize");
				BlockPos targetPos = BlockPos.fromLong(tag.getLong("TargetPos"));
				return new Data(validNetwork, connecting, ioMode, totalSize, targetPos);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean("ValidNetwork", value.validNetwork);
				tag.setBoolean("Connecting", value.connecting);
				tag.setInteger("IOMode", value.ioMode.ordinal());
				tag.setInteger("TotalSize", value.totalSize);
				tag.setLong("TargetPos", value.targetPos.toLong());
				buf.writeCompoundTag(tag);
			}
		};

		public void addIOText(ITooltip tooltip) {
			switch (ioMode) {
				case INPUT -> tooltip.add(new TextComponentTranslation("gregtech.top.ld_pipe_input"));
				case OUTPUT -> tooltip.add(new TextComponentTranslation("gregtech.top.ld_pipe_output"));
				default -> {}
			}
		}
	}

	public enum IOMode {
		INPUT, OUTPUT, INVALID;

		public static IOMode getByEndpoint(ILDEndpoint endpoint) {
			if (endpoint.isInput()) {
				return INPUT;
			} else if (endpoint.isOutput()) {
				return OUTPUT;
			} else {
				return INVALID;
			}
		}
	}

}
