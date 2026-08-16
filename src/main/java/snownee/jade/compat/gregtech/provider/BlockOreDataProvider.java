package snownee.jade.compat.gregtech.provider;

import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.BlockOre;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.compat.gregtech.GTIds;

public class BlockOreDataProvider implements IBlockComponentProvider {
	public static final BlockOreDataProvider INSTANCE = new BlockOreDataProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(GTIds.GT_ORE)) return;

		if (accessor.getBlock() instanceof BlockOre ore) {
			StoneType type = accessor.getBlockState().getValue(ore.STONE_TYPE);
			if (accessor.getPlayer().isSneaking() && !type.shouldBeDroppedAsItem) {
				tooltip.add(new TextComponentTranslation("gregtech.top.block_drops").appendText(":"));
				ItemStack itemDropped = ore.getItem(accessor.getLevel(), accessor.getPosition(),
						accessor.getBlockState());
				tooltip.add(JadeUI.item(itemDropped));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return GTIds.GT_ORE;
	}
}
