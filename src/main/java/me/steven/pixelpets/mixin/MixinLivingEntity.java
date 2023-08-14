package me.steven.pixelpets.mixin;

import com.google.common.collect.Multimap;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityAction;
import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import me.steven.pixelpets.extensions.PixelPetsPlayerExtension;
import me.steven.pixelpets.pets.PetData;
import me.steven.pixelpets.pets.PixelPet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements PixelPetsPlayerExtension {

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract AttributeContainer getAttributes();

    @Shadow public abstract ItemStack getMainHandStack();

    @Shadow public abstract ItemStack getOffHandStack();

    private final Map<PixelPet, Map<AbilityAction, PetData>> pixelPets_pets = new HashMap<>();
    private final Map<PixelPet, Map<AbilityAction, PetData>> pixelPets_appliedPets = new HashMap<>();

    @Inject(method = "getEquipmentChanges", at = @At("TAIL"))
    private void pixelPets_addExtraAttributes(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        pixelPets_pets.forEach((pet, abilities) -> abilities.forEach((ability, data) -> {
            if (ability != null && (!pixelPets_appliedPets.containsKey(pet) || !pixelPets_appliedPets.get(pet).containsKey(ability))) {
                Multimap<EntityAttribute, EntityAttributeModifier> attributes = ability.getEntityAttributeModifiers();
                if (attributes != null)
                    getAttributes().addTemporaryModifiers(attributes);
                StatusEffectInstance passiveEffect = ability.getPassiveEffect(entity.getWorld(), entity);
                if (passiveEffect != null) {
                    PixelPetsDataHolder ext = (PixelPetsDataHolder) passiveEffect;
                    ext.setPetData(data);
                    entity.removeStatusEffect(passiveEffect.getEffectType());
                    entity.setStatusEffect(passiveEffect, null);
                }
                pixelPets_appliedPets.computeIfAbsent(pet, (a) -> new HashMap<>()).putAll(abilities);
            }
        }));

        tickAppliedAbilities();
    }

    private void tickAppliedAbilities() {
        LivingEntity entity = (LivingEntity) (Object) this;
        pixelPets_appliedPets.entrySet().removeIf(entry -> {
            PixelPet pet = entry.getKey();
            entry.getValue().entrySet().removeIf(e -> {
                AbilityAction ability = e.getKey();
                PetData data = e.getValue();
                if (ability != null && (!pixelPets_pets.containsKey(pet) || !pixelPets_pets.get(pet).containsKey(ability))) {
                    Multimap<EntityAttribute, EntityAttributeModifier> attributes = ability.getEntityAttributeModifiers();
                    if (attributes != null)
                        getAttributes().removeModifiers(attributes);
                    StatusEffectInstance passiveEffect = ability.getPassiveEffect(entity.getWorld(), entity);
                    if (passiveEffect != null) {
                        PixelPetsDataHolder appliedExt = (PixelPetsDataHolder) entity.getStatusEffect(passiveEffect.getEffectType());
                        if (appliedExt != null) {
                            PetData providerPet = appliedExt.getPetData();
                            if (providerPet != null && providerPet.getAbilityId().equals(data.getAbilityId()))
                                entity.removeStatusEffect(passiveEffect.getEffectType());
                        }
                    }
                    return true;
                }
                return false;
            });
            return entry.getValue().isEmpty();
        });
    }

    @Override
    public Map<PixelPet, Map<AbilityAction, PetData>> getTickingAbilities() {
        return pixelPets_pets;
    }

    @Inject(method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isDead()Z",
                    shift = At.Shift.BEFORE,
                    ordinal = 1
    ))
    private void pixelpets_onDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack;
        if (getMainHandStack().isOf(PixelPetsMod.PET_ITEM)) {
            stack = getMainHandStack();
        } else if (getOffHandStack().isOf(PixelPetsMod.PET_ITEM)) {
            stack = getOffHandStack();
        } else return;
        PetData data = PetData.fromTag(stack);
        AbilityAction ability = data.getAbilityAction();
        if (ability != null && data.getCooldown() <= 0 && ability.onDamaged(data, getWorld(), (LivingEntity) (Object) this) ) {
            data.setCooldown(ability.getCooldown());
            data.setTotalCooldown(ability.getCooldown());
            data.update(stack);
        }
    }

    @Inject(method = "isSleepingInBed", at = @At("RETURN"), cancellable = true)
    private void pixelpets_sleepOffOfBed(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && this.getMainHandStack().isOf(PixelPetsMod.PET_ITEM)) {
            ItemStack stack = this.getMainHandStack();
            PetData petData = PetData.fromTag(stack);
            if (petData.getAbilityId().equals(new Identifier(PixelPetsMod.MOD_ID, "builtin/sleep"))) {
                cir.setReturnValue(true);
            }
         }
    }
}
