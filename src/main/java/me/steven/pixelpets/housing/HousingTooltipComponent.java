package me.steven.pixelpets.housing;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Matrix4f;

public record HousingTooltipComponent(HousingTooltipData data) implements TooltipComponent {

    @Override
    public int getHeight() {
        if (Screen.hasShiftDown()) return data.housingData().getStoredPets().size() * 18 + data.housingData().getEggs().size() * 18 + 4;
        else return 0;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        if (!Screen.hasShiftDown()) return 0;
        int width = 0;
        for (PetData petData : data.housingData().getStoredPets()) {
            ItemStack stack = new ItemStack(PixelPetsMod.PET_ITEM);
            stack.setSubNbt("PetData", petData.toTag());
            int length = stack.getName().asString().length();
            if (length > width) width = length;
        }

        for (PetData petData : data.housingData().getEggs()) {
            ItemStack stack = new ItemStack(PixelPetsMod.EGG_ITEM);
            int length = stack.getName().asString().length();
            if (length > width) width = length;
        }
        return width;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        if (!Screen.hasShiftDown()) return;
        int i = 0;
        for (PetData petData : data.housingData().getStoredPets()) {
            ItemStack stack = new ItemStack(PixelPetsMod.PET_ITEM);
            stack.setSubNbt("PetData", petData.toTag());
            itemRenderer.renderInGui(stack, x, y + i * 18);
            itemRenderer.renderGuiItemOverlay(textRenderer, stack, x, y + i * 18);
            i++;
        }

        for (PetData petData : data.housingData().getEggs()) {
            ItemStack stack = new ItemStack(PixelPetsMod.EGG_ITEM);
            itemRenderer.renderInGuiWithOverrides(stack, x, y + i * 18);
            itemRenderer.renderGuiItemOverlay(textRenderer, stack, x, y + i * 18);
            i++;
        }
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
        if (!Screen.hasShiftDown()) return;
        int i = 0;
        for (PetData petData : data.housingData().getStoredPets()) {
            ItemStack stack = new ItemStack(PixelPetsMod.PET_ITEM);
            stack.setSubNbt("PetData", petData.toTag());
            textRenderer.draw(stack.getName(), x + 20, y + (textRenderer.fontHeight/2f) + i * 18, -1, false, matrix4f, immediate, false, 0, 15728880);
            i++;
        }

        for (PetData petData : data.housingData().getEggs()) {
            ItemStack stack = new ItemStack(PixelPetsMod.EGG_ITEM);
            textRenderer.draw(stack.getName(), x + 20, y + (textRenderer.fontHeight/2f) + i * 18, -1, false, matrix4f, immediate, false, 0, 15728880);
            i++;
        }
    }
}
