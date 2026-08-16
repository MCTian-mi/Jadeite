package snownee.jade.compat.gregtech.provider;

import gregtech.common.blocks.BlockLamp;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.compat.gregtech.GTIds;

public class LampDataProvider implements IBlockComponentProvider {
	public static final LampDataProvider INSTANCE = new LampDataProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(GTIds.GT_LAMP) || !(accessor.getBlock() instanceof BlockLamp)) return;

		if (accessor.getBlock() instanceof BlockLamp lamp) {
			IBlockState state = accessor.getBlockState();
			boolean inverted = lamp.isInverted(state);
			boolean bloomEnabled = lamp.isBloomEnabled(state);
			boolean lightEnabled = lamp.isLightEnabled(state);

			if (inverted) tooltip.add(new TextComponentTranslation("tile.gregtech_lamp.tooltip.inverted"));
			if (!bloomEnabled) tooltip.add(new TextComponentTranslation("tile.gregtech_lamp.tooltip.no_bloom"));
			if (!lightEnabled) tooltip.add(new TextComponentTranslation("tile.gregtech_lamp.tooltip.no_light"));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return GTIds.GT_LAMP;
	}
}
