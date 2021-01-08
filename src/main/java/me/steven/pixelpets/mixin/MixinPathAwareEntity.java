package me.steven.pixelpets.mixin;

import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.player.PixelPetsPlayerExtension;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PathAwareEntity.class)
public class MixinPathAwareEntity extends MobEntity {
    protected MixinPathAwareEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void f(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        if (world != null && !world.isClient()) {
            goalSelector.add(3, new FleeEntityGoal<>((PathAwareEntity) (Object) this, PlayerEntity.class, (p) -> {
                for (Map<Ability, PetData> abilities : ((PixelPetsPlayerExtension) p).getTickingAbilities().values()) {
                    for (Ability ability : abilities.keySet()) {
                        if (ability.repels(entityType)) return true;
                    }
                }
                return false;
            }, 4.0F, 1.0D, 1.2D, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test));
        }
    }
}
