package me.steven.pixelpets.mixin;

import me.steven.pixelpets.PixelPetsMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {
    @Redirect(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean areStacksEqual(ItemStack original, ItemStack updated) {
        if (updated.getItem().equals(original.getItem()) && (updated.getItem().equals(PixelPetsMod.PET_ITEM) || updated.getItem().equals(PixelPetsMod.OVERWORLD_EGG_ITEM) )) {
            return true;
        }

        return ItemStack.areEqual(original, updated);
    }
}