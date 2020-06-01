package com.g4mesoft.mixin.client;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.packet.GSICustomPayloadHolder;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayNetworkHandler.class)
public class GSClientPlayNetworkHandlerMixin {

	private static final int WORLD_TIME_UPDATE_INTERVAL = 20;
	
	@Shadow private MinecraftClient client;
	@Shadow private ClientWorld world;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(CallbackInfo ci) {
		GSControllerClient.getInstance().setNetworkHandler((ClientPlayNetworkHandler)(Object) this);
	}

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadHolder<ClientPlayPacketListener> payload = (GSICustomPayloadHolder<ClientPlayPacketListener>)packet;
		
		GSControllerClient controllerClient = GSControllerClient.getInstance();
		GSVersion serverVersion = controllerClient.getServerVersion();
		GSIPacket gsPacket = packetManger.decodePacket(payload, serverVersion, (ClientPlayNetworkHandler)(Object)this, this.client);
		if (gsPacket != null) {
			gsPacket.handleOnClient(controllerClient);
			ci.cancel();
		}
	}

	@Inject(method = "onWorldTimeUpdate", at = @At("HEAD"))
	public void onWorldTimeSync(WorldTimeUpdateS2CPacket worldTimePacket, CallbackInfo ci) {
		// Handled by GSServerSyncPacket
		GSControllerClient controller = GSControllerClient.getInstance();
		if (controller.isG4mespeedServer())
			return;
		
		if (!this.client.isOnThread())
			controller.getTpsModule().onServerSyncPacket(WORLD_TIME_UPDATE_INTERVAL);
	}
	
	@Redirect(method = "onChunkData", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
	public boolean replaceChunkDataBlockEntityLoop(Iterator<CompoundTag> itr) {
		// Might be possible to replace only part 
		// of the loop but that would be a hassle.
		
		while(itr.hasNext()) {
			CompoundTag tag = itr.next();
			
			BlockPos blockPos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
			
			boolean pistonType = "minecraft:piston".equals(tag.getString("id"));
			
			if (pistonType) {
				// Because of a weird issue where the progress
				// saved by a piston is actually 1 gametick old
				// we have to increment the progress by 0.5.
				tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
			}
			
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity != null) {
				blockEntity.fromTag(tag);
			} else if (pistonType) {
				// Make sure we're actually supposed to put
				// a moving piston block entity in this location...
				BlockState blockState = world.getBlockState(blockPos);
				if (blockState.getBlock() == Blocks.MOVING_PISTON) {
					blockEntity = new PistonBlockEntity();
					blockEntity.fromTag(tag);
					world.setBlockEntity(blockPos, blockEntity);
					
					// Probably not needed but it's done in
					// other places so let's keep the standard.
					blockEntity.resetBlock();
				}
			}
		}
		
		return false;
	}
}
