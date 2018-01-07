package com.mindliner.synch.outlook.gui.contact;

import javax.swing.*;
import java.awt.*;

public class EtchedLine extends JPanel
{
   private Color mHighlightColor = Color.white;
   private Color mShadowColor = Color.gray;
   private Dimension mPrefferedSize = new Dimension(100, 2);

   public EtchedLine()
   {
      super();
   }

   public EtchedLine(Color highlight, Color shadow)
   {
      super();
      mHighlightColor = highlight;
      mShadowColor = shadow;
   }

   public void paint(Graphics g)
   {
      int w = this.getWidth();

      g.setColor(mHighlightColor);
      g.drawLine(0, 0, w, 0);
      g.setColor(mShadowColor);
      g.drawLine(0, 1, w, 1);
   }

   public Dimension getPreferredSize()
   {
      return mPrefferedSize;
   }

   public Dimension preferredSize()
   {
      return mPrefferedSize;
   }
}