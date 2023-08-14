package me.steven.pixelpets.abilities;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMultimap;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.mixin.AccessorPlayerEntity;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
                if (entity instanceof ServerPlayerEntity player) {
                    player.sleep(player.getBlockPos());
                    ((AccessorPlayerEntity) player).setSleepTimer(0);
                    ((ServerWorld) world).updateSleepingPlayers();
                    return true;
                }
                return false;
            }
        }, AbilitySource.REROLL);
    }

    private static void create(Identifier id, AbilityAction action, AbilitySource source) {
        REGISTRY.put(id, new Ability(id, action, source));
    }
}
