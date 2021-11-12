package me.steven.pixelpets.mixin;

import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Redirect(method = "onEntityStatusEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)V"))
    private void pixelPets_setProviderClient(LivingEntity livingEntity, StatusEffectInstance effect, Entity source, EntityStatusEffectS2CPacket packet) {
        PixelPetsDataHolder ext = (PixelPetsDataHolder) effect;
        PixelPetsDataHolder packetExt = (PixelPetsDataHolder) packet;
        ext.setPetData(packetExt.getPetData());
        livingEntity.addStatusEffect(effect);
    }
}
