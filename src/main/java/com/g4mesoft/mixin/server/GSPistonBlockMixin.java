package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import com.g4mesoft.core.GSController;

import net.minecraft.block.PistonBlock;

@Mixin(PistonBlock.class)
public class GSPistonBlockMixin {

	@ModifyArg(method = "move", require = 1, index = 2, at = @At(value = "INVOKE", ordinal = 0,
			target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private int modifySetAirBlockFlags(int flags) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sParanoidMode.getValue())
			flags |= 0x02;
		return flags;
	}
	
	@ModifyArg(method = "move", require = 2, index = 2, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
			slice = @Slice(from = @At(value = "FIELD", ordinal = 0, shift = Shift.BEFORE, target = "Lnet/minecraft/block/Blocks;MOVING_PISTON:Lnet/minecraft/block/Block;"),
			               to = @At(value = "INVOKE", ordinal = 0, shift = Shift.BEFORE, target = "Ljava/util/Set;iterator()Ljava/util/Iterator;")))
	private int modifySetMovingBlockFlags(int flags) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sParanoidMode.getValue())
			flags |= 0x02;
		return flags;
	}
}
