package snownee.jade.compat.gregtech;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntityHolder;

import gregtech.common.blocks.BlockLamp;
import gregtech.common.blocks.BlockOre;

import org.jspecify.annotations.NullMarked;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.compat.gregtech.provider.BlockOreDataProvider;
import snownee.jade.compat.gregtech.provider.ControllableDataProvider;
import snownee.jade.compat.gregtech.provider.ConverterDataProvider;
import snownee.jade.compat.gregtech.provider.DiodeDataProvider;
import snownee.jade.compat.gregtech.provider.ElectricContainerDataProvider;
import snownee.jade.compat.gregtech.provider.LDPipeDataProvider;
import snownee.jade.compat.gregtech.provider.LampDataProvider;
import snownee.jade.compat.gregtech.provider.LaserContainerInfoProvider;
import snownee.jade.compat.gregtech.provider.MaintenanceInfoProvider;
import snownee.jade.compat.gregtech.provider.MultiblockInfoProvider;
import snownee.jade.compat.gregtech.provider.PrimitivePumpInfoProvider;
import snownee.jade.compat.gregtech.provider.SteamBoilerInfoProvider;
import snownee.jade.compat.gregtech.provider.TransformerDataProvider;

@WailaPlugin("gregtech")
@NullMarked
public class GregTechPlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ControllableDataProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(ConverterDataProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(DiodeDataProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(LDPipeDataProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(MaintenanceInfoProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(MultiblockInfoProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(PrimitivePumpInfoProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(SteamBoilerInfoProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerBlockDataProvider(TransformerDataProvider.INSTANCE, MetaTileEntityHolder.class);

		registration.registerEnergyStorage(ElectricContainerDataProvider.INSTANCE, MetaTileEntityHolder.class);
		registration.registerEnergyStorage(LaserContainerInfoProvider.INSTANCE, MetaTileEntityHolder.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(GTIds.GT_ORE, true);
		registration.addConfig(GTIds.GT_CONTROLLABLE, true);
		registration.addConfig(GTIds.GT_CONVERTER, true);
		registration.addConfig(GTIds.GT_DIODE, true);
		registration.addConfig(GTIds.GT_LD_PIPE, true);
		registration.addConfig(GTIds.GT_LAMP, true);
		registration.addConfig(GTIds.GT_MAINTENANCE_INFO, true);
		registration.addConfig(GTIds.GT_MULTIBLOCK_INFO, true);
		registration.addConfig(GTIds.GT_PRIMITIVE_PUMP, true);
		registration.addConfig(GTIds.GT_STEAM_BOILER, true);
		registration.addConfig(GTIds.GT_TRANSFORMER, true);

		registration.registerBlockComponent(BlockOreDataProvider.INSTANCE, BlockOre.class);
		registration.registerBlockComponent(ControllableDataProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(ConverterDataProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(DiodeDataProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(LDPipeDataProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(LampDataProvider.INSTANCE, BlockLamp.class);
		registration.registerBlockComponent(MaintenanceInfoProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(MultiblockInfoProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(PrimitivePumpInfoProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(SteamBoilerInfoProvider.Client.INSTANCE, BlockMachine.class);
		registration.registerBlockComponent(TransformerDataProvider.Client.INSTANCE, BlockMachine.class);

		registration.registerEnergyStorageClient(ElectricContainerDataProvider.INSTANCE);
		registration.registerEnergyStorageClient(LaserContainerInfoProvider.INSTANCE);
	}
}
