package me.steven.pixelpets.housing;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

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
            stack.setSubNbt(PetData.PET_DATA_ID, petData.toTag());
            int length = stack.getName().getString().length();
            if (length > width) width = length;
        }

        for (PetData petData : data.housingData().getEggs()) {
            ItemStack stack = new ItemStack(PixelPetsMod.OVERWORLD_EGG_ITEM);
            int length = stack.getName().getString().length();
            if (length > width) width = length;
        }
        return width;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        if (!Screen.hasShiftDown()) return;
        int i = 0;
        for (PetData petData : data.housingData().getStoredPets()) {
            ItemStack stack = new ItemStack(PixelPetsMod.PET_ITEM);
            stack.setSubNbt(PetData.PET_DATA_ID, petData.toTag());
            context.drawItemInSlot(textRenderer, stack, x, y + i * 18);
            i++;
        }

        for (PetData petData : data.housingData().getEggs()) {
            ItemStack stack = new ItemStack(PixelPetsMod.OVERWORLD_EGG_ITEM);
            context.drawItemInSlot(textRenderer, stack, x, y + i * 18);
            i++;
        }
    }
    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
        if (!Screen.hasShiftDown()) return;
        int i = 0;
        for (PetData petData : data.housingData().getStoredPets()) {
            ItemStack stack = new ItemStack(PixelPetsMod.PET_ITEM);
            stack.setSubNbt(PetData.PET_DATA_ID, petData.toTag());
            textRenderer.draw(stack.getName(), x + 20, y + (textRenderer.fontHeight/2f) + i * 18, -1, false, matrix4f, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            i++;
        }

        for (PetData petData : data.housingData().getEggs()) {
            ItemStack stack = new ItemStack(PixelPetsMod.OVERWORLD_EGG_ITEM);
            textRenderer.draw(stack.getName(), x + 20, y + (textRenderer.fontHeight/2f) + i * 18, -1, false, matrix4f, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            i++;
        }
    }
}
