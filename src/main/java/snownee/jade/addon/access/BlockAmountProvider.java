package snownee.jade.addon.access;

import lombok.val;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockRedstoneWire.EnumAttachPosition;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import snownee.jade.JadeClient;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

import java.util.EnumMap;
import java.util.Map;

public class BlockAmountProvider implements IBlockComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(JadeIds.ACCESS_BLOCK_DETAILS)) {
			return;
		}
		val blockState = accessor.getBlockState();

		if (blockState.getPropertyKeys().contains(BlockCake.BITES)) {
			tooltip.add(JadeClient.format("jade.access.block.bites", blockState.getValue(BlockCake.BITES)));
		}
		if (blockState.getPropertyKeys().contains(BlockSnow.LAYERS)) {
			tooltip.add(JadeClient.format("jade.access.block.layers", blockState.getValue(BlockSnow.LAYERS)));
		}
		if (blockState.getPropertyKeys().contains(BlockCauldron.LEVEL)) {
			tooltip.add(JadeClient.format("jade.access.block.level", blockState.getValue(BlockCauldron.LEVEL)));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_BLOCK_AMOUNT;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
