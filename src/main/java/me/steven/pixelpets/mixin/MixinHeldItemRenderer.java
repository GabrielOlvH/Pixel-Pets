package me.steven.pixelpets.mixin;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.housing.HousingData;
import me.steven.pixelpets.pets.PetData;
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
        if (updated.getItem().equals(original.getItem()) && updated.getItem().equals(PixelPetsMod.OVERWORLD_EGG_ITEM)) {
            return true;
        } else if (updated.getItem().equals(original.getItem()) && updated.getItem().equals(PixelPetsMod.PET_ITEM)) {
            PetData originalPet = PetData.fromTag(original);
            PetData updatedPet = PetData.fromTag(updated);
            if (originalPet.getPetId().equals(updatedPet.getPetId())) return true;
        } else if (updated.getItem().equals(original.getItem()) && updated.getItem().equals(PixelPetsMod.HOUSING_ITEM)) {
            HousingData originalHosing = HousingData.fromTag(original);
            HousingData updatedHousing = HousingData.fromTag(updated);
            if (originalHosing.getId().equals(updatedHousing.getId())) return true;
        }

        return ItemStack.areEqual(original, updated);
    }
}