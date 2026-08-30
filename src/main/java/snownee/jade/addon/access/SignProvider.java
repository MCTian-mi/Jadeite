package snownee.jade.addon.access;

import ibxm.Player;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class SignProvider implements IBlockComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!(accessor.getBlockEntity() instanceof TileEntitySign be)) {
			return;
		}
		tooltip.add(new TextComponentTranslation("jade.access.sign.front"));

		for (int i = 0; i < Math.min(be.signText.length, 4); i++) {
			val message = be.signText[i];
			tooltip.add(accessor.showDetails()
					? new TextComponentTranslation("jade.access.sign.line" + (i + 1), message)
					: message);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_SIGN;
	}
}
