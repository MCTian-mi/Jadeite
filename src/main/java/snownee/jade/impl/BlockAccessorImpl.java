package snownee.jade.impl;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Suppliers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.RayTraceResult;
import snownee.jade.Jade;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.DataCodec;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.network.RequestBlockPacket;
import snownee.jade.network.ServerPayloadContext;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<RayTraceResult> implements BlockAccessor {

	private final IBlockState blockState;
	@Nullable
	private final Supplier<TileEntity> blockEntity;

	private BlockAccessorImpl(Builder builder) {
		super(
				Objects.requireNonNull(builder.level),
				Objects.requireNonNull(builder.player),
				builder.serverData,
				Suppliers.ofInstance(Objects.requireNonNull(builder.hit)),
				builder.connected,
				builder.showDetails);
		blockState = builder.blockState;
		blockEntity = builder.blockEntity;
		serversideRep = builder.serversideRep;
	}

	public static void handleRequest(RequestBlockPacket message, ServerPayloadContext context, Consumer<NBTTagCompound> responseSender) {
		EntityPlayerMP player = context.player();
		context.execute(() -> {
			BlockPos pos = message.data().hit().getBlockPos();
			if (Jade.isOutOfReach(player, pos, player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue())
					|| !player.getEntityWorld().isBlockLoaded(pos)) {
				NBTTagCompound tag = message.data().data();
				if (tag == null) {
					tag = new NBTTagCompound();
				}
				tag.setInteger("x", pos.getX());
				tag.setInteger("y", pos.getY());
				tag.setInteger("z", pos.getZ());
				responseSender.accept(tag);
				return;
			}

			BlockAccessor accessor = message.data().unpack(player);
			if (accessor == null) {
				return;
			}

			NBTTagCompound tag = accessor.getServerData();
			tag.setInteger("x", pos.getX());
			tag.setInteger("y", pos.getY());
			tag.setInteger("z", pos.getZ());
			tag.setString("BlockId", CommonProxy.getId(accessor.getBlock()).toString());

			List<IServerDataProvider<BlockAccessor>> providers = WailaCommonRegistration.instance()
					.blockDataProvidersOf(accessor.getBlockState(), accessor.getBlockEntity(), true);
			for (IServerDataProvider<BlockAccessor> provider : providers) {
				if (!message.dataProviders().contains(provider)) {
					continue;
				}
				try {
					provider.appendServerData(tag, accessor);
				} catch (Exception e) {
					WailaExceptionHandler.handleErr(e, provider, null);
				}
			}

			responseSender.accept(tag);
		});
	}

	@Override
	public Block getBlock() {
		return getBlockState().getBlock();
	}

	@Override
	public IBlockState getBlockState() {
		return blockState;
	}

	@Override
	public @Nullable TileEntity getBlockEntity() {
		return blockEntity == null ? null : blockEntity.get();
	}

	@Override
	public BlockPos getPosition() {
		return getHitResult().getBlockPos();
	}

	@Override
	public EnumFacing getSide() {
		return getHitResult().sideHit;
	}

	@Override
	public ItemStack getPickedResult() {
		if (isServersideContent()) {
			return getServersideRep();
		}
		return CommonProxy.getBlockPickedResult(blockState, getPlayer(), getHitResult());
	}

	@Nullable
	@Override
	public Object getTarget() {
		return getBlockEntity();
	}

	@Override
	public boolean verifyData(NBTTagCompound data) {
		if (!verify) {
			return true;
		}
		int x = data.getInteger("x");
		int y = data.getInteger("y");
		int z = data.getInteger("z");
		BlockPos hitPos = getPosition();
		return x == hitPos.getX() && y == hitPos.getY() && z == hitPos.getZ();
	}

	public static class Builder implements BlockAccessor.Builder {

		private @Nullable World level;
		private @Nullable EntityPlayer player;
		private @Nullable NBTTagCompound serverData;
		private boolean connected;
		private boolean showDetails;
		private @Nullable RayTraceResult hit;
		private IBlockState blockState = Blocks.AIR.getDefaultState();
		private @Nullable Supplier<@Nullable TileEntity> blockEntity;
		private ItemStack serversideRep = ItemStack.EMPTY;
		private boolean verify;

		@Override
		public Builder level(World level) {
			this.level = level;
			return this;
		}

		@Override
		public Builder player(EntityPlayer player) {
			this.player = player;
			return this;
		}

		@Override
		public Builder serverData(@Nullable NBTTagCompound serverData) {
			this.serverData = serverData;
			return this;
		}

		@Override
		public Builder serverConnected(boolean connected) {
			this.connected = connected;
			return this;
		}

		@Override
		public Builder showDetails(boolean showDetails) {
			this.showDetails = showDetails;
			return this;
		}

		@Override
		public Builder hit(RayTraceResult hit) {
			this.hit = hit;
			return this;
		}

		@Override
		public Builder blockState(IBlockState blockState) {
			this.blockState = blockState;
			return this;
		}

		@Override
		public Builder blockEntity(@Nullable Supplier<TileEntity> blockEntity) {
			this.blockEntity = blockEntity;
			return this;
		}

		@Override
		public Builder serversideRep(ItemStack stack) {
			serversideRep = stack;
			return this;
		}

		@Override
		public Builder from(BlockAccessor accessor) {
			level = accessor.getLevel();
			player = accessor.getPlayer();
			serverData = accessor.getServerData().copy();
			connected = accessor.isServerConnected();
			showDetails = accessor.showDetails();
			hit = accessor.getHitResult();
			blockEntity = accessor::getBlockEntity;
			blockState = accessor.getBlockState();
			serversideRep = accessor.getServersideRep();
			verify = accessor.shouldVerifyData();
			return this;
		}

		@Override
		public BlockAccessor.Builder requireVerification(boolean verify) {
			this.verify = verify;
			return this;
		}

		@Override
		public BlockAccessor build() {
			BlockAccessorImpl accessor = new BlockAccessorImpl(this);
			if (verify) {
				accessor.requireVerification();
			}
			return accessor;
		}
	}

	public record SyncData(boolean showDetails, RayTraceResult hit, ItemStack serversideRep, NBTTagCompound data) {
		public static final DataCodec<SyncData> STREAM_CODEC = new DataCodec<>() {
			@Override
			public SyncData decode(PacketBuffer buf) {
				boolean showDetails = buf.readBoolean();
				RayTraceResult hit = readBlockHitResult(buf);
				ItemStack serversideRep = DataCodec.readStack(buf);
				NBTTagCompound data = DataCodec.readTag(buf);
				return new SyncData(showDetails, hit, serversideRep, data);
			}

			@Override
			public void encode(PacketBuffer buf, SyncData value) {
				buf.writeBoolean(value.showDetails);
				writeBlockHitResult(buf, value.hit);
				buf.writeItemStack(value.serversideRep);
				buf.writeCompoundTag(value.data);
			}
		};

		public static void writeBlockHitResult(PacketBuffer buf, RayTraceResult hit) {
			buf.writeBlockPos(hit.getBlockPos());
			buf.writeByte(hit.sideHit.ordinal());
			buf.writeDouble(hit.hitVec.x);
			buf.writeDouble(hit.hitVec.y);
			buf.writeDouble(hit.hitVec.z);
		}

		public static RayTraceResult readBlockHitResult(PacketBuffer buf) {
			BlockPos pos = buf.readBlockPos();
			EnumFacing side = EnumFacing.values()[(buf.readByte() & 0xFF) % EnumFacing.values().length];
			double x = buf.readDouble();
			double y = buf.readDouble();
			double z = buf.readDouble();
			// 1.12.2: the 4-arg (Type, Vec3d, EnumFacing, BlockPos) constructor is private;
			// the 3-arg form implies Type.BLOCK.
			return new RayTraceResult(new Vec3d(x, y, z), side, pos);
		}

		public SyncData(BlockAccessor accessor) {
			this(
					accessor.showDetails(),
					accessor.getHitResult(),
					accessor.getServersideRep(),
					accessor.getServerData());
		}

		@SuppressWarnings("DataFlowIssue")
		public @Nullable BlockAccessor unpack(EntityPlayerMP player) {
			Supplier<TileEntity> blockEntity = null;
			IBlockState blockState = player.getEntityWorld().getBlockState(hit.getBlockPos());
			if (blockState.getBlock().hasTileEntity(blockState)) {
				blockEntity = Suppliers.memoize(() -> player.getEntityWorld().getTileEntity(hit.getBlockPos()));
			}
			return new Builder()
					.level(player.getEntityWorld())
					.player(player)
					.showDetails(showDetails)
					.hit(hit)
					.blockState(blockState)
					.blockEntity(blockEntity)
					.serversideRep(serversideRep)
					.serverData(data)
					.build();
		}
	}
}
