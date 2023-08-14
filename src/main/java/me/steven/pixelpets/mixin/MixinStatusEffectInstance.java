package me.steven.pixelpets.mixin;

import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public class MixinStatusEffectInstance implements PixelPetsDataHolder {

    @Unique
    @Nullable
    private PetData petProvider = null;

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void pixelPets_savePetProvider(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        if (petProvider != null)
            tag.put(PetData.PET_DATA_ID, petProvider.toTag());
    }

    @Inject(method = "fromNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/effect/StatusEffectInstance;", at = @At("RETURN"))
    private static void pixelPets_loadPetProvider(NbtCompound tag, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (tag.contains(PetData.PET_DATA_ID, 10)) {
            PetData providerPet = PetData.fromTag(tag.getCompound(PetData.PET_DATA_ID));
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
