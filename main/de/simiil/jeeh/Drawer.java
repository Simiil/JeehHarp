package de.simiil.jeeh;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;

import org.jfugue.Note;


public class Drawer {

	private enum value {
		WHOLE, HALF, QUARTER, EIGHTH;
	}

	public static final int DIN_A_4_H = 297;
	public static final int DIN_A_4_W = 210;
	public static final int FACTOR = 10;

	public static final int ROWS_PER_SITE = 100;
	public static final int COLS_PER_SITE = 100;
//	public static final int STARTING_NOTE = 43;
	public static final int STARTING_NOTE = 55;
	public static double NOTE_H = 2 * FACTOR;
	public static final double NOTE_W = (11.5 * FACTOR);

	private int beatlength;

	private int currentRow = 0;
	private Graphics2D g;
	private BufferedImage i;
	private File targetDir;
	private Song song;

	public static void main(String[] args) throws IOException,
			InvalidMidiDataException {
		if(args.length!=2){
			System.out.println("Usage: ");
			System.out.println("cmd FILE.mid outputdir");
			return;
		}
		new Drawer(args[0], args[1]);
	}

	public Drawer(String song, String dir) throws IOException, InvalidMidiDataException {
		MidiSeqParser sp = new MidiSeqParser();
		drawSong(sp.parse(song),dir);
	}

	public void drawSong(Song song, String picture) throws IOException {
		int sites = (int) Math.ceil(song.getNotes().size()/ROWS_PER_SITE);
		this.song = song;
		
		if(song.getInterpret() == null || song.getInterpret().equals("")){
			Scanner s = new Scanner(System.in);
			System.out.println("Interpret: ");
			song.setInterpret(s.nextLine());
		}
		if(song.getTitle() == null || song.getTitle().equals("")){
			Scanner s = new Scanner(System.in);
			System.out.println("Title: ");
			song.setTitle(s.nextLine());
		}
		
		this.targetDir = new File(picture);
		if(!targetDir.exists()){
			targetDir.mkdirs();
		}
		if (song.getNotes().size() < ROWS_PER_SITE) {
			NOTE_H = DIN_A_4_W * FACTOR / (song.getNotes().size());
		} else {
			NOTE_H = DIN_A_4_W * FACTOR / (ROWS_PER_SITE);
		}

		beatlength = song.getBpm() / 60;

		initBufferedImage();
		drawTitle();
		drawHelpLine();
		currentRow = 0;
		// for(int j = 0; j<COLS_PER_SITE;j++){
		// double notepos = j*NOTE_W ;
		//
		// g.draw(new Ellipse2D.Double(notepos, currentRow+(currentRow*NOTE_H),
		// NOTE_W, NOTE_H));
		// }

		currentRow = 1;
		Entry<Timestamp, LinkedList<Note>> lastEntry = null;
		int index = 0;
		int pageIndex = 1;
		for (Entry<Timestamp, LinkedList<Note>> e : song.getNotes().entrySet()) {
			index++;
			if(index>=ROWS_PER_SITE-6){
				finishPage(g, pageIndex, sites);
				g.dispose();

				ImageIO.write(i, "png", new File(picture+"Page-"+(pageIndex++)+".png"));
				initBufferedImage();
				drawHelpLine();
				currentRow = 1;
				index = 0;
			}
			double lastPos = -1;
			for (Note n : e.getValue()) {
				double dur = n.getDecimalDuration();
				dur = (Math.ceil(dur * 10)) / 10;
				value v = calculateDur(dur);
				double notepos = (n.getValue() - STARTING_NOTE) * NOTE_W;
				drawNote(v, g, notepos, currentRow + (currentRow * NOTE_H));
				if (lastPos > 0 && notepos > 0) {
					drawDottedLine(g, lastPos, notepos, currentRow
							+ (currentRow * NOTE_H));
				}
				if (notepos > 0) {
					lastPos = notepos;
				}
				if (lastEntry != null) {
					handleAndDrawLine(g, n, currentRow, lastEntry);
				}
			}
			lastEntry = e;
			currentRow++;
		}
		
	}

	private void drawHelpLine() {
		g.draw(new Line2D.Double(NOTE_W/2, 1*FACTOR, NOTE_W/2, 1*FACTOR + 20*FACTOR));
	}

	private void drawTitle() {
		Font oldFont = g.getFont();
		Color oldColor = g.getColor();
		g.setFont(new Font("Arial", Font.PLAIN, 5*FACTOR));
		int titleheight =g.getFontMetrics().getHeight();
		
		g.drawChars(song.getTitle().toCharArray(), 0, song.getTitle().length(),(int) (NOTE_W+1*FACTOR), 10 + titleheight);
		
		g.setFont(new Font("Arial", Font.PLAIN, 3*FACTOR));
		g.setColor(new Color(0x999999));
		
		String subtitle = "By "+song.getInterpret();
		g.drawChars(subtitle.toCharArray(), 0, subtitle.length(),(int) (NOTE_W+1*FACTOR), 15 + g.getFontMetrics().getHeight()+titleheight);
		g.setFont(oldFont);
		g.setColor(oldColor);
		
	}

	private void finishPage(Graphics2D g2, int pageIndex, int sites) {
		String foo = pageIndex+"/"+sites;
		Font oldFont = g2.getFont();
		g2.setFont(new Font("Arial", Font.PLAIN, 5*FACTOR));
		
		g2.drawChars(foo.toCharArray(), 0, foo.length(),10, DIN_A_4_W*FACTOR-g2.getFontMetrics().getHeight()-10);
		g2.setFont(oldFont);
		
	}

