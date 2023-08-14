package me.steven.pixelpets.items;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PixelPetEggItem extends Item {
    public static final String HATCH_TICKS = "Hatching";
    public static final int TOTAL_HATCH_TIME = 120;

    private final Identifier eggGroupId;
    private final int color;
    public PixelPetEggItem(Identifier eggGroupId, int color) {
        super(new Item.Settings().maxCount(1));
        this.color = color;
        this.eggGroupId = eggGroupId;
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName().copy().styled(s -> s.withColor(color));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) return;
        NbtCompound tag = stack.getOrCreateNbt();
        if (!tag.contains(HATCH_TICKS)) {
            tag.putInt(HATCH_TICKS, TOTAL_HATCH_TIME);
        } else if (entity.getVelocity().x != 0 || entity.getVelocity().z != 0) {
            int hatching = tag.getInt(HATCH_TICKS);
            tag.putInt(HATCH_TICKS, hatching - 1);
            if (hatching <= 0) {
                hatch((PlayerEntity) entity, stack, slot);
            }
        }
    }
    private void hatch(PlayerEntity player, ItemStack stack, int slot) {
        NbtCompound tag = stack.getOrCreateNbt();
        PetData data;
        if (tag.contains(PetData.PET_DATA_ID)) {
            data = PetData.fromTag(stack);
        } else {
            data = PetData.createFromGroupId(eggGroupId, player.getRandom());
        }

        if (data == null) {
            player.getInventory().setStack(slot, ItemStack.EMPTY);
            return;
        }

        ItemStack petStack = new ItemStack(PixelPetsMod.PET_ITEM);
        player.getInventory().setStack(slot, data.update(petStack));
        player.sendMessage(Text.literal(data.getNickname()).styled(s -> s.withColor(data.getPet().getColor(data.getVariant()))).append(Text.literal(" has hatched!").formatted(Formatting.WHITE)));
    }
}
