package net.kodiakweb.worsestorage.blocks;

import net.kodiakweb.worsestorage.Worsestorage;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class TypeBundle extends BundleItem {
    private static final String ITEMS_KEY = "Items";
    public static final int MAX_STORAGE = 128;
    private static final int BUNDLE_ITEM_OCCUPANCY = 4;
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4f, 0.4f, 1.0f);
    public TypeBundle(Item.Settings settings) {
        super(settings);

    }
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) { //runs if you click *while holding the bundle*
        if (clickType != ClickType.RIGHT) {
            return false;
        }
        ItemStack itemStack = slot.getStack();
        if (itemStack.isEmpty()) {
            this.playInsertSound(player);
            TypeBundle.removeFirstStack(stack).ifPresent(removedStack -> TypeBundle.addToBundle(stack, slot.insertStack((ItemStack)removedStack)));
        } else if (itemStack.getItem().canBeNested()) {
            int i = (64 - TypeBundle.getBundleOccupancy(stack)) / TypeBundle.getItemOccupancy(itemStack); //
            int j = TypeBundle.addToBundle(stack, slot.takeStackRange(itemStack.getCount(), i, player));
            if (j > 0) {
                this.playInsertSound(player);
            }
        }
        return true;
    }
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false;
        }
        if (otherStack.isEmpty()) {
            TypeBundle.removeFirstStack(stack).ifPresent(itemStack -> {
                this.playRemoveOneSound(player);
                cursorStackReference.set((ItemStack)itemStack);
            });
        } else {
            int i = TypeBundle.addToBundle(stack, otherStack);
            if (i > 0) {
                this.playInsertSound(player);
                otherStack.decrement(i);
            }
        }
        return true;
    }
    public static int addToBundle(ItemStack bundle, ItemStack stack) {
        if (stack.isEmpty() || !stack.getItem().canBeNested()) {
            return 0;
        }
        NbtCompound nbtCompound = bundle.getOrCreateNbt();
        if (!nbtCompound.contains(ITEMS_KEY)) {
            nbtCompound.put(ITEMS_KEY, new NbtList());
        }
        int i = TypeBundle.getBundleOccupancy(bundle);
        int j = TypeBundle.getItemOccupancy(stack);
        int k = Math.min(stack.getCount(), (128 - i) / j);
        if (k == 0) {
            return 0;
        }
        NbtList nbtList = nbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        Optional<NbtCompound> optional = TypeBundle.canMergeStack(stack, nbtList);
        if (optional.isPresent()) {
            NbtCompound nbtCompound2 = optional.get();
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound2);
            itemStack.increment(k);
            itemStack.writeNbt(nbtCompound2);
            nbtList.remove(nbtCompound2);
            nbtList.add(0, nbtCompound2);
        } else {
            ItemStack itemStack2 = stack.copy();
            itemStack2.setCount(k);
            NbtCompound nbtCompound3 = new NbtCompound();
            itemStack2.writeNbt(nbtCompound3);
            nbtList.add(0, nbtCompound3);
        }
        return k;
    }

    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.minecraft.bundle.fullness", BundleItem.getBundleOccupancy(stack), 128).formatted(Formatting.GRAY));
    }
    public int getItemBarStep(ItemStack stack) {
        return Math.min(1 + 12 * BundleItem.getBundleOccupancy(stack) / 128, 13);
    }
    public static Optional<NbtCompound> canMergeStack(ItemStack stack, NbtList items) {
        if (stack.isOf(Items.BUNDLE)){
            return Optional.empty();
        }
        return items.stream().filter(NbtCompound.class::isInstance).map(NbtCompound.class::cast).filter(item -> TypeBundle.canCombine(ItemStack.fromNbt(item), stack)).findFirst();
    }
    public static boolean canCombine(ItemStack stack, ItemStack otherStack) {
        return stack.isOf(otherStack.getItem()) && ItemStack.areNbtEqual(stack, otherStack) && ((otherStack.getCount() + stack.getCount()) <=64);
    }
}
