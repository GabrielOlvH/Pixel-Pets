package me.steven.pixelpets.mixin;

import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import me.steven.pixelpets.items.PetData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.CompoundTag;
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

    @Inject(method = "read", at = @At("TAIL"))
    private void pixelPets_readPetProvider(PacketByteBuf buf, CallbackInfo ci) {
        boolean hasProvider = buf.readBoolean();
        if (hasProvider) {
            CompoundTag compoundTag = buf.readCompoundTag();
            if (compoundTag != null)
                this.petProvider = PetData.fromTag(compoundTag);
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void pixelPets_writePetProvider(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeBoolean(petProvider != null);
        if (petProvider != null) {
            CompoundTag tag = new CompoundTag();
            tag.put("PetData", petProvider.toTag());
            buf.writeCompoundTag(tag);
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
