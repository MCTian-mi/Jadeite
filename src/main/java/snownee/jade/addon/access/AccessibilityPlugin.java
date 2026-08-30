package snownee.jade.addon.access;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSign;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import snownee.jade.JadeClient;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.util.JadeLanguages;

@WailaPlugin
public class AccessibilityPlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(new SignProvider(), BlockSign.class);
		registration.markAsClientFeature(JadeIds.ACCESS_SIGN);

		registration.registerBlockComponent(new BlockDetailsProvider(), Block.class);
		registration.registerBlockComponent(new BlockDetailsBodyProvider(), Block.class);
		registration.markAsClientFeature(JadeIds.ACCESS_BLOCK_DETAILS);

		registration.registerBlockComponent(new BlockAmountProvider(), Block.class);
		registration.markAsClientFeature(JadeIds.ACCESS_BLOCK_AMOUNT);

		registration.registerEntityComponent(new EntityDetailsProvider(), Entity.class);
		registration.registerEntityComponent(new EntityDetailsBodyProvider(), Entity.class);
		registration.markAsClientFeature(JadeIds.ACCESS_ENTITY_DETAILS);

		registration.markAsClientFeature(JadeIds.ACCESS_NPC_DESCRIPTION);

		registration.registerEntityComponent(new EntityVariantProvider(), EntityLivingBase.class);
		registration.markAsClientFeature(JadeIds.ACCESS_ENTITY_VARIANT);

		registration.registerEntityComponent(new HeldItemProvider(), EntityLivingBase.class);
		registration.markAsClientFeature(JadeIds.ACCESS_HELD_ITEM);

		// 1.12.2: DataComponents.SHULKER_COLOR does not exist -- the color variant is
		// identified by the marker "color", which EntityVariantHelper resolves to
		// EntityShulker#getColor() (an EnumDyeColor).
		registration.addEntityVariantMapping(EntityShulker.class, "color");
		registration.addEntityVariantMapping(EntityVillager.class, null);
	}

	public static void replaceTitle(ITooltip tooltip, String originalName, String key) {
		String message = tooltip.getString(JadeIds.CORE_OBJECT_NAME);
		key = "jade.access." + key;
		if (!message.isEmpty() && JadeUI.hasTranslation(key)) {
			String nameClass = JadeLanguages.INSTANCE.getNameClass(originalName);
			ITextComponent title = IThemeHelper.get().title(JadeClient.format(key, message, nameClass));
			tooltip.replace(JadeIds.CORE_OBJECT_NAME, title);
		}
	}
}
