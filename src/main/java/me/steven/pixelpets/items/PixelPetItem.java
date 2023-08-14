package me.steven.pixelpets.items;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityAction;
import me.steven.pixelpets.extensions.PixelPetsPlayerExtension;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PixelPetItem extends Item {

    public PixelPetItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        PetData data = PetData.fromTag(stack);
        if (data.getAbilityId() != null) {
            Ability ability = Abilities.REGISTRY.get(data.getAbilityId());
            tooltip.add(Text.translatable("item.pixelpets.pet.ability", Text.translatable(ability.getTranslationKey().formatted(Formatting.WHITE)).formatted(Formatting.DARK_PURPLE)));
            if (Screen.hasShiftDown()) {
                tooltip.add(Text.translatable(ability.getTranslationKey() + ".usage").formatted(Formatting.GRAY));
                if (data.getCooldown() > 0) {
                    tooltip.add(Text.empty());
                    tooltip.add(Text.translatable("item.pixelpets.pet.cooldown", data.getCooldown()).formatted(Formatting.GRAY, Formatting.ITALIC));
                }
            }
            else {
                tooltip.add(Text.translatable(ability.getTranslationKey() + ".description").formatted(Formatting.YELLOW, Formatting.ITALIC));
                tooltip.add(Text.literal("Press SHIFT to see more").formatted(Formatting.GRAY));
            }
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        PetData petData = PetData.fromTag(stack);
        return petData.toText();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.pass(stack);
        PetData data = PetData.fromTag(stack);
        if (data.getCooldown() <= 0) {
            AbilityAction ability = data.getAbilityAction();
            if (ability != null && ability.onInteract(data, world, user)) {
                data.setCooldown(ability.getCooldown());
                data.setTotalCooldown(ability.getCooldown());
                data.update(stack);
            }
        } else return TypedActionResult.pass(stack);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        PetData data = PetData.fromTag(stack);
        boolean update = tick(data, stack, world, entity);
        if (update) {
            data.update(stack);
        }
    }

    @Override
    public int getItemBarStep(ItemStack itemStack) {
        PetData data = PetData.fromTag(itemStack);
        return (int)((data.getTotalCooldown() - data.getCooldown()) * 13.0f / data.getTotalCooldown());
    }

    @Override
    public boolean isItemBarVisible(ItemStack itemStack) {
        PetData data = PetData.fromTag(itemStack);
        return data.getCooldown() > 0 && Abilities.REGISTRY.containsKey(data.getAbilityId());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        PetData data = PetData.fromTag(stack);
        return data.getPet().getColor(data.getVariant());
    }

    public static boolean tick(PetData data, @Nullable ItemStack stack, World world, Entity entity) {
        if (world.isClient()) return false;
        if (data.getAbilityId() == null) {
            List<Identifier> abilities = new ArrayList<>();

            for (int i = 0; i < data.getPet().getAbilities().length; i++) {
                int times = data.getPet().getAbilities().length - i;
                for (int j = 0; j < times; j++) {
                    abilities.add(data.getPet().getAbilities()[i].id());
                }
            }

            Identifier ability = abilities.get(world.random.nextInt(abilities.size()));
            data.setAbilityId(ability);
            return true;
        }

        if (data.getCooldown() <= 0) {
            AbilityAction ability = data.getAbilityAction();
            if (ability != null && stack != null) {
                if (entity instanceof PixelPetsPlayerExtension) {
                    ((PixelPetsPlayerExtension) entity).getTickingAbilities().computeIfAbsent(data.getPet(), (a) -> new HashMap<>()).put(ability, data);
                }
                if (ability.inventoryTick(data, world, (LivingEntity) entity)) {
                    data.setCooldown(ability.getCooldown());
                    data.setTotalCooldown(ability.getCooldown());
                    return true;
                }
            }
        } else {
            data.setCooldown(data.getCooldown() - 1);
            return true;
        }

        return false;
    }
}
