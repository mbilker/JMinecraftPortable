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