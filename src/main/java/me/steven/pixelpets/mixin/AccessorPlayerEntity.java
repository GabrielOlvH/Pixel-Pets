package me.steven.pixelpets.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface AccessorPlayerEntity {
    @Accessor
    void setSleepTimer(int s);

}
