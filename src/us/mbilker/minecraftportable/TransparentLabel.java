package us.mbilker.minecraftportable;

import java.awt.Color;
import javax.swing.JLabel;

public class TransparentLabel extends JLabel
{
  private static final long serialVersionUID = 1L;

  public TransparentLabel(String paramString, int paramInt)
  {
    super(paramString, paramInt);
    setForeground(Color.WHITE);
  }

  public TransparentLabel(String paramString) {
    super(paramString);
    setForeground(Color.WHITE);
  }

  @Override
public boolean isOpaque()
  {
    return false;
  }
}