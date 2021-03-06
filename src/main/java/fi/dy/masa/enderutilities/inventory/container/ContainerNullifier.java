package fi.dy.masa.enderutilities.inventory.container;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacks;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.item.ItemNullifier;
import fi.dy.masa.enderutilities.item.ItemNullifier.ItemHandlerNullifier;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ContainerNullifier extends ContainerLargeStacks implements IContainerItem
{
    public final ItemHandlerNullifier inventoryItem;
    private final UUID containerUUID;
    private ItemStack stackLast = ItemStack.EMPTY;

    public ContainerNullifier(EntityPlayer player, ItemStack containerStack)
    {
        super(player, ItemNullifier.createInventoryForItem(containerStack, player.getEntityWorld().isRemote));
        this.containerUUID = NBTUtils.getUUIDFromItemStack(containerStack, "UUID", true);
        this.inventoryItem = (ItemHandlerNullifier) this.inventory;
        this.inventoryItem.setHostInventory(this.playerInv, this.containerUUID);
        this.inventoryNonWrapped = this.inventoryItem;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 69);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int xOff = 8;
        int yOff = 24;

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), this.inventory.getSlots());

        for (int slot = 0; slot < this.inventory.getSlots(); slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, xOff + slot * 18, yOff));
        }
    }

    @Override
    public ItemStack getContainerItem()
    {
        return InventoryUtils.getItemStackByUUID(this.playerInv, this.containerUUID, "UUID");
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.player.getEntityWorld().isRemote == false)
        {
            ItemStack stack = this.getContainerItem();

            // The Nullifier stack has changed (ie. to/from empty, or different instance), re-read the inventory contents.
            if (stack != this.stackLast)
            {
                this.inventoryItem.readFromContainerItemStack();
                this.stackLast = stack;
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        // Ctrl + Middle click: Void the contents of the slot
        if (EnumKey.MIDDLE_CLICK.matches(action, HotKeys.MOD_CTRL))
        {
            if (this.customInventorySlots.contains(element))
            {
                SlotItemHandlerGeneric slot = this.getSlotItemHandler(element);

                if (slot != null)
                {
                    slot.putStack(ItemStack.EMPTY);
                }
            }
        }
        else
        {
            super.performGuiAction(player, action, element);
        }
    }
}
