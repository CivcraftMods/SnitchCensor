package com.biggestnerd.snitchcensor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

@Mod(modid="snitchcensor", name="Snitch Censor", version="v2.1")
public class SnitchCensor
{
  Minecraft mc;
  private KeyBinding toggle;
  Pattern snitch = Pattern.compile("^ \\* ([a-zA-Z0-9_]+) (entered|logged out in|logged in to) snitch at (.*?)\\[(world.*?) ([-]?[0-9]+) ([-]?[0-9]+) ([-]?[0-9]+)\\]$");
  private boolean censor = true;
  
  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event)
  {
    this.mc = Minecraft.getMinecraft();
    this.toggle = new KeyBinding("Toggle Censor", Keyboard.KEY_I, "Snitch Censor");
    ClientRegistry.registerKeyBinding(this.toggle);
    MinecraftForge.EVENT_BUS.register(this);
    FMLCommonHandler.instance().bus().register(this);
  }
  
  @SubscribeEvent
  public void onChat(ClientChatReceivedEvent event)
  {
    if (!this.censor) {
      return;
    }
    String msg = event.message.getUnformattedText();
    Matcher snitchMatcher = this.snitch.matcher(msg);
    while (snitchMatcher.find())
    {
      event.setCanceled(true);
      String censoredSnitch = " * " + snitchMatcher.group(1) + " " + snitchMatcher.group(2) + " snitch at " + snitchMatcher.group(3) + " [" + snitchMatcher.group(4);
      int x = Integer.parseInt(snitchMatcher.group(5));
      int y = Integer.parseInt(snitchMatcher.group(6));
      int z = Integer.parseInt(snitchMatcher.group(7));
      int xx = (int) (x - mc.thePlayer.posX);
      int yy = (int) (y - mc.thePlayer.posY);
      int zz = (int) (z - mc.thePlayer.posZ);
      int distance = (int)Math.sqrt(xx * xx + yy * yy + zz * zz);
      if(getWorldFromName(snitchMatcher.group(4)) == mc.thePlayer.dimension) {
    	  censoredSnitch += " " + distance + "m]";
      } else {
    	  censoredSnitch += "]";
      }
      this.mc.thePlayer.addChatMessage(new ChatComponentText(formatString(EnumChatFormatting.AQUA, censoredSnitch)));
    }
  }
  
  @SubscribeEvent
  public void onKeyInput(KeyInputEvent event)
  {
    if (this.toggle.isKeyDown())
    {
      this.censor = (!this.censor);
      this.mc.thePlayer.addChatMessage(new ChatComponentText(formatString(EnumChatFormatting.DARK_AQUA, "[SnitchCensor]") + formatString(EnumChatFormatting.GRAY, new StringBuilder().append(" is now ").append(this.censor ? "enabled" : "disabled").toString())));
    }
  }
  
  private String formatString(EnumChatFormatting color, String msg)
  {
    String[] split = msg.split(" ");
    StringBuilder out = new StringBuilder();
    for (String part : split) {
      out.append(color + part + " ");
    }
    return out.toString();
  }
  
  public int getWorldFromName(String worldName) {
	  if(worldName.equalsIgnoreCase("world")) {
		  return 0;
	  } else if (worldName.equalsIgnoreCase("world_nether")) {
		  return -1;
	  } else if (worldName.equals("world_the_end")) {
		  return 1;
	  }
	  return 2;
  }
}
