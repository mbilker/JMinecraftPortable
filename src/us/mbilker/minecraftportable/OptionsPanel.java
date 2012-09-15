package us.mbilker.minecraftportable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class OptionsPanel extends JDialog
{
  private static final long serialVersionUID = 1L;

  public OptionsPanel(Frame paramFrame)
  {
    super(paramFrame);

    setModal(true);

    JPanel localJPanel1 = new JPanel(new BorderLayout());
    JLabel localJLabel = new JLabel("Launcher options", 0);
    localJLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
    localJLabel.setFont(new Font("Default", 1, 16));
    localJPanel1.add(localJLabel, "North");

    JPanel localJPanel2 = new JPanel(new BorderLayout());
    JPanel localJPanel3 = new JPanel(new GridLayout(0, 1));
    JPanel localJPanel4 = new JPanel(new GridLayout(0, 1));
    localJPanel2.add(localJPanel3, "West");
    localJPanel2.add(localJPanel4, "Center");

    final JButton localJButton1 = new JButton("Force update!");
    localJButton1.addActionListener(new ActionListener() {
      @Override
	public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
        GameUpdater.forceUpdate = true;
        localJButton1.setText("Will force!");
        localJButton1.setEnabled(false);
      }
    });
    localJPanel3.add(new JLabel("Force game update: ", 4));
    localJPanel4.add(localJButton1);

    localJPanel3.add(new JLabel("Game location on disk: ", 4));
    TransparentLabel local2 = new TransparentLabel(Util.getWorkingDirectory().toString())
    {
      private static final long serialVersionUID = 0L;

      @Override
	public void paint(Graphics paramAnonymousGraphics) {
        super.paint(paramAnonymousGraphics);

        int i = 0;
        int j = 0;

        FontMetrics localFontMetrics = paramAnonymousGraphics.getFontMetrics();
        int k = localFontMetrics.stringWidth(getText());
        int m = localFontMetrics.getHeight();

        if (getAlignmentX() == 2.0F) i = 0;
        else if (getAlignmentX() == 0.0F) i = getBounds().width / 2 - k / 2;
        else if (getAlignmentX() == 4.0F) i = getBounds().width - k;
        j = getBounds().height / 2 + m / 2 - 1;

        paramAnonymousGraphics.drawLine(i + 2, j, i + k - 2, j);
      }

      @Override
	public void update(Graphics paramAnonymousGraphics)
      {
        paint(paramAnonymousGraphics);
      }
    };
    local2.setCursor(Cursor.getPredefinedCursor(12));
    local2.addMouseListener(new MouseAdapter()
    {
      @Override
	public void mousePressed(MouseEvent paramAnonymousMouseEvent) {
        try {
          Util.openLink(Util.getWorkingDirectory().toURI());
        } catch (Exception localException) {
          localException.printStackTrace();
        }
      }
    });
    local2.setForeground(new Color(2105599));

    localJPanel4.add(local2);

    localJPanel1.add(localJPanel2, "Center");

    JPanel localJPanel5 = new JPanel(new BorderLayout());
    localJPanel5.add(new JPanel(), "Center");
    JButton localJButton2 = new JButton("Done");
    localJButton2.addActionListener(new ActionListener() {
      @Override
	public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
        setVisible(false);
      }
    });
    localJPanel5.add(localJButton2, "East");
    localJPanel5.setBorder(new EmptyBorder(16, 0, 0, 0));

    localJPanel1.add(localJPanel5, "South");

    add(localJPanel1);
    localJPanel1.setBorder(new EmptyBorder(16, 24, 24, 24));
    pack();
    setLocationRelativeTo(paramFrame);
  }
}