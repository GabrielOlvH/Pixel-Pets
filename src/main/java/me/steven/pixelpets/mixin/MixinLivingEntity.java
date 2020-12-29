package me.steven.pixelpets.mixin;

import com.google.common.collect.Multimap;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.pets.PixelPet;
import me.steven.pixelpets.player.PixelPetsPlayerExtension;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements PixelPetsPlayerExtension {

    @Shadow public abstract AttributeContainer getAttributes();

    private final Map<PixelPet, Set<Ability>> pixelPets_pets = new HashMap<>();
    private final Map<PixelPet, Set<Ability>> pixelPets_appliedPets = new HashMap<>();

    @Inject(method = "method_30129", at = @At("TAIL"))
    private void pixelPets_addExtraAttributes(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        pixelPets_pets.forEach((pet, abilities) -> abilities.forEach(ability -> {
            if (ability != null && (!pixelPets_appliedPets.containsKey(pet) || !pixelPets_appliedPets.get(pet).contains(ability))) {
                Multimap<EntityAttribute, EntityAttributeModifier> attributes = ability.getEntityAttributeModifiers();
                if (attributes != null)
                    getAttributes().addTemporaryModifiers(attributes);
                pixelPets_appliedPets.put(pet, abilities);
            }
        }));

        pixelPets_appliedPets.entrySet().removeIf(entry -> {
            PixelPet pet = entry.getKey();
            Set<Ability> abilities = entry.getValue();
            abilities.removeIf(ability -> {
                if (ability != null && (!pixelPets_pets.containsKey(pet) || !pixelPets_pets.get(pet).contains(ability))) {
                    Multimap<EntityAttribute, EntityAttributeModifier> attributes = ability.getEntityAttributeModifiers();
                    if (attributes != null)
                        getAttributes().removeModifiers(attributes);
                    return true;
                }
                return false;
            });
            return abilities.isEmpty();
        });
    }

    @Override
    public Map<PixelPet, Set<Ability>> getInventoryPets() {
        return pixelPets_pets;
    }
}
