package com.g4mesoft.mixin.client;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.access.client.GSIWorldRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSICustomPayloadPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.client.network.handler.ClientPlayPacketHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Connection;
import net.minecraft.network.packet.c2s.play.AcceptTeleportC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlocksUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTeleportS2CPacket;
import net.minecraft.network.packet.s2c.play.LoginS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeS2CPacket;
import net.minecraft.server.entity.EntityTracker;
import net.minecraft.util.math.BlockPos;

/* Use priority -1001 to ensure we have priority over MultiConnect */
@Mixin(value = ClientPlayNetworkHandler.class, priority = -1001)
public class GSClientPlayNetworkHandlerMixin {

	@Shadow @Final private Connection connection;
	@Shadow private Minecraft minecraft;
	@Shadow private ClientWorld world;

	private static final int WORLD_TIME_UPDATE_INTERVAL = 20;
	private static final double IGNORE_TELEPORT_MAX_DISTANCE = 2.0; /* Must be > 0.51 */

	@Unique
	private GSClientController gs_controller;
	@Unique
	private GSTpsModule gs_tpsModule;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_tpsModule = gs_controller.getTpsModule();
		
		gs_controller.setNetworkHandler((ClientPlayNetworkHandler)(Object)this);
	}
	
	@Inject(
		method = "handleLogin",
		at = @At("RETURN")
	)
	private void onOnGameJoin(LoginS2CPacket packet, CallbackInfo ci) {
		gs_controller.onJoinServer();
	}

	@Inject(
		method = "handleEntityTeleport",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target = 
				"Lnet/minecraft/network/PacketUtils;ensureOnSameThread(" +
					"Lnet/minecraft/network/packet/Packet;" +
					"Lnet/minecraft/network/handler/PacketHandler;" +
					"Lnet/minecraft/util/BlockableEventLoop;" +
				")V"
		)
	)
	private void onOnEntityPosition(EntityTeleportS2CPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.cCorrectPistonPushing.get()) {
			Entity entity = world.getEntity(packet.getId());
			if (entity != null && isRecentlyMovedByPiston(entity)) {
				// Update the tracked position such that the entity position
				// does not get out of sync later.
				EntityTracker.updatePosition(entity, packet.getX(), packet.getY(), packet.getZ());
				ci.cancel();
			}
		}
	}

	@Inject(
		method = "handleEntityMove",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/PacketUtils;ensureOnSameThread(" +
					"Lnet/minecraft/network/packet/Packet;" +
					"Lnet/minecraft/network/handler/PacketHandler;" +
					"Lnet/minecraft/util/BlockableEventLoop;" +
				")V"
		)
	)
	private void onOnEntityUpdate(EntityMoveS2CPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.cCorrectPistonPushing.get()) {
			Entity entity = packet.getEntity(world);
			if (entity != null && isRecentlyMovedByPiston(entity)) {
				// See comment above.
				entity.packetX = entity.packetX + packet.getDx();
				entity.packetY = entity.packetY + packet.getDy();
				entity.packetZ = entity.packetZ + packet.getDz();
				
				if (!entity.isLogicalSideForUpdatingMovement()) {
					if (packet.hasAngles()) {
						// Do not ignore rotation changes.
						float yaw   = (float)(packet.getYaw()   * 360) / 256.0f;
						float pitch = (float)(packet.getPitch() * 360) / 256.0f;
						entity.updatePositionAndAngles(entity.x, entity.y, entity.z, yaw, pitch, 3, false);
					}
					
					entity.onGround = packet.getOnGround();
				}
				ci.cancel();
			}
		}
	}
	
	@Inject(
		method = "handlePlayerMove",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/PacketUtils;ensureOnSameThread(" +
					"Lnet/minecraft/network/packet/Packet;" +
					"Lnet/minecraft/network/handler/PacketHandler;" +
					"Lnet/minecraft/util/BlockableEventLoop;" +
				")V"
		)
	)
	private void onOnPlayerPositionLook(PlayerMoveS2CPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.cCorrectPistonPushing.get()) {
			// The server will inherently detect that the player moved in an incorrect way, if the
			// player was moved by a piston. In this case we ignore the update and send confirmation.
			// The confirmation is important, since we do not want the server to teleport the player
			// 20 ticks after it has been ignored.
			PlayerEntity player = minecraft.player;
			
			if (isRecentlyMovedByPiston(player)) {
				// Note: there might be a few issues with an actual teleport, if the player was just moved
				//       by a piston. But this should hopefully be solved by a simple distance check.
				boolean isDeltaX = packet.getRelativeArgs().contains(PlayerMoveS2CPacket.Argument.X);
				boolean isDeltaY = packet.getRelativeArgs().contains(PlayerMoveS2CPacket.Argument.Y);
				boolean isDeltaZ = packet.getRelativeArgs().contains(PlayerMoveS2CPacket.Argument.Z);
				
				double dx = isDeltaX ? packet.getX() : (packet.getX() - player.x);
				double dy = isDeltaY ? packet.getY() : (packet.getY() - player.y);
				double dz = isDeltaZ ? packet.getZ() : (packet.getZ() - player.z);
				
				if (Math.abs(dx) < IGNORE_TELEPORT_MAX_DISTANCE &&
				    Math.abs(dy) < IGNORE_TELEPORT_MAX_DISTANCE &&
				    Math.abs(dz) < IGNORE_TELEPORT_MAX_DISTANCE) {
					
					connection.send(new AcceptTeleportC2SPacket(packet.getTeleportId()));
					ci.cancel();
				}
			}
		}
	}
	
	@Unique
	private boolean isRecentlyMovedByPiston(Entity entity) {
		return (((GSIEntityAccess)entity).gs_isMovedByPiston() || ((GSIEntityAccess)entity).gs_wasMovedByPiston());
	}
	
	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadPacket<ClientPlayPacketHandler> payload = (GSICustomPayloadPacket<ClientPlayPacketHandler>)packet;
		
		GSIPacket gsPacket = packetManger.decodePacket(payload, gs_controller.getServerExtensionInfoList(), (ClientPlayNetworkHandler)(Object)this, this.minecraft);
		if (gsPacket != null) {
			gsPacket.handleOnClient(gs_controller);
			ci.cancel();
		}
	}

	@Inject(
		method = "handleWorldTime",
		at = @At("HEAD")
	)
	private void onWorldTimeSync(WorldTimeS2CPacket worldTimePacket, CallbackInfo ci) {
		// Check if handled by GSServerSyncPacket (gs server)
		if (!gs_controller.isG4mespeedServer() && !this.minecraft.isOnSameThread())
			gs_tpsModule.onServerSyncPacket(WORLD_TIME_UPDATE_INTERVAL);
	}
	
	@Redirect(
		method = "handleWorldChunk",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Iterator;hasNext()Z"
		)
	)
	private boolean replaceChunkDataBlockEntityLoop(Iterator<NbtCompound> itr) {
		// Note that Fabric Carpet changes parts of the loop, so we have
		// to override the entirety of the look by redirecting the condition.
		
		while(itr.hasNext()) {
			NbtCompound tag = itr.next();
			
			BlockPos blockPos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
			
			boolean pistonType = "minecraft:piston".equals(tag.getString("id"));
			
			if (pistonType) {
				// Because of a weird issue where the progress saved
				// by a piston is actually 1 gametick old we have to
				// increment the progress by 0.5.
				//
				// Make sure the block entity has actually ticked before
				// we increment the progress. Note that it is guaranteed
				// that the block entity has ticked if it is not a g4mespeed
				// server or if the immediate block updates setting is not
				// enabled.
				if (!gs_tpsModule.sImmediateBlockBroadcast.get() || !tag.contains("ticked") || tag.getBoolean("ticked"))
					tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
			}
			
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity != null) {
				blockEntity.readNbt(tag);
			} else if (pistonType) {
				// Make sure we're actually supposed to put
				// a moving piston block entity in this location...
				BlockState blockState = world.getBlockState(blockPos);
				if (blockState.getBlock() == Blocks.MOVING_BLOCK) {
					blockEntity = new MovingBlockEntity();
					blockEntity.readNbt(tag);
					world.setBlockEntity(blockPos, blockEntity);

					// Probably not needed but it's done in
					// other places so let's keep the standard.
					blockEntity.clearBlockCache();
				}
			}
		}
		
		return false;
	}

	@Inject(
		method = "handleBlockEntityUpdate",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/PacketUtils;ensureOnSameThread(" +
					"Lnet/minecraft/network/packet/Packet;" +
					"Lnet/minecraft/network/handler/PacketHandler;" +
					"Lnet/minecraft/util/BlockableEventLoop;" +
				")V"
		)
	)
	private void onOnBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.sParanoidMode.get()) {
			BlockPos pos = packet.getPos();
			
			if (packet.getType() == 0 && world.isChunkLoaded(pos)) {
				NbtCompound tag = packet.getNbt();

				if ("minecraft:piston".equals(tag.getString("id"))) {
					BlockState blockState = world.getBlockState(pos);
					BlockEntity blockEntity = world.getBlockEntity(pos);
					
					if (blockState.getBlock() != Blocks.MOVING_BLOCK) {
						blockState = Blocks.MOVING_BLOCK.defaultState();
						world.setBlockState(pos, blockState, 4 | 64 /* NO_REDRAW | MOVED */);
					}
					
					// See above redirect method.
					if (!gs_tpsModule.sImmediateBlockBroadcast.get() || !tag.contains("ticked") || tag.getBoolean("ticked"))
						tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
					
					if (blockEntity == null) {
						blockEntity = new MovingBlockEntity();
						blockEntity.readNbt(tag);
						world.setBlockEntity(pos, blockEntity);
					} else {
						blockEntity.readNbt(tag);
					}

					blockEntity.clearBlockCache();

					// Cancel vanilla handling of the packet.
					ci.cancel();
				}
			}
		}
	}

	@Inject(
		method = "handleBlockUpdate",
		at = @At("RETURN")
	)
	private void onOnBlockUpdateReturn(BlockUpdateS2CPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			scheduleRenderUpdateForFallingBlock(packet.getPos(), packet.getBlockState());
	}

	@Inject(
		method = "handleBlocksUpdate",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/PacketUtils;ensureOnSameThread(" +
					"Lnet/minecraft/network/packet/Packet;" +
					"Lnet/minecraft/network/handler/PacketHandler;" +
					"Lnet/minecraft/util/BlockableEventLoop;" +
				")V"
		)
	)
	private void onOnChunkDeltaUpdateRedirect(BlocksUpdateS2CPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.sParanoidMode.get()) {
			for (BlocksUpdateS2CPacket.BlockUpdate record : packet.getUpdates()) {
				if (record.getBlockState().getBlock() == Blocks.MOVING_BLOCK) {
					BlockState state = world.getBlockState(record.getBlockPos());
					
					if (state.getBlock() != Blocks.MOVING_BLOCK) {
						// By setting the block state to the state in the world, it
						// is equivalent to ignoring the block change.
						((GSIBlocksUpdateBlockUpdateAccess)record).setBlockState(state);
					}
				}
			}
		}
	}
	
	@Inject(
		method = "handleBlocksUpdate",
		at = @At("RETURN")
	)
	private void onOnChunkDeltaUpdateReturn(BlocksUpdateS2CPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED) {
			for (BlocksUpdateS2CPacket.BlockUpdate record : packet.getUpdates())
				scheduleRenderUpdateForFallingBlock(record.getBlockPos(), record.getBlockState());
		}
	}

	@Unique
	private void scheduleRenderUpdateForFallingBlock(BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof FallingBlock)
			((GSIWorldRendererAccess)minecraft.worldRenderer).gs_scheduleBlockUpdate(pos, true);
	}
}
