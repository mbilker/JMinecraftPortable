package us.mbilker.minecraftportable;

import java.awt.Color;
import javax.swing.JCheckBox;

public class TransparentCheckbox extends JCheckBox
{
  private static final long serialVersionUID = 1L;

  public TransparentCheckbox(String paramString)
  {
    super(paramString);
    setForeground(Color.WHITE);
  }

  @Override
public boolean isOpaque()
  {
    return false;
  }
}

/* Location:           /Users/mbilker/Downloads/minecraft/
 * Qualified Name:     net.minecraft.TransparentCheckbox
 * JD-Core Version:    0.6.1
 */