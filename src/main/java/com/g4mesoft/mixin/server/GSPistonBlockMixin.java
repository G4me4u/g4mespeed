package com.g4mesoft.mixin.server;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.access.GSIServerChunkManagerAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlock.class)
public class GSPistonBlockMixin {

	@Inject(method = "onSyncedBlockEvent", slice = @Slice(
			to = @At(value = "INVOKE", ordinal = 0,
				target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V")),
			at = @At(value = "INVOKE", shift = Shift.AFTER,
	        	target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private void onOnSyncedBlockEventBaseChanged(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
		onBlockChanged(world, pos);
	}

	@Inject(method = "onSyncedBlockEvent", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	private void onOnSyncedBlockEventHeadChanged(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
		onBlockChanged(world, pos.offset(state.get(Properties.FACING)));
	}
	
	@Inject(method = "onSyncedBlockEvent", at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/world/World;addBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void onOnSyncedBlockEventBlockEntityChanged(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
		onBlockEntityChanged(world, pos);
	}
	
	@Inject(method = "move", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", ordinal = 0, shift = Shift.AFTER,
			target = "Lnet/minecraft/world/World;addBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void onMoveBlockEntityChanged0(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonHandler pistonHandler, Map<?, ?> map, List<?> list, List<?> list2, List<?> list3, BlockState blockStates[], Direction direction, int j, int l, BlockPos blockPos4) {
		onBlockEntityChanged(world, blockPos4);
	}

	@Inject(method = "move", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", ordinal = 1, shift = Shift.AFTER,
			target = "Lnet/minecraft/world/World;addBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void onMoveBlockEntityChanged1(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos) {
		onBlockEntityChanged(world, blockPos);
	}
	
	@Inject(method = "move", expect = 1, locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", shift = Shift.BEFORE,
	        target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
	private void onMoveAfterRemovingLoop(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonHandler pistonHandler, Map<BlockPos, ?> map, List<?> list, List<?> list2, List<BlockPos> list3) {
		if (!retract) {
			// Piston head
			onBlockChanged(world, blockPos);
		}
		// Broken blocks
		for (BlockPos brokenBlockPos : list3)
			onBlockChanged(world, brokenBlockPos);
		// Moved blocks (not replaced by other moved blocks)
		for (Map.Entry<BlockPos, ?> blockEnty : map.entrySet())
			onBlockChanged(world, blockEnty.getKey());
	}
	
	private void onBlockChanged(World world, BlockPos pos) {
		if (!world.isClient) {
			GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
			if (tpsModule.sParanoidMode.getValue() && !tpsModule.sImmediateBlockBroadcast.getValue())
				((GSIServerChunkManagerAccess)world.getChunkManager()).updateBlockImmediately(pos);
		}
	}
	
	private void onBlockEntityChanged(World world, BlockPos pos) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sParanoidMode.getValue()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if (blockEntity != null) {
				Packet<?> packet = new BlockEntityUpdateS2CPacket(pos, 0, blockEntity.writeNbt(new NbtCompound()));
				((GSIServerChunkManagerAccess)world.getChunkManager()).sendToNearbyPlayers(pos, packet);
			}
		}
	}
}
