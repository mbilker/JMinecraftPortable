package us.mbilker.minecraftportable;

import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JPanel;

public class TransparentPanel extends JPanel
{
  private static final long serialVersionUID = 1L;
  private Insets insets;

  public TransparentPanel()
  {
  }

  public TransparentPanel(LayoutManager paramLayoutManager)
  {
    setLayout(paramLayoutManager);
  }

  @Override
public boolean isOpaque()
  {
    return false;
  }

  public void setInsets(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    insets = new Insets(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  @Override
public Insets getInsets()
  {
    if (insets == null) return super.getInsets();
    return insets;
  }
}

/* Location:           /Users/mbilker/Downloads/minecraft/
 * Qualified Name:     net.minecraft.TransparentPanel
 * JD-Core Version:    0.6.1
 */