	private void initBufferedImage() {
		i = new BufferedImage(DIN_A_4_H * FACTOR, DIN_A_4_W
				* FACTOR, BufferedImage.TYPE_INT_RGB);
		g = i.createGraphics();
		g.setColor(Color.WHITE);
		g.fill(new Rectangle(0, 0, DIN_A_4_H * FACTOR, DIN_A_4_W * FACTOR));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
	}

	private void handleAndDrawLine(Graphics2D g, Note n, int currentRow,
			Entry<Timestamp, LinkedList<Note>> lastEntry) {
		int offset = Integer.MAX_VALUE;
		Note nextNote = null;
		for(Note n2 : lastEntry.getValue()){
			if(Math.abs(n.getValue() - n2.getValue())<offset){
				offset = Math.abs(n.getValue() - n2.getValue());
				nextNote = n2;
			}
		}

		if(Math.abs(nextNote.getValue() - n.getValue()) > 10){
			return;
		}
		
		double notepos1 = (n.getValue()- STARTING_NOTE)*NOTE_W ;
		double notepos2 = (nextNote.getValue()- STARTING_NOTE)*NOTE_W ;
		
		double row1 = currentRow+(currentRow*NOTE_H);
		double row2 = (currentRow-1)+((currentRow-1)*NOTE_H);
		
		g.draw(new Line2D.Double(notepos1+ NOTE_W / 2, row1+ NOTE_H / 2, notepos2+ NOTE_W / 2, row2+ NOTE_H / 2));
	}

	private void drawDottedLine(Graphics2D g, double lastPos, double notepos,
			double row) {
		Stroke stroke = new BasicStroke(3, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
		Stroke oldstroke = g.getStroke();
		Color oldcolor = g.getColor();
		g.setStroke(stroke);
		g.setColor(new Color(0x999999));
		g.draw(new Line2D.Double(lastPos + NOTE_W / 2, row + NOTE_H / 2,
				notepos + NOTE_W / 2, row + NOTE_H / 2));
		g.setStroke(oldstroke);
		g.setColor(oldcolor);

	}

	private void drawNote(value v, Graphics2D g, double notepos, double d) {
		switch (v) {
		case WHOLE:
			drawWhole(g, notepos, currentRow + (currentRow * NOTE_H));
			break;
		case HALF:
			drawHalf(g, notepos, currentRow + (currentRow * NOTE_H));
			break;
		case QUARTER:
			drawQuarter(g, notepos, currentRow + (currentRow * NOTE_H));
			break;
		case EIGHTH:
			drawEighth(g, notepos, currentRow + (currentRow * NOTE_H));
			break;
		default:
			break;
		}

	}

	private value calculateDur(double dur) {
		double offset = 0.11;
		if (dur > 1) {
			return value.WHOLE;
		}
		if (dur > (1 - offset) && dur < (1 + offset)) {
			return value.WHOLE;
		}
		if (dur > (0.5 - offset) && dur < (0.5 + offset)) {
			return value.HALF;
		}
		if (dur > (0.25 - offset) && dur < (0.25 + offset)) {
			return value.QUARTER;
		}
		if (dur > (0.125 - offset) && dur < (0.125 + offset)) {
			return value.EIGHTH;
		}
		return value.WHOLE;
	}

	private void drawWhole(Graphics2D g, double x, double y) {
		double margin = NOTE_W / 8;
		double border = 10;
		double outer_h = NOTE_H;
		double outer_w = NOTE_W - (margin * 2);
		double inner_h = NOTE_H - border;
		double inner_w = NOTE_W - (margin * 2) - border;

		Color old = g.getColor();
		g.fill(new Ellipse2D.Double(x + margin, y, outer_w, outer_h));
		g.setColor(Color.WHITE);
		g.fill(new Ellipse2D.Double(x + margin + (border / 2),
				y + (border / 2), inner_w, inner_h));
		g.setColor(old);
		// g.draw(new Ellipse2D.Double(x+(NOTE_W/3), y, NOTE_W-(NOTE_W/3),
		// NOTE_H));
	}

	private void drawHalf(Graphics2D g, double x, double y) {
		double margin = NOTE_W / 4;
		double border = 5;
		double outer_h = NOTE_H;
		double outer_w = NOTE_W - (margin * 2);
		double inner_h = NOTE_H - border;
		double inner_w = NOTE_W - (margin * 2) - border;

		Color old = g.getColor();
		g.fill(new Ellipse2D.Double(x + margin, y, outer_w, outer_h));
		g.setColor(Color.WHITE);
		g.fill(new Ellipse2D.Double(x + margin + (border / 2),
				y + (border / 2), inner_w, inner_h));
		g.setColor(old);
		// g.draw(new Ellipse2D.Double(x+(NOTE_W/3), y, NOTE_W-(NOTE_W/3),
		// NOTE_H));
	}

	private void drawQuarter(Graphics2D g, double x, double y) {
		double margin = NOTE_W / 4;
		double outer_h = NOTE_H;
		double outer_w = NOTE_W - (margin * 2);

		g.fill(new Ellipse2D.Double(x + margin, y, outer_w, outer_h));
		// g.draw(new Ellipse2D.Double(x+(NOTE_W/3), y, NOTE_W-(NOTE_W/3),
		// NOTE_H));
	}

	private void drawEighth(Graphics2D g, double x, double y) {
		double margin = NOTE_W / 3;
		double outer_h = NOTE_H;
		double outer_w = NOTE_W - (margin * 2);

		g.fill(new Ellipse2D.Double(x + margin, y, outer_w, outer_h));
		// g.draw(new Ellipse2D.Double(x+(NOTE_W/3), y, NOTE_W-(NOTE_W/3),
		// NOTE_H));

	}
}
