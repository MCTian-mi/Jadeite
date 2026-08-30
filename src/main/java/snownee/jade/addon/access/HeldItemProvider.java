package snownee.jade.addon.access;

import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class HeldItemProvider implements IEntityComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		val entity = (EntityLivingBase) accessor.getEntity();

		if (!entity.getHeldItemMainhand().isEmpty()) {
			tooltip.add(new TextComponentTranslation("jade.access.held_item.main", entity.getHeldItemMainhand().getTextComponent()));
		}
		if (!entity.getHeldItemOffhand().isEmpty()) {
			tooltip.add(new TextComponentTranslation("jade.access.held_item.off", entity.getHeldItemOffhand().getTextComponent()));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_HELD_ITEM;
	}
}
