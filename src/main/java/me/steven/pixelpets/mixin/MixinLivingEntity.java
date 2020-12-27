package me.steven.pixelpets.mixin;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.pets.PixelPet;
import me.steven.pixelpets.player.PixelPetsPlayerExtension;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements PixelPetsPlayerExtension {

    @Shadow public abstract AttributeContainer getAttributes();

    private final Map<PixelPet, PetData> pixelPets_pets = new HashMap<>();
    private final Map<PixelPet, PetData> pixelPets_appliedPets = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void pixelPets_clearPetsMap(CallbackInfo ci) {
        pixelPets_pets.clear();
    }

    @Inject(method = "method_30129", at = @At("TAIL"))
    private void pixelPets_addExtraAttributes(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        pixelPets_pets.forEach((pet, data) -> {
            Ability ability = Abilities.REGISTRY.get(data.getSelected());
            if (!pixelPets_appliedPets.containsKey(pet) && ability != null) {
                getAttributes().addTemporaryModifiers(ability.getEntityAttributeModifiers());
                pixelPets_appliedPets.put(pet, data);
            }
        });

        pixelPets_appliedPets.entrySet().removeIf(entry -> {
            PixelPet pet = entry.getKey();
            PetData data = entry.getValue();

            Ability ability = Abilities.REGISTRY.get(data.getSelected());
            if (!pixelPets_pets.containsKey(pet) && ability != null) {
                getAttributes().removeModifiers(ability.getEntityAttributeModifiers());
                return true;
            }
            return false;
        });
    }

    @Override
    public Map<PixelPet, PetData> getInventoryPets() {
        return pixelPets_pets;
    }
}
