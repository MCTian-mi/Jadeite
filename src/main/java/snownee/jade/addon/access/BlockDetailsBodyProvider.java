package snownee.jade.addon.access;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockRedstoneWire.EnumAttachPosition;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextComponentUtils;
import snownee.jade.addon.core.BlockFaceProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class BlockDetailsBodyProvider implements IBlockComponentProvider {

	private static final Map<EnumFacing, PropertyEnum<EnumAttachPosition>> BY_DIRECTION = initPropertyMap();

	private static Map<EnumFacing, PropertyEnum<EnumAttachPosition>> initPropertyMap() {
		val map = new EnumMap<EnumFacing, PropertyEnum<EnumAttachPosition>>(EnumFacing.class);
		map.put(EnumFacing.NORTH, BlockRedstoneWire.NORTH);
		map.put(EnumFacing.EAST, BlockRedstoneWire.EAST);
		map.put(EnumFacing.SOUTH, BlockRedstoneWire.SOUTH);
		map.put(EnumFacing.WEST, BlockRedstoneWire.WEST);
		return map;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		IBlockState blockState = accessor.getBlockState();
		Block block = blockState.getBlock();
		if (block instanceof BlockRedstoneWire) {
			List<ITextComponent> list = Lists.newArrayListWithExpectedSize(4);
			BY_DIRECTION.forEach((facing, property) -> {
				if (blockState.getValue(property) != EnumAttachPosition.NONE) {
					list.add(BlockFaceProvider.directionName(facing));
				}
			});

			if (list.isEmpty()) {
				tooltip.add(new TextComponentTranslation("jade.access.block.redstone_wire.dot"));
			} else {
				tooltip.add(new TextComponentTranslation("jade.access.block.redstone_wire", join(list)));
			}
			return;
		}

		EnumRailDirection railShape = null;
		if (blockState.getPropertyKeys().contains(BlockRail.SHAPE)) {
			railShape = blockState.getValue(BlockRail.SHAPE);
		} else if (blockState.getPropertyKeys().contains(BlockRailPowered.SHAPE)) {
			railShape = blockState.getValue(BlockRailPowered.SHAPE);
		} else if (blockState.getPropertyKeys().contains(BlockRailDetector.SHAPE)) {
			railShape = blockState.getValue(BlockRailDetector.SHAPE);
		}
		if (railShape != null) {
			tooltip.add(new TextComponentTranslation("jade.access.block.rail." + railShape.getName()));
		}

		EnumFacing facing = null;
		if (blockState.getPropertyKeys().contains(BlockDirectional.FACING)) {
			facing = blockState.getValue(BlockDirectional.FACING);
		} else if (blockState.getPropertyKeys().contains(BlockHorizontal.FACING)) {
			facing = blockState.getValue(BlockHorizontal.FACING);
		} else if (blockState.getPropertyKeys().contains(BlockHopper.FACING)) {
			facing = blockState.getValue(BlockHopper.FACING);
		}
		if (facing != null) {
			tooltip.add(new TextComponentTranslation("jade.access.block.facing", BlockFaceProvider.directionName(facing)));
		}
	}

	/**
	 * 1.12.2: ComponentUtils.formatList does not exist -- the wire directions are joined
	 * with a ", " literal separator instead of the modern DEFAULT_NO_STYLE_SEPARATOR.
	 */
	private static ITextComponent join(List<ITextComponent> list) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(list.get(i).getUnformattedText());
		}
		return new TextComponentString(builder.toString());
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_BLOCK_DETAILS_BODY;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
