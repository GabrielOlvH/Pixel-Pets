package me.steven.pixelpets.mixin;

import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import me.steven.pixelpets.items.PetData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public class MixinStatusEffectInstance implements PixelPetsDataHolder {

    @Nullable
    private PetData petProvider = null;

    @Inject(method = "toTag", at = @At("RETURN"))
    private void pixelPets_savePetProvider(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (petProvider != null)
            tag.put("PetData", petProvider.toTag());
    }

    @Inject(method = "fromTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/entity/effect/StatusEffectInstance;", at = @At("RETURN"))
    private static void pixelPets_loadPetProvider(CompoundTag tag, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (tag.contains("PetData", 10)) {
            PetData providerPet = PetData.fromTag(tag.getCompound("PetData"));
            StatusEffectInstance instance = cir.getReturnValue();
            ((PixelPetsDataHolder) instance).setPetData(providerPet);
        }
    }

    @Override
    public @Nullable PetData getPetData() {
        return petProvider;
    }

    @Override
    public void setPetData(PetData data) {
        this.petProvider = data;
    }
}
