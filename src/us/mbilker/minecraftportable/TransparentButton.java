package us.mbilker.minecraftportable;

import javax.swing.JButton;

public class TransparentButton extends JButton
{
  private static final long serialVersionUID = 1L;

  public TransparentButton(String paramString)
  {
    super(paramString);
  }

  @Override
public boolean isOpaque()
  {
    return false;
  }
}

/* Location:           /Users/mbilker/Downloads/minecraft/
 * Qualified Name:     net.minecraft.TransparentButton
 * JD-Core Version:    0.6.1
 */