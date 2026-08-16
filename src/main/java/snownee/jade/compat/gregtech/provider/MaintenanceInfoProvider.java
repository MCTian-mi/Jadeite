package snownee.jade.compat.gregtech.provider;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.material.Materials;
import gregtech.common.ConfigHolder;
import gregtech.common.items.ToolItems;
import net.minecraft.item.ItemStack;
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
import snownee.jade.api.ui.JadeUI;
import snownee.jade.compat.gregtech.GTIds;

public class MaintenanceInfoProvider implements StreamServerDataProvider<BlockAccessor, MaintenanceInfoProvider.Data> {
	public static final MaintenanceInfoProvider INSTANCE = new MaintenanceInfoProvider();

	private static final ItemStack WRENCH = ToolItems.WRENCH.get(Materials.Neutronium);
	private static final ItemStack SCREWDRIVER = ToolItems.SCREWDRIVER.get(Materials.Neutronium);
	private static final ItemStack SOFT_MALLET = ToolItems.SOFT_MALLET.get(Materials.Neutronium);
	private static final ItemStack HARD_HAMMER = ToolItems.HARD_HAMMER.get(Materials.Neutronium);
	private static final ItemStack WIRE_CUTTERS = ToolItems.WIRE_CUTTER.get(Materials.Neutronium);
	private static final ItemStack CROWBAR = ToolItems.CROWBAR.get(Materials.Neutronium);

	@Override
	public @Nullable Data streamData(BlockAccessor accessor) {
		if (accessor.typedBlockEntity() instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity() instanceof MultiblockControllerBase controller) {
			IMaintenance capability = controller.getCapability(GregtechTileCapabilities.CAPABILITY_MAINTENANCE, null);
			if (capability != null) {
				return new Data(capability.hasMaintenanceMechanics(), capability.hasMaintenanceProblems(),
						capability.getMaintenanceProblems());
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
		return GTIds.GT_MAINTENANCE_INFO;
	}

	public static class Client implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (!config.get(GTIds.GT_MAINTENANCE_INFO) || !ConfigHolder.machines.enableMaintenance || !(accessor.getBlockEntity() instanceof MetaTileEntityHolder)) return;
			if (accessor.getBlockEntity() instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity().hasCapability(GregtechTileCapabilities.CAPABILITY_MAINTENANCE, null)) {
				Data data = MaintenanceInfoProvider.INSTANCE.decodeFromData(accessor).orElse(null);
				if (data != null && data.hasMechanics) {
					if (data.hasProblems) {
						if (accessor.getPlayer().isSneaking()) {
							int problems = data.problems;
							for (byte i = 0; i < 6; i++) {
								if (((problems >> i) & 1) == 0) {
									ItemStack stack = ItemStack.EMPTY;
									String text = "";
									switch (i) {
										case 0: {
											stack = WRENCH;
											text = "gregtech.top.maintenance.wrench";
											break;
										}
										case 1: {
											stack = SCREWDRIVER;
											text = "gregtech.top.maintenance.screwdriver";
											break;
										}
										case 2: {
											stack = SOFT_MALLET;
											text = "gregtech.top.maintenance.soft_mallet";
											break;
										}
										case 3: {
											stack = HARD_HAMMER;
											text = "gregtech.top.maintenance.hard_hammer";
											break;
										}
										case 4: {
											stack = WIRE_CUTTERS;
											text = "gregtech.top.maintenance.wire_cutter";
											break;
										}
										case 5: {
											stack = CROWBAR;
											text = "gregtech.top.maintenance.crowbar";
											break;
										}
									}
									tooltip.add(JadeUI.item(stack));
									tooltip.append(new TextComponentTranslation(text));
								}
							}
						} else {
							tooltip.add(new TextComponentTranslation("gregtech.top.maintenance_broken").setStyle(new Style().setColor(TextFormatting.RED)));
						}
					} else {
						tooltip.add(new TextComponentTranslation("gregtech.top.maintenance_fixed").setStyle(new Style().setColor(
								TextFormatting.GREEN)));
					}
				}
			}
		}

		@Override
		public ResourceLocation getUid() {
			return GTIds.GT_MAINTENANCE_INFO;
		}
	}

	public record Data(boolean hasMechanics, boolean hasProblems, int problems) {
		public static final DataCodec<Data> STREAM_CODEC = new DataCodec<>() {
			@Override
			public Data decode(PacketBuffer buf) {
				NBTTagCompound tag = DataCodec.readTag(buf);
				boolean hasMechanics = tag.getBoolean("HasMechanics");
				boolean hasProblems = tag.getBoolean("HasProblems");
				int problems = tag.getInteger("Problems");
				return new Data(hasMechanics, hasProblems, problems);
			}

			@Override
			public void encode(PacketBuffer buf, Data value) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean("HasMechanics", value.hasMechanics);
				tag.setBoolean("HasProblems", value.hasProblems);
				tag.setInteger("Problems", value.problems);
				buf.writeCompoundTag(tag);
			}
		};
	}
}
