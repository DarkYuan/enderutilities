package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerCreationStation extends ContainerLargeStacks
{
    protected final TileEntityCreationStation tecs;
    public int selectionsLast; // action mode and module selection
    public int modeMask;
    public int fuelProgress;
    public int smeltProgress;

    public final InventoryItemCrafting[] craftMatrices;
    private final IInventory[] craftResults;
    private final IItemHandler furnaceInventory;
    private SlotRange craftingGridSlotsLeft;
    private SlotRange craftingGridSlotsRight;
    private int lastInteractedCraftingGridId;

    public ContainerCreationStation(EntityPlayer player, TileEntityCreationStation te)
    {
        super(player, te.getItemInventory());
        this.tecs = te;
        this.tecs.openInventory(player);

        this.craftMatrices = new InventoryItemCrafting[] { te.getCraftingInventory(0, this, player), te.getCraftingInventory(1, this, player) };
        this.craftResults = new IInventory[] { te.getCraftResultInventory(0), te.getCraftResultInventory(1) };
        this.furnaceInventory = this.tecs.getFurnaceInventory();
        this.lastInteractedCraftingGridId = 0;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(40, 174);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 40;
        int posY = 102;

        // Item inventory slots
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        this.customInventorySlots = new SlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        // Add the module slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posX = 216;
        posY = 102;

        // The Storage Module slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.tecs.getMemoryCardInventory(), i, posX, posY + i * 18));
        }

        // Crafting slots, left side
        this.craftingGridSlotsLeft = new SlotRange(this.inventorySlots.size(), 9);
        posX = 40;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.craftMatrices[0], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrices[0], this.craftResults[0], 0, 112, 33));

        // Crafting slots, right side
        this.craftingGridSlotsRight = new SlotRange(this.inventorySlots.size(), 9);
        posX = 148;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.craftMatrices[1], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrices[1], this.craftResults[1], 0, 112, 69));

        // Add the furnace slots as priority merge slots
        //this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 6);

        // Furnace slots, left side
        // Smeltable items
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 0, 8, 8));
        // Fuel
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 1, 8, 51));
        // Output
        this.addSlotToContainer(new SlotItemHandlerFurnaceOutput(this.player, this.furnaceInventory, 2, 40, 8));

        // Furnace slots, right side
        // Smeltable items
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 3, 216, 8));
        // Fuel
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 4, 216, 51));
        // Output
        this.addSlotToContainer(new SlotItemHandlerFurnaceOutput(this.player, this.furnaceInventory, 5, 184, 8));

        this.onCraftMatrixChanged(this.craftMatrices[0]);
    }

    /**
     * Get the SlotRange for the given crafting grid id.
     * 0 = Left, 1 = Right
     */
    public SlotRange getCraftingGridSlotRange(int id)
    {
        return id == 1 ? this.craftingGridSlotsRight : this.craftingGridSlotsLeft;
    }

    public int getLastInteractedCraftingGridId()
    {
        return this.lastInteractedCraftingGridId;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv)
    {
        super.onCraftMatrixChanged(inv);

        this.craftResults[0].setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[0], this.player.worldObj));
        this.craftResults[1].setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[1], this.player.worldObj));
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        this.tecs.closeInventory(player);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot)
    {
        return slot.inventory != this.craftResults[0] && slot.inventory != this.craftResults[0] && super.canMergeSlot(stack, slot);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our main item inventory or the furnace inventory
        if (slot instanceof SlotItemHandler)
        {
            SlotItemHandler slotItemHandler = (SlotItemHandler)slot;
            if (slotItemHandler.itemHandler == this.inventory || slotItemHandler.itemHandler == this.tecs.getFurnaceInventory())
            {
                return slotItemHandler.getItemStackLimit(stack);
            }
        }

        // Player inventory, module slots or crafting slots
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    public boolean transferStackFromSlot(EntityPlayer player, int slotNum)
    {
        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (slotNum == 40 || slotNum == 50)
        {
            int invId = slotNum == 50 ? 1 : 0;

            if (this.tecs.canCraftItems(invId) == false)
            {
                return false;
            }

            boolean ret = super.transferStackFromSlot(player, slotNum);
            this.tecs.restockCraftingGrid(invId);

            return ret;
        }
        // Crafting grid slots, try to merge to the main item inventory first
        else if (this.isSlotInRange(this.craftingGridSlotsLeft, slotNum) == true || this.isSlotInRange(this.craftingGridSlotsRight, slotNum) == true)
        {
            if (this.transferStackToSlotRange(player, slotNum, this.customInventorySlots.first, this.customInventorySlots.lastExc, false) == true)
            {
                return true;
            }
        }

        return super.transferStackFromSlot(player, slotNum);
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        // Update the "last interacted on" crafting grid id, used for JEI recipe filling
        if (this.isSlotInRange(this.craftingGridSlotsLeft, slotNum) == true || slotNum == 40)
        {
            this.lastInteractedCraftingGridId = 0;
        }
        else if (this.isSlotInRange(this.craftingGridSlotsRight, slotNum) == true || slotNum == 50)
        {
            this.lastInteractedCraftingGridId = 1;
        }

        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (slotNum == 40 || slotNum == 50)
        {
            int invId = slotNum == 50 ? 1 : 0;

            if (this.tecs.canCraftItems(invId) == false)
            {
                return null;
            }

            ItemStack stack = super.slotClick(slotNum, button, type, player);
            this.tecs.restockCraftingGrid(invId);

            return stack;
        }

        return super.slotClick(slotNum, button, type, player);
    }

    @Override
    public void onCraftGuiOpened(ICrafting icrafting)
    {
        super.onCraftGuiOpened(icrafting);

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModule();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        icrafting.sendProgressBarUpdate(this, 0, modeMask);
        icrafting.sendProgressBarUpdate(this, 1, selection);
        icrafting.sendProgressBarUpdate(this, 2, fuelProgress);
        icrafting.sendProgressBarUpdate(this, 3, smeltProgress);

        this.detectAndSendChanges();
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.tecs.getWorld().isRemote == true)
        {
            return;
        }

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModule();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.modeMask != modeMask)
            {
                icrafting.sendProgressBarUpdate(this, 0, modeMask);
            }
            if (this.selectionsLast != selection)
            {
                icrafting.sendProgressBarUpdate(this, 1, selection);
            }
            if (this.fuelProgress != fuelProgress)
            {
                icrafting.sendProgressBarUpdate(this, 2, fuelProgress);
            }
            if (this.smeltProgress != smeltProgress)
            {
                icrafting.sendProgressBarUpdate(this, 3, smeltProgress);
            }
        }

        this.modeMask = modeMask;
        this.selectionsLast = selection;
        this.fuelProgress = fuelProgress;
        this.smeltProgress = smeltProgress;
    }

    @Override
    public void updateProgressBar(int var, int val)
    {
        super.updateProgressBar(var, val);

        switch (var)
        {
            case 0:
                this.modeMask = val;
                break;
            case 1:
                this.tecs.setSelectedModule(val & 0x3); // 0..3
                this.tecs.setQuickMode((val >> 2) & 0x7); // 0..5
                this.tecs.inventoryChanged(TileEntityCreationStation.INV_ID_MODULES, 0); // The slot is not used
                break;
            case 2:
                this.fuelProgress = val; // value is 0..100, left furnace is in the lower bits 7..0
                break;
            case 3:
                this.smeltProgress = val; // value is 0..100, left furnace is in the lower bits 7..0
                break;
            default:
        }
    }
}
