package me.steven.pixelpets.abilities;

import com.google.common.collect.Multimap;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface AbilityAction {

    int getCooldown();

    default boolean onInteract(PetData petData, World world, LivingEntity entity) {
        return false;
    }

    default boolean inventoryTick(PetData petData, World world, LivingEntity entity) {
        return false;
    }

    default boolean onStackClick() {
        return false;
    }

    default boolean onDamaged(PetData petData, World world, LivingEntity entity) {
        return false;
    }

    @Nullable
    default StatusEffectInstance getPassiveEffect(World world, LivingEntity entity) {
        return null;
    }

    @Nullable
    default Multimap<EntityAttribute, EntityAttributeModifier> getEntityAttributeModifiers() {
        return null;
    }

    default boolean repels(EntityType<?> type) {
        return false;
    }

    static AbilityAction interact(AbilityContext ctx, int cooldown) {
        return new AbilityAction() {
            @Override
            public int getCooldown() {
                return cooldown;
            }

            @Override
            public boolean onInteract(PetData petData, World world, LivingEntity entity) {
                return ctx.test(petData, world, entity);
            }
        };
    }

    static AbilityAction inventoryTick(AbilityContext ctx, int cooldown) {
        return new AbilityAction() {
            @Override
            public int getCooldown() {
                return cooldown;
            }

            @Override
            public boolean inventoryTick(PetData petData, World world, LivingEntity entity) {
                return ctx.test(petData, world, entity);
            }
        };
    }
}
