package me.steven.pixelpets.abilities;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import me.steven.pixelpets.utils.InventoryTick;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Ability {

    Identifier getId();
    AbilityRarity getRarity();
    boolean onInteract(PlayerEntity playerEntity);
    boolean inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected);
    int getCooldown();
    @Nullable
    Multimap<EntityAttribute, EntityAttributeModifier> getEntityAttributeModifiers();

    default String getTranslationKey() {
        return "ability." + getId().getNamespace() + "." + getId().getPath();
    }

    class Builder {
        private Identifier id;
        private final AbilityRarity rarity;
        private Function<PlayerEntity, Boolean> interact = (user) -> false;
        private InventoryTick inventoryTick = (stack, world, entity, slot, selected) ->  false;
        private Supplier<Multimap<EntityAttribute, EntityAttributeModifier>> attributes = ImmutableMultimap::of;
        private int cooldown;

        public Builder(AbilityRarity rarity) {
            this.rarity = rarity;
        }

        public Builder setId(Identifier id) {
            this.id = id;
            return this;
        }

        public Builder onInteract(Function<PlayerEntity, Boolean> interact) {
            this.interact = interact;
            return this;
        }

        public Builder onInventoryTick(InventoryTick inventoryTick) {
            this.inventoryTick = inventoryTick;
            return this;
        }

        public Builder setCooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder setEntityAttributeModifierProvider(Supplier<Multimap<EntityAttribute, EntityAttributeModifier>> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Ability build() {
            Objects.requireNonNull(id, "id cannot be null");
            Ability ability = new Ability() {
                @Override
                public Identifier getId() {
                    return id;
                }

                @Override
                public AbilityRarity getRarity() {
                    return rarity;
                }

                @Override
                public boolean onInteract(PlayerEntity playerEntity) {
                    return interact.apply(playerEntity);
                }

                @Override
                public boolean inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
                    return inventoryTick.inventoryTick(stack, world, entity, slot, selected);
                }

                @Override
                public int getCooldown() {
                    return cooldown;
                }

                @Override
                public @Nullable Multimap<EntityAttribute, EntityAttributeModifier> getEntityAttributeModifiers() {
                    return attributes.get();
                }
            };
            Abilities.REGISTRY.put(ability.getId(), ability);
            return ability;
        }


    }
}
