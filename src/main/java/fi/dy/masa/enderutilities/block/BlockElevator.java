package fi.dy.masa.enderutilities.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderElevator;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockElevator extends BlockEnderUtilitiesTileEntity
{
    public static final AxisAlignedBB BOUNDS_SLAB   = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    public static final AxisAlignedBB BOUNDS_LAYER  = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2 / 16D, 1.0D);
    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.<EnumDyeColor>create("color", EnumDyeColor.class);

    public BlockElevator(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(COLOR, EnumDyeColor.WHITE));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { COLOR });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        String[] names = new String[EnumDyeColor.values().length];

        int i = 0;
        for (EnumDyeColor color : EnumDyeColor.values())
        {
            names[i++] = this.blockName + "_" + color.getName();
        }

        return names;
    }

    @Override
    protected String[] generateTooltipNames()
    {
        // Use a common tooltip by adding exactly one entry into the array
        return new String[] { this.blockName };
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityEnderElevator();
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(COLOR).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(COLOR).getMetadata();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // Don't try to set the facing as the elevator doesn't have one, which is what the super would do
        return state;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(playerIn, ItemDye.class);

        if (stack != null)
        {
            EnumDyeColor stackColor = EnumDyeColor.byDyeDamage(stack.getMetadata());

            if (state.getValue(COLOR) != stackColor)
            {
                if (worldIn.isRemote == false)
                {
                    worldIn.setBlockState(pos, state.withProperty(COLOR, stackColor), 3);

                    if (playerIn.capabilities.isCreativeMode == false)
                    {
                        stack.stackSize--;
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
    {
        if (this == EnderUtilitiesBlocks.blockElevatorSlab)
        {
            return BOUNDS_SLAB;
        }
        else if (this == EnderUtilitiesBlocks.blockElevatorLayer)
        {
            return BOUNDS_LAYER;
        }

        return FULL_BLOCK_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    /*
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (EnumDyeColor color : EnumDyeColor.values())
        {
            list.add(new ItemStack(item, 1, color.getMetadata()));
        }
    }
    */

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
}
