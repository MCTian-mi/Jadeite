package snownee.jade.compat.gregtech.provider;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.util.ResourceLocation;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.compat.gregtech.GTIds;
import snownee.jade.util.CommonProxy;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ElectricContainerDataProvider implements IServerExtensionProvider<EnergyView.Data>, IClientExtensionProvider<EnergyView.Data, EnergyView> {
	public static final ElectricContainerDataProvider INSTANCE = new ElectricContainerDataProvider();

	@Override
	public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<EnergyView.Data>> groups) {
		return groups.stream().map($ -> {
			String unit = $.getExtraData().hasKey("Unit") ? $.getExtraData().getString("Unit") : CommonProxy.defaultEnergyUnit();
			return new ClientViewGroup<>($.views.stream().map(data -> EnergyView.read(data, unit)).filter(Objects::nonNull)
					.collect(Collectors.toList()));
		}).collect(Collectors.toList());
	}

	@Override
	public @Nullable List<ViewGroup<EnergyView.Data>> getGroups(Accessor<?> accessor) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			MetaTileEntityHolder holder = blockAccessor.typedBlockEntity();
			IEnergyContainer capability = holder.getMetaTileEntity().getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
			if (capability != null) {
				var group = new ViewGroup<>(List.of(new EnergyView.Data(capability.getEnergyStored(), capability.getEnergyCapacity())));
				group.getExtraData().setString("Unit", " EU");
				return List.of(group);
			}
		}
		return null;
	}

	@Override
	public boolean shouldRequestData(Accessor<?> accessor) {
		return accessor instanceof BlockAccessor blockAccessor && blockAccessor.getBlockEntity() instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity().hasCapability(
				GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
	}

	@Override
	public ResourceLocation getUid() {
		return GTIds.GT_ENERGY_CONTAINER;
	}
}
