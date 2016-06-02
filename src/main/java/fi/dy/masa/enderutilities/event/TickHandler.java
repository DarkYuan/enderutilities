package fi.dy.masa.enderutilities.event;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.event.tasks.TaskScheduler;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class TickHandler
{
    private int serverTickCounter;
    private int playerTickCounter;

    public void Tickhandler()
    {
        this.serverTickCounter = 0;
        this.playerTickCounter = 0;
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        // Once every second
        if (++this.serverTickCounter >= 20)
        {
            this.serverTickCounter = 0;

            ChunkLoading.getInstance().tickChunkTimeouts();
        }

        TaskScheduler.getInstance().runTasks();

        ++this.playerTickCounter;
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.side == Side.CLIENT || event.phase == TickEvent.Phase.END || event.player == null)
        {
            return;
        }

        // Once every 2 seconds
        if (this.playerTickCounter % 40 == 0)
        {
            if (event.player.isRiding() == true && event.player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.mobHarness)))
            {
                ItemMobHarness.addAITask(event.player.getRidingEntity(), false);
            }

            ItemStack stack = EntityUtils.getHeldItemOfType(event.player, IChunkLoadingItem.class);
            if (stack != null)
            {
                NBTTagCompound nbt = stack.getTagCompound();

                // If the player is holding an item that requires a chunk to stay loaded, refresh the timeout value
                if (nbt != null && nbt.getBoolean("ChunkLoadingRequired") == true)
                {
                    TargetData target;

                    // Note: There is the possibility that the target or the selected link crystal
                    // has been changed since the chunk loading first started, but it just means
                    // that the refreshing will not happen, or will happen to the new target chunk,
                    // (the one currently active in the item) if that also happens to be chunk loaded by us.

                    // In case of modular items, we get the target info from the selected module (= Link Crystal)
                    if (stack.getItem() instanceof IModular)
                    {
                        target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
                    }
                    else
                    {
                        target = TargetData.getTargetFromItem(stack);
                    }

                    if (target != null)
                    {
                        ChunkLoading.getInstance().refreshChunkTimeout(target.dimension, target.pos.getX() >> 4, target.pos.getZ() >> 4);
                    }
                }
            }
        }

        BlockPos pos = EntityUtils.getPositionOfBlockEntityIsCollidingWith(event.player.worldObj, event.player, EnderUtilitiesBlocks.blockPortal);

        if (pos != null)
        {
            //IBlockState state = event.player.worldObj.getBlockState(pos);
            //state.getBlock().onEntityCollidedWithBlock(event.player.worldObj, pos, state, event.player);

            TileEntity te = event.player.worldObj.getTileEntity(pos);
            if (te instanceof TileEntityPortal)
            {
                TargetData target = ((TileEntityPortal) te).getDestination();

                if (target != null)
                {
                    OwnerData owner = ((TileEntityPortal) te).getOwner();
                    if (owner == null || owner.canAccess(event.player))
                    {
                        TeleportEntity.teleportEntityUsingTarget(event.player, target, true, true);
                        //TeleportEntity.teleportEntity(entityIn, -1070, 6, -450, entityIn.dimension, true, true);
                    }
                }
            }
        }

        PlayerTaskScheduler.getInstance().runTasks(event.player.worldObj, event.player);
    }
}
