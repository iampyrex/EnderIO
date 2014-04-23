package crazypants.enderio.fluid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.fuels.IronEngineFuel.Fuel;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import crazypants.enderio.Config;
import crazypants.enderio.machine.power.PowerDisplayUtil;
import crazypants.util.Lang;

public class BucketHandler {

  public static BucketHandler instance = new BucketHandler();

  static {
    MinecraftForge.EVENT_BUS.register(instance);
  }

  private Map<Block, Item> buckets = new HashMap<Block, Item>();

  private BucketHandler() {
  }

  public void registerFluid(Block fluidBlock, Item fullBucket) {
    buckets.put(fluidBlock, fullBucket);
  }

  @SubscribeEvent
  public void onBucketFill(FillBucketEvent event) {
    ItemStack res = getFilledBucket(event.world, event.target);
    if(res != null) {
      event.result = res;
      event.setResult(Result.ALLOW);
    }
  }

  private ItemStack getFilledBucket(World world, MovingObjectPosition pos) {

    Block block = world.getBlock(pos.blockX, pos.blockY, pos.blockZ);
    Item bucket = buckets.get(block);
    if(bucket != null && world.getBlockMetadata(pos.blockX, pos.blockY, pos.blockZ) == 0) {
      world.setBlockToAir(pos.blockX, pos.blockY, pos.blockZ);
      return new ItemStack(bucket);
    } else {
      return null;
    }
  }

  @SubscribeEvent
  public void addFuelTooltip(ItemTooltipEvent evt) {
    ItemStack stack = evt.itemStack;
    if(stack == null) {
      return;
    }

    if(Config.addFuelTooltipsToAllFluidContainers || stack.getItem() instanceof ItemBucketEio) {
      FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);
      if(fluidStack == null) {
        return;
      }
      addTooltipForFluid(evt.toolTip, fluidStack.getFluid());
    }
  }

  protected void addTooltipForFluid(List list, Fluid fluid) {
    if(fluid != null) {
      Fuel fuel = IronEngineFuel.getFuelForFluid(fluid);
      if(fuel != null) {
        list.add(Lang.localize("fuel.tooltip.heading"));
        list.add(EnumChatFormatting.ITALIC + " " + PowerDisplayUtil.formatPowerPerTick(fuel.powerPerCycle));
        list.add(EnumChatFormatting.ITALIC + " " + fuel.totalBurningTime + " " + Lang.localize("fuel.tooltip.burnTime"));
      }
    }
  }

}
