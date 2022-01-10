package me.steven.pixelpets.mixin;

import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityStatusEffectS2CPacket.class)
public class MixinEntityStatusEffectS2CPacket implements PixelPetsDataHolder {
    private PetData petProvider;

    @Inject(method = "<init>(ILnet/minecraft/entity/effect/StatusEffectInstance;)V", at = @At("TAIL"))
    private void pixelPets_initPacket(int entityId, StatusEffectInstance effect, CallbackInfo ci) {
        PixelPetsDataHolder ext = (PixelPetsDataHolder) effect;
        this.petProvider = ext.getPetData();
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void pixelPets_readPetProvider(PacketByteBuf buf, CallbackInfo ci) {
        boolean hasProvider = buf.readBoolean();
        if (hasProvider) {
            NbtCompound compoundTag = buf.readNbt();
            if (compoundTag != null)
                this.petProvider = PetData.fromTag(compoundTag.getCompound("PetData"));
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void pixelPets_writePetProvider(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeBoolean(petProvider != null);
        if (petProvider != null) {
            NbtCompound tag = new NbtCompound();
            tag.put("PetData", petProvider.toTag());
            buf.writeNbt(tag);
        }
    }

    @Override
    public @Nullable PetData getPetData() {
        return petProvider;
    }

    @Override
    public void setPetData(PetData data) {
    }
}
