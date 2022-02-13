package chess;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

/*
 * ºÚÆå
 */
public class PointBlack extends Canvas {
	Pad padBelonged;
	public PointBlack(Pad padBelonged) {
		setSize(20,20);
		this.padBelonged=padBelonged;
	}
//	»­ºÚÆå
	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.fillOval(0, 0, 14, 14);
	}
}
