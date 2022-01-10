package me.steven.pixelpets.abilities;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMultimap;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.UUID;

public class Abilities {

    public static final HashBiMap<Identifier, Ability> REGISTRY = HashBiMap.create();

    public static void loadBuiltins() {
        create(new Identifier(PixelPetsMod.MOD_ID, "builtin/sleep"), new AbilityAction() {
            @Override
            public int getCooldown() {
                return 120;
            }

            @Override
            public boolean onInteract(PetData petData, World world, LivingEntity entity) {
                if (entity instanceof PlayerEntity player) {
                    player.trySleep(player.getBlockPos());
                    return true;
                }
                return false;
            }
        });
    }

    private static void create(Identifier id, AbilityAction... actions) {
        REGISTRY.put(id, new Ability(id, actions));
    }

    /*public static final Ability SATURATION = new Ability.Builder(AbilityRarity.COMMON)
            .setId(new Identifier(PixelPetsMod.MOD_ID, "saturation"))
            .onInteract((stack, world, entity) -> {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 120, 0));
                return true;
            })
            .setCooldown(200)
            .build();

    public static final Ability PREY = new Ability.Builder(AbilityRarity.UNUSUAL)
            .setId(new Identifier(PixelPetsMod.MOD_ID, "prey"))
            .onInventoryTick((stack, world, entity) -> {
                if (entity instanceof PlayerEntity) {
                    boolean empty = entity.world.getEntitiesByClass(HostileEntity.class, new Box(entity.getBlockPos()).expand(6), (e) -> true).isEmpty();
                    if (!empty) {
                        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 240, 2));
                        return true;
                    }
                }
                return false;
            })
            .setCooldown(200)
            .build();

    public static final Ability INSTA_REGEN = new Ability.Builder(AbilityRarity.UNUSUAL)
            .setId(new Identifier(PixelPetsMod.MOD_ID, "instaregen"))
            .onInteract((stack, world, entity) -> {
                entity.heal(8f);
                return true;
            })
            .setCooldown(200)
            .build();

    private static final UUID MORE_HEALTH_UUID = UUID.randomUUID();

    public static final Ability MORE_HEARTS = new Ability.Builder(AbilityRarity.RARE)
            .setId(new Identifier(PixelPetsMod.MOD_ID, "more_hearts"))
            .setCooldown(0)
            .setEntityAttributeModifierProvider(() -> {
                ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(EntityAttributes.GENERIC_MAX_HEALTH,
                        new EntityAttributeModifier(
                                MORE_HEALTH_UUID, "More health", 8, EntityAttributeModifier.Operation.ADDITION
                        )
                );
                return builder.build();
            })
            .build();*/
}
