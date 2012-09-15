package us.mbilker.minecraftportable;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class LogoPanel extends JPanel
{
  private static final long serialVersionUID = 1L;
  private Image bgImage;

  public LogoPanel()
  {
    setOpaque(true);
    try
    {
      BufferedImage localBufferedImage = ImageIO.read(LoginForm.class.getResource("logo.png"));
      int i = localBufferedImage.getWidth();
      int j = localBufferedImage.getHeight();
      bgImage = localBufferedImage.getScaledInstance(i, j, 16);
      setPreferredSize(new Dimension(i + 32, j + 32));
    } catch (IOException localIOException) {
      localIOException.printStackTrace();
    }
  }

  @Override
public void update(Graphics paramGraphics)
  {
    paint(paramGraphics);
  }

  @Override
public void paintComponent(Graphics paramGraphics)
  {
    paramGraphics.drawImage(bgImage, 24, 24, null);
  }
}

/* Location:           /Users/mbilker/Downloads/minecraft/
 * Qualified Name:     net.minecraft.LogoPanel
 * JD-Core Version:    0.6.1
 */