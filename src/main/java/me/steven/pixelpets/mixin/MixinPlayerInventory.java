package me.steven.pixelpets.mixin;

import me.steven.pixelpets.extensions.PixelPetsPlayerExtension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {

    @Shadow @Final public PlayerEntity player;

    @Inject(method = "updateItems", at = @At("HEAD"))
    private void pixelPets_clearPetsMap(CallbackInfo ci) {
        ((PixelPetsPlayerExtension) this.player).getTickingAbilities().clear();
    }
}
