package me.steven.pixelpets.json.abilities;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Function;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.AbilityAction;
import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import me.steven.pixelpets.pets.PetData;
import me.steven.pixelpets.abilities.AbilityContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AbilitySupplierParser {
    public static Optional<AbilityContext> parse(JsonObject object) {
        if (object == null) return Optional.empty();
        List<AbilityContext> actions = new ArrayList<>();
        Optional<Function<Entity, Boolean>> condition = object.has("condition") ? AbilityParser.parseEntityCondition(object.getAsJsonObject("condition")) : Optional.empty();

        if (object.has("effect")) {
            Optional<Supplier<StatusEffectInstance>> statusOptional = AbilityParser.parseStatusEffect(object.getAsJsonObject("effect"));
            statusOptional.ifPresent(statusEffectInstanceSupplier -> actions.add((petData, world, entity) -> {
                StatusEffectInstance effect = statusEffectInstanceSupplier.get();
                PixelPetsDataHolder ext = (PixelPetsDataHolder) effect;
                ext.setPetData(petData);
                entity.addStatusEffect(effect);
                return true;
            }));
        }

        if (object.has("heal")) {
            Optional<Supplier<Integer>> heal = AbilityParser.parseIntProvider(object.get("heal"));
            heal.ifPresent(integerSupplier -> actions.add((stack, world, entity) -> {
                entity.setHealth(entity.getHealth() + integerSupplier.get());
                return true;
            }));
        }

        if (object.has("give")) {
            Optional<Supplier<ItemStack>> give = AbilityParser.parseStackProvider(object.get("give").getAsJsonObject());
            give.ifPresent(itemStackSupplier -> actions.add((stack, world, entity) -> {
                ItemScatterer.spawn(world, entity.getX(), entity.getY(), entity.getZ(), itemStackSupplier.get());
                return true;
            }));
        }

        if (object.has("air")) {
            Optional<Supplier<Integer>> air = AbilityParser.parseIntProvider(object.get("air"));
            air.ifPresent(integerSupplier -> actions.add((stack, world, entity) -> {
                entity.setAir(Math.min(entity.getAir() + integerSupplier.get(), entity.getMaxAir()));
                return true;
            }));
        }

        if (object.has("show_item")) {
            Optional<Supplier<ItemStack>> stackProvider = Optional.empty();
            if (object.get("show_item").isJsonObject()) {
                stackProvider = AbilityParser.parseStackProvider(object.get("show_item").getAsJsonObject());
            } else if (object.get("show_item").isJsonPrimitive() && object.get("show_item").getAsString().equals("this")) {
                stackProvider = Optional.of(() -> ItemStack.EMPTY);
            }
            if (stackProvider.isPresent()) {
                Optional<Supplier<ItemStack>> finalStackProvider = stackProvider;
                actions.add((petData, world, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        ItemStack stack = finalStackProvider.get().get();
                        if (stack.isEmpty()) {
                            stack = new ItemStack(PixelPetsMod.PET_ITEM);
                            stack.setSubNbt("PetData", petData.toTag());
                        }
                        buf.writeItemStack(stack);
                        ServerPlayNetworking.send(player, PixelPetsMod.SHOW_ITEM_PACKET, buf);
                        return true;
                    }
                    return false;
                });
            }
        }

        return Optional.of((petData, world, entity) -> {
            if (condition.isEmpty() || condition.get().apply(entity)) {
                for (AbilityContext action : actions) {
                    action.test(petData, world, entity);
                }
                return true;
            }
            return false;
        });
    }
}
