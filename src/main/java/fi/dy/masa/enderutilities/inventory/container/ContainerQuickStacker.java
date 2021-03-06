package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerCustomSlotClick;
import fi.dy.masa.enderutilities.item.ItemQuickStacker;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ContainerQuickStacker extends ContainerCustomSlotClick implements IContainerItem
{
    public ContainerQuickStacker(EntityPlayer player, ItemStack containerStack)
    {
        super(player, null);

        this.addPlayerInventorySlots(25, 45);
        this.addOffhandSlot(25 - 18, 45 - 18);
    }

    @Override
    public ItemStack getContainerItem()
    {
        return ItemQuickStacker.getEnabledItem(this.player);
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        ItemStack stack = ItemQuickStacker.getEnabledItem(player);

        // Middle click
        if (clickType == ClickType.CLONE && dragType == 2 && stack.isEmpty() == false)
        {
            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;

            if (invSlotNum != -1)
            {
                byte selected = NBTUtils.getByte(stack, ItemQuickStacker.TAG_NAME_CONTAINER, ItemQuickStacker.TAG_NAME_PRESET_SELECTION);
                long mask = NBTUtils.getLong(stack, ItemQuickStacker.TAG_NAME_CONTAINER, ItemQuickStacker.TAG_NAME_PRESET + selected);
                mask ^= (0x1L << invSlotNum);
                NBTUtils.setLong(stack, ItemQuickStacker.TAG_NAME_CONTAINER, ItemQuickStacker.TAG_NAME_PRESET + selected, mask);
            }

            return ItemStack.EMPTY;
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }
}
