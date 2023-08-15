package me.steven.pixelpets.items;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.housing.HousingData;
import me.steven.pixelpets.housing.HousingTooltipData;
import me.steven.pixelpets.pets.PetData;
import me.steven.pixelpets.utils.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HousingItem extends Item {

    public HousingItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        Item item = otherStack.getItem();
        if (clickType == ClickType.LEFT && item instanceof PixelPetItem) {
            PetData petData = PetData.fromTag(otherStack);
            boolean store = store(petData, stack);
            if (store) {
                cursorStackReference.set(ItemStack.EMPTY);
                return true;
            }
        } else if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            HousingData housingData = HousingData.fromTag(stack);

            List<PetData> eggs = housingData.getEggs();
            if (!eggs.isEmpty()) {
                PetData petData = eggs.remove(0);
                ItemStack eggStack = new ItemStack(PixelPetsMod.OVERWORLD_EGG_ITEM);
                eggStack.setSubNbt(PetData.PET_DATA_ID, petData.toTag());
                stack.setSubNbt("HousingData", housingData.toNbt());
                cursorStackReference.set(eggStack);
            } else {
                List<PetData> storedPets = housingData.getStoredPets();
                if (storedPets.isEmpty()) return false;
                PetData petData = storedPets.remove(0);
                ItemStack petStack = new ItemStack(PixelPetsMod.PET_ITEM);
                petStack.setSubNbt(PetData.PET_DATA_ID, petData.toTag());
                stack.setSubNbt("HousingData", housingData.toNbt());
                cursorStackReference.set(petStack);
            }
            return true;
        }
        return false;
    }

    private boolean store(PetData data, ItemStack housingStack) {
        HousingData housingData = HousingData.fromTag(housingStack);
        Identifier typeId = data.getPet().getHousingId();
        if (housingData.getStoredPets().size() >= housingData.getHousing().capacity()
                || typeId == null
                || !typeId.equals(housingData.getId())
        ) return false;
        housingData.getStoredPets().add(data);
        housingStack.setSubNbt("HousingData", housingData.toNbt());
        return true;
    }

    @Override
    public Text getName(ItemStack stack) {
        HousingData housingData = HousingData.fromTag(stack);
        Identifier id = housingData.getId();
        return Text.translatable("item." + id.getNamespace() + ".housing." + id.getPath()).setStyle(Style.EMPTY.withColor(housingData.getHousing().color()));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        HousingData housingData = HousingData.fromTag(stack);
        if (housingData.getStoredPets().isEmpty() && !Screen.hasShiftDown()) {
            tooltip.add(Text.literal("No pets inside!"));
        } else {
            if (!Screen.hasShiftDown()) {
                tooltip.add(Text.literal(housingData.getStoredPets().size() + " pets inside!"));
                tooltip.add(Text.empty());
                tooltip.add(Text.literal("Press Shift to see more."));
            }
            tooltip.add(Text.literal("Press Right Click to pick them up."));
        }
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new HousingTooltipData(HousingData.fromTag(stack)));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) return;
        tick(stack, world, entity);

    }

    private static void tick(ItemStack stack, World world, Entity entity) {

        HousingData housingData = HousingData.fromTag(stack);
        for (PetData petData : housingData.getStoredPets()) {
            PixelPetItem.tick(petData, null, world, entity);
        }

        stack.setSubNbt("HousingData", housingData.toNbt());
        tickBreedingLogic(stack, world, entity);
    }

    private static void tickBreedingLogic(ItemStack stack, World world, Entity entity) {
        HousingData housingData = HousingData.fromTag(stack);
        List<PetData> storedPets = housingData.getStoredPets();
        if (storedPets.size() != 2 || !housingData.getEggs().isEmpty()) {
            stack.removeSubNbt("BreedingProgress");
            return;
        }

        PetData first = storedPets.get(0);
        PetData second = storedPets.get(1);

        if (first.getCooldown() > 0 || second.getCooldown() > 0 || !first.getPetId().equals(second.getPetId())) {
            stack.removeSubNbt("BreedingProgress");
            return;
        }

        if (entity.getVelocity().x == 0 && entity.getVelocity().z == 0) return;

        NbtCompound nbt = stack.getOrCreateNbt();
        int breedingProgress = nbt.getInt("BreedingProgress") + 1;
        nbt.putInt("BreedingProgress", breedingProgress);

        if (breedingProgress >= Config.INSTANCE.breedingTicks) {
            PetData babyData = new PetData(first.getPetId());

            first.setCooldown(1200);
            first.setTotalCooldown(1200);
            second.setCooldown(1200);
            second.setTotalCooldown(1200);
            nbt.remove("BreedingProgress");
            housingData.getEggs().add(babyData);
            stack.setSubNbt("HousingData", housingData.toNbt());
            entity.sendMessage(Text.literal("An egg has been laid in ").append(stack.getName()));
        }
    }
}
