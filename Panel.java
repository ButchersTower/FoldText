package FoldText;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Panel extends JPanel implements MouseListener, KeyListener,
		MouseWheelListener {
	int width = 640;
	int height = 580;

	Image[] txtAr;

	Thread thread;
	Image image;
	Graphics g;

	int scrollY;
	boolean canScrollDown = false;
	int spaceBetweenRows = 2;

	// spaceBetweenBoxes is actualy 2 less because the cyan box extends down
	// by an addition 2.
	int spaceBetweenBoxes = 6;

	int[][] panes = { { 0, 0, width, 24 }, { 0, 24, width - 24, height - 24 },
			{ width - 24, 24, 24, height - 24 } };

	// 0 is user mode
	// 1 is edit mode.
	int mode = 0;

	public Panel() {
		super();

		setPreferredSize(new Dimension(width, height));
		setFocusable(true);
		requestFocus();

		addKeyListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		this.setSize(new Dimension(width, height));

		pStart();
	}

	/**
	 * Methods go below here.
	 */

	String[] on = { "Didnt load from .txt" };

	// [x][0] = indent;
	// [x][1] = (0 minimize after) (1 show after)
	// [0][2] = pane;
	int[][] occ = { { 0, 1, 1 } };

	int topSpaceY = 10;

	public void pStart() {
		imageInit();

		try {
			TextInit.readNewFold();
			// get the two strings and break them down.
			ArrayList<String> temp = TextInit.getStrings();
			// temp 1 is string[] temp 2 is int[][]
			// deals with the Strings
			String tempS = temp.get(0);
			on = tempS.split("<!>");
			// deals with intArs
			String tempI = temp.get(1);
			String[] tempIa;
			tempIa = tempI.split("!");
			String[][] tempIb = new String[tempIa.length][];
			occ = new int[tempIa.length][];
			// System.out.println("tempIa.length: " + tempIa.length);
			for (int t = 0; t < tempIa.length; t++) {
				tempIb[t] = tempIa[t].split(",");
				occ[t] = new int[tempIb[t].length];
				// System.out.println("tempIb[t].length: " + tempIb[t].length);
				for (int tb = 0; tb < tempIb[t].length; tb++) {
					// System.out.println("[t]: " + t + "   [tb]: " + tb);
					occ[t][tb] = Integer.parseInt(tempIb[t][tb]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		multTextBox(on, 0, 1);
		drawAll();
	}

	void drawAll() {

		int font = 0;
		if (mode == 0) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.PINK);
		}
		g.fillRect(0, 0, width, height);
		int boxes = on.length;
		letters = new int[boxes][][];
		int row = 0;
		// get the string and line info of each box.
		// then use
		drawsIt(font, 1);

		// for (int b = 0; b < boxes; b++) {
		// int boxWidth = panes[occ[b][2]][2] - 20
		// - (occ[b][0] * fonts[font][0]);
		// int panesX = panes[occ[b][2]][0];
		// int panesY = panes[occ[b][2]][1];
		// // draw button infront.
		// newTextBox(on[b], panesX + 16 + (occ[b][0] * 12), panesY + 10
		// + (row * 20), boxWidth, font);
		// row++;
		// }

	}

	void compileText() {
		// compile string[] and int[][]
		StringBuilder compS = new StringBuilder();
		for (int n = 0; n < on.length; n++) {
			compS.append(on[n] + "<!>");
		}
		StringBuilder compI = new StringBuilder();
		for (int o = 0; o < occ.length; o++) {
			for (int c = 0; c < occ[o].length; c++) {
				compI.append(occ[o][c] + ",");
			}
			compI.append("!");
		}
		System.out.println("compS: " + compS.toString());
		System.out.println("compI: " + compI.toString());
		TextInit.saveNew(compS.toString(), compI.toString());
	}

	void drawTopPane() {

	}

	void drawScrollPane() {

	}

	int[][] fonts = { { 12, 16 } };

	// each box, each line, then x's
	int[][][] letters;

	int[][] pinkLocs;
	// the info for the draw boxes.
	int[][] txtBoxInfo;
	// [0] = x;
	// [1] = y;
	// [2] = width;
	// [3] = height;
	// [4] the String[]'s number for editing later on.

	int[][][] allLns;
	String[] allSts;

	int[][] pane0butts = { { 6, 3, 50, 18 }, { 60, 3, 50, 18 } };

	// [0] = box
	// [1] = row
	// [2] = lett
	int[] cursor = { -1 };

	boolean shiftP = false;

	// Scroll variables
	double lastDraw = System.currentTimeMillis();
	double lastScroll = System.currentTimeMillis();
	int notchesSinceDraw = 0;
	int tickSinceRest = 0;

	// gets the string[] and layers[][] of each text box.
	void multTextBox(String[] st, int font, int pane) {
		// draws them all in pane[1]

		int[] paneLoc = { panes[pane][0], panes[pane][1] };

		// String[] allSts = new String[st.length];
		// int[][] allLns = new int[st.length][];
		allSts = new String[0];
		/**
		 * THIS SHIT JUST SPLITS UP THE STRING BY ROW.
		 */
		// work with allLns
		// EITHER add another dimention,
		// OR double the lenth and have evens be wether its a break and odds be
		// the length of the line.
		// ill do the first one first.
		allLns = new int[0][][];
		boolean skipIt = false;
		int closeLay = 0;
		for (int s = 0; s < st.length; s++) {
			// System.out.println("st[" + s + "]: " + st[s]);
			// if s is zero draw that txt but nothing below.
			if (skipIt) {
				// System.out.println("occ.l: " + occ.length + "    s: " + s);
				// System.out.println("occ[s].l: " + occ[s].length);
				if (occ[s][0] <= closeLay) {
					skipIt = false;
				}
			}
			if (!skipIt) {
				String singSt = st[s];
				// returns int[rows][letters]
				StringBuilder stCollect = new StringBuilder();
				int[][] lineS = new int[0][];
				// construct a long string of all the letters drawn to this
				// txtBox
				// and draw it later in the method.
				String[] lines = parseString(singSt);
				int[][] letLoc = new int[lines.length][];
				// between <br>'s
				for (int l = 0; l < lines.length; l++) {
					// System.out.println("lines[" + l + "]: " + lines[l]);
					int boxW = panes[pane][2]
							- (16 + paneLoc[0] + (occ[s][0] * 12)) - 10;
					int totLettsLine = (boxW - (boxW % fonts[font][0]))
							/ fonts[font][0];
					// parse by spaces into words.
					// System.out.println("lines[" + l + "]: " + lines[l]);
					String[] words = lines[l].split("[ ]");
					// take a letter (period) off the beginning first and end of
					// last word.
					if (words.length == 1) {
						StringBuilder stB = new StringBuilder();
						stB.append(words[0]);
						stB.deleteCharAt(0);
						stB.deleteCharAt(stB.length() - 1);
						words[0] = stB.toString();
					} else if (words.length > 1) {
						StringBuilder stB = new StringBuilder();
						stB.append(words[0]);
						stB.deleteCharAt(0);
						words[0] = stB.toString();
						stB.delete(0, stB.length());
						stB.append(words[words.length - 1]);
						stB.deleteCharAt(stB.length() - 1);
						words[words.length - 1] = stB.toString();
					}
					int lettersThisLine = 0;
					// figure out with of each word and if it can fit onto this
					// line.
					for (int w = 0; w < words.length; w++) {
						// System.out.println("(" + words[w].length()
						// + ")  words[" + w + "]: " + words[w]);
						int wordL = words[w].length();
						if (lettersThisLine + wordL <= totLettsLine) {
							stCollect.append(words[w] + " ");
							lettersThisLine += wordL + 1;
						} else {
							// move to next line
							lineS = JaMa.appendIntArAr(lineS, new int[] { 0,
									lettersThisLine });
							lettersThisLine = 0;
							stCollect.append(words[w] + " ");
							lettersThisLine += wordL + 1;
						}
					}
					lineS = JaMa.appendIntArAr(lineS, new int[] { 1,
							lettersThisLine });
					lettersThisLine = 0;
				}
				// allSts[s] = stCollect.toString();
				// allLns[s] = lineS;
				allSts = JaMa.appendStringAr(allSts, stCollect.toString());
				allLns = JaMa.appendIntArArAr(allLns, lineS);
				if (occ[s][1] == 0) {
					skipIt = true;
					closeLay = occ[s][0];
				}
			}
		}
		/**
		 * save BUTTON LOCS FOR LATER CHECKING.
		 */
		txtBoxInfo = new int[allSts.length][5];
		pinkLocs = new int[allSts.length][5];
		// [0] = x;
		// [1] = y;
		// [2] = width;
		// [4] = height;
		// [5] should be the String[]'s number for editing later on.
		skipIt = false;
		// setting closeLay to zero is useless since the only time it gets
		// called is when skipIt is set to true which also sets this.
		closeLay = 0;
		int skipTimes = 0;
		// for (int b = 0; b < allSts.length; b++) {
		int drawRow = paneLoc[1] + scrollY + topSpaceY;
		for (int b = 0; b < st.length; b++) {
			// if (occ[b][1] == 1) {
			if (skipIt) {
				if (occ[b][0] <= closeLay) {
					skipIt = false;
				}
			}
			if (!skipIt) {
				// System.out.println("B: " + b);
				txtBoxInfo[b - skipTimes][0] = 16 + paneLoc[0]
						+ (occ[b][0] * 12);
				txtBoxInfo[b - skipTimes][1] = drawRow;
				// width is dist from the x to 10 before the end of the pane.
				// draws box.
				txtBoxInfo[b - skipTimes][2] = panes[pane][2]
						- txtBoxInfo[b - skipTimes][0] - 10;
				txtBoxInfo[b - skipTimes][3] = 18 + ((allLns[b - skipTimes].length - 1) * 18);
				txtBoxInfo[b - skipTimes][4] = b;
				// If the next box is not below this one (more indent) then do
				// not draw a pinkButton.
				boolean drawPink = true;
				if (b + 1 < st.length) {
					// if the next layer is not greater than (less than or equal
					// to) this one then dont draw a pink.
					if (occ[b][0] >= occ[b + 1][0]) {
						drawPink = false;
					}
				} else {
					drawPink = false;
				}
				// EITHER draw pinkButts separately from the other rectangles
				// OR have it so when there is no pink everything is 0.
				if (drawPink) {
					pinkLocs[b - skipTimes][0] = txtBoxInfo[b - skipTimes][0] - 10;
					pinkLocs[b - skipTimes][1] = txtBoxInfo[b - skipTimes][1] + 2;
					pinkLocs[b - skipTimes][2] = 8;
					pinkLocs[b - skipTimes][3] = 12;
					pinkLocs[b - skipTimes][4] = b;
				}
				// System.out .println("Add: " + (allLns[b - skipTimes].length *
				// (fonts[font][1] + spaceBetweenBoxes)));
				drawRow += (allLns[b - skipTimes].length
						* (fonts[font][1] + spaceBetweenRows) + spaceBetweenBoxes);
				if (occ[b][1] == 0) {
					skipIt = true;
					closeLay = occ[b][0];
				}
			} else {
				skipTimes++;
			}
		}
	}

	void drawsIt(int font, int pane) {
		canScrollDown = false;
		/**
		 * THIS SHIT DRAAAAAWS IT
		 */
		int spaceBetweenBoxes = 6;
		int[] paneLoc = { panes[pane][0], panes[pane][1] };
		int drawRow = paneLoc[1] + scrollY;
		for (int b = 0; b < allSts.length; b++) {
			boolean onScreen = true;
			// if lowY is after panel or highY is before then dont draw
			if (txtBoxInfo[b][1] + txtBoxInfo[b][3] < panes[pane][1]) {
				// before
				// System.out.println("before");
				onScreen = false;
			} else if (txtBoxInfo[b][1] > panes[pane][1] + panes[pane][3]) {
				// System.out.println("After");
				canScrollDown = true;
				onScreen = false;
			} else if (txtBoxInfo[b][1] + txtBoxInfo[b][3] + 10 > panes[pane][1]
					+ panes[pane][3]) {
				canScrollDown = true;
			}

			int spaceBetweenRows = 2;
			// System.out.println("fill: " + b);
			if (onScreen) {
				g.setColor(Color.CYAN);
				g.fillRect(txtBoxInfo[b][0], txtBoxInfo[b][1],
						txtBoxInfo[b][2], txtBoxInfo[b][3]);
				g.setColor(Color.MAGENTA);
				g.fillRect(pinkLocs[b][0], pinkLocs[b][1], pinkLocs[b][2],
						pinkLocs[b][3]);
			}

			int letsDrawn = 0;
			int[] stNums = converter(allSts[b].toString());
			for (int r = 0; r < allLns[b].length; r++) {
				if (onScreen) {
					// draw the correct number of letters on each line.
					for (int le = 0; le < allLns[b][r][1]; le++) {
						int letX = txtBoxInfo[b][0] + (le * (fonts[font][0]));
						g.drawImage(
								txtAr[stNums[le + letsDrawn]],
								letX,
								drawRow
										+ (r * (fonts[font][1] + spaceBetweenRows))
										+ topSpaceY, null);
						// System.out.println("dr");
					}
					letsDrawn += allLns[b][r][1];
				}
			}
			drawRow += (allLns[b].length * (fonts[font][1] + spaceBetweenRows))
					+ spaceBetweenBoxes;
		}

		/**
		 * Draw cursor
		 */
		boolean allGood = true;
		for (int c = 0; c < cursor.length; c++) {
			// System.out.println("cursor[" + c + "]: " + cursor[c]);
			if (cursor[c] == -1) {
				allGood = false;
			}
		}
		// implement panes for drawing?
		if (allGood) {
			g.setColor(Color.BLACK);
			g.fillRect(
					+(cursor[1] * fonts[font][0]) + txtBoxInfo[cursor[0]][0],
					+2 + (cursor[2] * (fonts[font][1] + spaceBetweenRows))
							+ txtBoxInfo[cursor[0]][1], 2, 14);
		}

		// draw pane 0
		// two buttons, labeled save and add.
		g.setColor(Color.GRAY);
		g.fillRect(panes[0][0], panes[0][1], panes[0][2], panes[0][3]);
		g.setColor(Color.WHITE);
		for (int p = 0; p < pane0butts.length; p++) {
			g.fillRect(panes[0][0] + pane0butts[p][0], panes[0][1]
					+ pane0butts[p][1], pane0butts[p][2], pane0butts[p][3]);
		}
		int[] stNums = converter("Save");
		for (int r = 0; r < stNums.length; r++) {
			g.drawImage(txtAr[stNums[r]], 7 + (r * fonts[font][0]), 3, null);
			// System.out.println("da");
		}
		stNums = converter("Edit");
		for (int r = 0; r < stNums.length; r++) {
			g.drawImage(txtAr[stNums[r]], 62 + (r * fonts[font][0]), 3, null);
			// System.out.println("da");
		}

	}

	void pane0Butts(int[] mouseLoc) {
		int[] relMouseLoc = { mouseLoc[0] - panes[0][0],
				mouseLoc[1] - panes[0][1] };
		for (int b = 0; b < pane0butts.length; b++) {
			if (relMouseLoc[0] > pane0butts[b][0]
					&& relMouseLoc[0] < pane0butts[b][0] + pane0butts[b][2]
					&& relMouseLoc[1] > pane0butts[b][1]
					&& relMouseLoc[1] < pane0butts[b][1] + pane0butts[b][2]) {
				System.out.println("butt: " + b);
				if (b == 0) {
					compileText();
				} else if (b == 1) {
					if (mode == 0) {
						System.out.println("EDIT MODE");
						mode = 1;
					} else {
						System.out.println("exit Edit");
						mode = 0;
					}
				}
			}
		}
	}

	void pane1Butts(int[] clickLoc) {
		for (int m = 0; m < pinkLocs.length; m++) {
			if (clickLoc[0] > pinkLocs[m][0]
					&& clickLoc[0] < pinkLocs[m][0] + pinkLocs[m][2]
					&& clickLoc[1] > pinkLocs[m][1]
					&& clickLoc[1] < pinkLocs[m][1] + pinkLocs[m][3]) {
				System.out
						.println("PinkBox: " + m + "(" + pinkLocs[m][4] + ")");
				if (occ[pinkLocs[m][4]][1] == 1) {
					occ[pinkLocs[m][4]][1] = 0;
				} else {
					occ[pinkLocs[m][4]][1] = 1;
				}
				multTextBox(on, 0, 1);
			}
		}

		for (int m = 0; m < txtBoxInfo.length; m++) {
			if (clickLoc[0] > txtBoxInfo[m][0]
					&& clickLoc[0] < txtBoxInfo[m][0] + txtBoxInfo[m][2]
					&& clickLoc[1] > txtBoxInfo[m][1]
					&& clickLoc[1] < txtBoxInfo[m][1] + txtBoxInfo[m][3]) {
				// System.out.println("txtBoxInfo: " + m);
				int[] relClickLoc = { clickLoc[0] - txtBoxInfo[m][0],
						clickLoc[1] - txtBoxInfo[m][1] };
				clickInTextBox(relClickLoc, m);
			}
		}
	}

	void clickInTextBox(int[] relClickLoc, int boxInfo) {
		// bring in the number of the box font.
		System.out.println("boxInfo: " + boxInfo);
		System.out.println("relClickLoc (" + relClickLoc[0] + ", "
				+ relClickLoc[1] + ")");
		// occ[box] is has info for this box.
		// find row and letter
		int row = (relClickLoc[1] - (relClickLoc[1] % (fonts[0][1] + spaceBetweenRows)))
				/ (fonts[0][1] + spaceBetweenRows);
		// uses
		int column = (-2 + relClickLoc[0] + (fonts[0][0] / 2)) / fonts[0][0];
		// int column = (relClickLoc[0] - (relClickLoc[0] % (fonts[0][0])))
		// / (fonts[0][0]);
		System.out.println("row: " + row);
		System.out.println("column: " + column);
		// if the column is past the number of lines in the row then set cursor
		// to the end of the row.
		if (row >= allLns[boxInfo].length) {
			row = allLns[boxInfo].length - 1;
		}
		if (column >= allLns[boxInfo][row][1]) {
			column = allLns[boxInfo][row][1] - 1;
		}
		// System.out.println("txtBoxInfo[boxInfo][0]: " +
		// txtBoxInfo[boxInfo][0]);
		// System.out.println("txtBoxInfo[boxInfo][1]: " +
		// txtBoxInfo[boxInfo][1]);
		cursor = new int[] { boxInfo, column, row };
	}

	// gets the string[] and layers[][] of each text box.
	void multTextBoxOld1(String[] st, int font) {
		// draws them all in pane[1]
		int pane = 1;
		int[] paneLoc = { panes[pane][0], panes[pane][1] };
		// spaceBetweenBoxes is actualy 2 less because the cyan box extends down
		// by an addition 2.
		int spaceBetweenBoxes = 6;
		String[] allSts = new String[st.length];
		int[][] allLns = new int[st.length][];
		for (int s = 0; s < st.length; s++) {
			String singSt = st[s];
			int spaceBetweenRows = 2;
			// returns int[rows][letters]
			StringBuilder stCollect = new StringBuilder();
			int[] lineS = new int[0];
			// construct a long string of all the letters drawn to this txtBox
			// and draw it later in the method.
			int row = 0;
			String[] lines = parseString(singSt);
			int[][] letLoc = new int[lines.length][];
			// between <br>'s
			for (int l = 0; l < lines.length; l++) {
				// int totLettsLine = (width - (width % fonts[font][0]))
				// / fonts[font][0];
				int boxW = panes[pane][2]
						- (16 + paneLoc[0] + (occ[s][0] * 12)) - 10;
				int totLettsLine = (boxW - (boxW % fonts[font][0]))
						/ fonts[font][0];
				// int widthLeft = width;
				// parse by spaces into words.
				// System.out.println("lines[" + l + "]: " + lines[l]);
				String[] words = lines[l].split("[ ]");

				// take a letter (period) off the beginning first and end of
				// last word.
				if (words.length == 1) {
					StringBuilder stB = new StringBuilder();
					stB.append(words[0]);
					stB.deleteCharAt(0);
					stB.deleteCharAt(stB.length() - 1);
					words[0] = stB.toString();
					// System.out.println("words[0]): " + words[0]);
				} else if (words.length > 1) {
					StringBuilder stB = new StringBuilder();
					stB.append(words[0]);
					stB.deleteCharAt(0);
					words[0] = stB.toString();

					stB.delete(0, stB.length());

					stB.append(words[words.length - 1]);
					stB.deleteCharAt(stB.length() - 1);
					words[words.length - 1] = stB.toString();
				}

				int lettersThisLine = 0;
				// figure out with of each word and if it can fit onto this
				// line.
				for (int w = 0; w < words.length; w++) {
					// System.out.println("(" + words[w].length() + ")  words["
					// + w + "]: " + words[w]);
					int wordL = words[w].length();
					if (lettersThisLine + wordL <= totLettsLine) {
						stCollect.append(words[w] + " ");
						lettersThisLine += wordL + 1;
					} else {
						// move to next line
						row += 1;
						lineS = JaMa.appendIntAr(lineS, lettersThisLine);
						lettersThisLine = 0;
						stCollect.append(words[w] + " ");
						lettersThisLine += wordL + 1;
					}
				}
				row += 1;
				lineS = JaMa.appendIntAr(lineS, lettersThisLine);
				lettersThisLine = 0;
			}
			allSts[s] = stCollect.toString();
			allLns[s] = lineS;
			/**
			 * // draws box. g.setColor(Color.CYAN); g.fillRect(x, y, width,
			 * lineS.length (fonts[font][1] + spaceBetweenRows)); // draws
			 * letters. int letsDrawn = 0; int[] stNums =
			 * converter(stCollect.toString()); System.out.println("stNums.l: "
			 * + stNums.length); System.out.println("lineS.l:  " +
			 * lineS.length); for (int r = 0; r < lineS.length; r++) { // draw
			 * the correct number of letters on each line. int xIndent = 0;
			 * System.out.println("lineS[" + r + "]: " + lineS[r]); for (int le
			 * = 0; le < lineS[r]; le++) { g.drawImage(txtAr[stNums[le +
			 * letsDrawn]], x + xIndent, y + (r * (fonts[font][1] +
			 * spaceBetweenRows)), null); // System.out.println("dr"); xIndent
			 * += fonts[font][0]; } letsDrawn += lineS[r]; }
			 */
		}
		int drawRow = paneLoc[1] + scrollY;

		for (int b = 0; b < allSts.length; b++) {
			int boxX = 16 + paneLoc[0] + (occ[b][0] * 12);
			// width is dist from the x to 10 before the end of the pane.
			int spaceBetweenRows = 2;
			// draws box.
			g.setColor(Color.CYAN);
			g.fillRect(boxX, drawRow, panes[pane][2] - boxX - 10,
					18 + ((allLns[b].length - 1) * 18));
			// draws letters.
			int letsDrawn = 0;
			int[] stNums = converter(allSts[b].toString());
			// System.out.println("stNums.l: " + stNums.length);
			// System.out.println("lineS.l:  " + allLns[b].length);
			for (int r = 0; r < allLns[b].length; r++) {
				// draw the correct number of letters on each line.
				int xIndent = 16 + paneLoc[0] + (occ[b][0] * 12);
				// System.out.println("lineS[" + r + "]: " + allLns[b][r]);
				for (int le = 0; le < allLns[b][r]; le++) {
					int letX = xIndent;
					g.drawImage(txtAr[stNums[le + letsDrawn]], letX, drawRow
							+ (r * (fonts[font][1] + spaceBetweenRows)), null);
					// System.out.println("dr");
					xIndent += fonts[font][0];
				}
				letsDrawn += allLns[b][r];

			}
			drawRow += (fonts[font][1] + spaceBetweenBoxes);
		}
	}

	// draw a single text box
	void newTextBox(String st, int x, int y, int width, int font) {
		int spaceBetweenRows = 2;
		int[][] attempt;
		// returns int[rows][letters]
		StringBuilder stCollect = new StringBuilder();
		int[] lineS = new int[0];
		// construct a long string of all the letters drawn to this txtBox and
		// draw it later in the method.
		int row = 0;
		String[] lines = parseString(st);
		int[][] letLoc = new int[lines.length][];
		// between <br>'s
		for (int l = 0; l < lines.length; l++) {
			int totLettsLine = (width - (width % fonts[font][0]))
					/ fonts[font][0];
			// int widthLeft = width;
			// parse by spaces into words.
			System.out.println("lines[" + l + "]: " + lines[l]);
			String[] words = lines[l].split("[ ]");

			// take a letter (period) off the beginning first and end of last
			// word.
			if (words.length == 1) {
				StringBuilder stB = new StringBuilder();
				stB.append(words[0]);
				stB.deleteCharAt(0);
				stB.deleteCharAt(stB.length() - 1);
				words[0] = stB.toString();
				// System.out.println("words[0]): " + words[0]);
			} else if (words.length > 1) {
				StringBuilder stB = new StringBuilder();
				stB.append(words[0]);
				stB.deleteCharAt(0);
				words[0] = stB.toString();

				stB.delete(0, stB.length());

				stB.append(words[words.length - 1]);
				stB.deleteCharAt(stB.length() - 1);
				words[words.length - 1] = stB.toString();
			}

			int lettersThisLine = 0;

			// figure out with of each word and if it can fit onto this line.
			for (int w = 0; w < words.length; w++) {
				// System.out.println("(" + words[w].length() + ")  words[" + w
				// + "]: " + words[w]);
				int wordL = words[w].length();
				if (lettersThisLine + wordL <= totLettsLine) {
					// int[] nb = converter(words[w]);
					// between letters
					// if (nb.length == 0) {
					// // add a space
					// xIndent += fonts[font][0];
					// }
					// for (int le = 0; le < nb.length; le++) {
					// g.drawImage(txtAr[nb[le]], x + xIndent, y + (row * 18),
					// null);
					// System.out.println("dr");
					// xIndent += fonts[font][0];
					// }
					stCollect.append(words[w] + " ");
					lettersThisLine += wordL + 1;
				} else {
					// move to next line
					row += 1;
					lineS = JaMa.appendIntAr(lineS, lettersThisLine);
					lettersThisLine = 0;
					stCollect.append(words[w] + " ");
					lettersThisLine += wordL + 1;
				}
			}
			row += 1;
			lineS = JaMa.appendIntAr(lineS, lettersThisLine);
			lettersThisLine = 0;

			// int[] nb = converter(words[w]);
			// for (int le = 0; le < nb.length; le++) {
			// g.drawImage(txtAr[nb[le]], x + xIndent, y + (row * 18),
			// null);
			// System.out.println("dr");
			// xIndent += fonts[font][0];
			// }
		}
		// draws box.
		g.setColor(Color.CYAN);
		g.fillRect(x, y, width, row * (fonts[font][1] + spaceBetweenRows));
		// draws letters.
		int letsDrawn = 0;
		int[] stNums = converter(stCollect.toString());
		System.out.println("stNums.l: " + stNums.length);
		System.out.println("lineS.l:  " + lineS.length);
		for (int r = 0; r < lineS.length; r++) {
			// draw the correct number of letters on each line.
			int xIndent = 0;
			System.out.println("lineS[" + r + "]: " + lineS[r]);
			for (int le = 0; le < lineS[r]; le++) {
				g.drawImage(txtAr[stNums[le + letsDrawn]], x + xIndent, y
						+ (r * (fonts[font][1] + spaceBetweenRows)), null);

				// System.out.println("dr");
				xIndent += fonts[font][0];
			}
			letsDrawn += lineS[r];
		}
	}

	// leaves a period before and after each string
	String[] parseString(String st) {
		// split by "<br>"
		StringBuilder stringB = new StringBuilder();
		stringB.append(st);
		// add "a"'s so the program does not forget about arrows or breaks that
		// come before all other text.
		stringB.insert(0, ".");
		stringB.append(".");
		String[] strngs = stringB.toString().split("<br>");
		stringB.delete(0, stringB.length());
		for (int s = 0; s < strngs.length; s++) {
			// System.out.println("strngs[" + s + "]: " + strngs[s]);
			// decompile greater than.
			String[] a1 = strngs[s].split("<gt>");
			// recomple with correct sign.

			stringB.append(strngs[s]);
			stringB.insert(0, ".");
			stringB.append(".");
			for (int a = 0; a < a1.length; a++) {
				// take out and reinsure greater than and greater than.
				// if there are
				String[] a2 = stringB.toString().split("<lt>");
				stringB.delete(0, stringB.length());
				for (int b = 0; b < a2.length; b++) {
					if (b != 0) {
						stringB.append("<");
					}
					stringB.append(a2[b]);
				}
				// System.out.println("stB.ts1: " + stringB.toString());
				String[] a3 = stringB.toString().split("<gt>");
				stringB.delete(0, stringB.length());
				// System.out.println("stb.l: " + stringB.length());
				for (int b = 0; b < a3.length; b++) {
					if (b != 0) {
						stringB.append(">");
					}
					stringB.append(a3[b]);
				}
			}
			// gets rid of the string before and after each string
			// stringB.deleteCharAt(0);
			// stringB.deleteCharAt(stringB.length() - 1);
			strngs[s] = stringB.toString();
			stringB.delete(0, stringB.length());
			// System.out.println("strngsf[" + s + "]: " + strngs[s]);
		}
		// gets rid of the period before and after the first and last strings.
		if (strngs.length == 1) {
			StringBuilder stB = new StringBuilder();
			stB.append(strngs[0]);
			stB.deleteCharAt(0);
			stB.deleteCharAt(stB.length() - 1);
			strngs[0] = stB.toString();
		} else if (strngs.length > 1) {
			StringBuilder stB = new StringBuilder();
			stB.append(strngs[0]);
			stB.deleteCharAt(0);
			strngs[0] = stB.toString();

			stB.delete(0, stB.length());

			stB.append(strngs[strngs.length - 1]);
			stB.deleteCharAt(stB.length() - 1);
			strngs[strngs.length - 1] = stB.toString();
		}
		return strngs;
	}

	void addTxtBox(int loc) {
		int[] standard = { occ[loc][0], 0, 0 };
		occ = JaMa.injectIntArAr(occ, standard, loc);
		System.out.println("occ[" + loc + "]: " + occ[loc].length);
		on = JaMa.injectStringAr(on, ".", loc);
	}

	int addLett(int lett) {
		for (int r = 0; r < cursor[2]; r++) {
			lett += allLns[cursor[0]][r][1];
			if (allLns[cursor[0]][r][0] == 1) {
				lett += 3;
			}
		}
		return lett;
	}

	void addLet(String let) {
		int lett = cursor[1];
		int oNum = txtBoxInfo[cursor[0]][4];
		for (int r = 0; r < cursor[2]; r++) {
			lett += allLns[cursor[0]][r][1];
			if (allLns[cursor[0]][r][0] == 1) {
				lett += 3;
			}
		}
		StringBuilder buff = new StringBuilder(on[oNum]);
		buff.insert(lett, let);
		on[oNum] = buff.toString();
		cursor[1]++;
	}

	/**
	 * Methods go above here.
	 */

	public static int[] converter(String st) {
		int a = st.length();
		int[] nw = new int[a];

		for (int b = 0; b < a; b++) {
			if (st.charAt(b) == 'a') {
				nw[b] = 26;
			} else if (st.charAt(b) == 'A') {
				nw[b] = 0;
			} else if (st.charAt(b) == 'b') {
				nw[b] = 27;
			} else if (st.charAt(b) == 'B') {
				nw[b] = 1;
			} else if (st.charAt(b) == 'c') {
				nw[b] = 28;
			} else if (st.charAt(b) == 'C') {
				nw[b] = 2;
			} else if (st.charAt(b) == 'd') {
				nw[b] = 29;
			} else if (st.charAt(b) == 'D') {
				nw[b] = 3;
			} else if (st.charAt(b) == 'e') {
				nw[b] = 30;
			} else if (st.charAt(b) == 'E') {
				nw[b] = 4;
			} else if (st.charAt(b) == 'f') {
				nw[b] = 31;
			} else if (st.charAt(b) == 'F') {
				nw[b] = 5;
			} else if (st.charAt(b) == 'g') {
				nw[b] = 32;
			} else if (st.charAt(b) == 'G') {
				nw[b] = 6;
			} else if (st.charAt(b) == 'h') {
				nw[b] = 33;
			} else if (st.charAt(b) == 'H') {
				nw[b] = 7;
			} else if (st.charAt(b) == 'i') {
				nw[b] = 34;
			} else if (st.charAt(b) == 'I') {
				nw[b] = 8;
			} else if (st.charAt(b) == 'j') {
				nw[b] = 35;
			} else if (st.charAt(b) == 'J') {
				nw[b] = 9;
			} else if (st.charAt(b) == 'k') {
				nw[b] = 36;
			} else if (st.charAt(b) == 'K') {
				nw[b] = 10;
			} else if (st.charAt(b) == 'l') {
				nw[b] = 37;
			} else if (st.charAt(b) == 'L') {
				nw[b] = 11;
			} else if (st.charAt(b) == 'm') {
				nw[b] = 38;
			} else if (st.charAt(b) == 'M') {
				nw[b] = 12;
			} else if (st.charAt(b) == 'n') {
				nw[b] = 39;
			} else if (st.charAt(b) == 'N') {
				nw[b] = 13;
			} else if (st.charAt(b) == 'o') {
				nw[b] = 40;
			} else if (st.charAt(b) == 'O') {
				nw[b] = 14;
			} else if (st.charAt(b) == 'p') {
				nw[b] = 41;
			} else if (st.charAt(b) == 'P') {
				nw[b] = 15;
			} else if (st.charAt(b) == 'q') {
				nw[b] = 42;
			} else if (st.charAt(b) == 'Q') {
				nw[b] = 16;
			} else if (st.charAt(b) == 'r') {
				nw[b] = 43;
			} else if (st.charAt(b) == 'R') {
				nw[b] = 17;
			} else if (st.charAt(b) == 's') {
				nw[b] = 44;
			} else if (st.charAt(b) == 'S') {
				nw[b] = 18;
			} else if (st.charAt(b) == 't') {
				nw[b] = 45;
			} else if (st.charAt(b) == 'T') {
				nw[b] = 19;
			} else if (st.charAt(b) == 'u') {
				nw[b] = 46;
			} else if (st.charAt(b) == 'U') {
				nw[b] = 20;
			} else if (st.charAt(b) == 'v') {
				nw[b] = 47;
			} else if (st.charAt(b) == 'V') {
				nw[b] = 21;
			} else if (st.charAt(b) == 'w') {
				nw[b] = 48;
			} else if (st.charAt(b) == 'W') {
				nw[b] = 22;
			} else if (st.charAt(b) == 'x') {
				nw[b] = 49;
			} else if (st.charAt(b) == 'X') {
				nw[b] = 23;
			} else if (st.charAt(b) == 'y') {
				nw[b] = 50;
			} else if (st.charAt(b) == 'Y') {
				nw[b] = 24;
			} else if (st.charAt(b) == 'z') {
				nw[b] = 51;
			} else if (st.charAt(b) == 'Z') {
				nw[b] = 25;
			} else if (st.charAt(b) == ' ') {
				nw[b] = 52;
			} else if (st.charAt(b) == '0') {
				nw[b] = 53;
			} else if (st.charAt(b) == '1') {
				nw[b] = 54;
			} else if (st.charAt(b) == '2') {
				nw[b] = 55;
			} else if (st.charAt(b) == '3') {
				nw[b] = 56;
			} else if (st.charAt(b) == '4') {
				nw[b] = 57;
			} else if (st.charAt(b) == '5') {
				nw[b] = 58;
			} else if (st.charAt(b) == '6') {
				nw[b] = 59;
			} else if (st.charAt(b) == '7') {
				nw[b] = 60;
			} else if (st.charAt(b) == '8') {
				nw[b] = 61;
			} else if (st.charAt(b) == '9') {
				nw[b] = 62;
			} else if (st.charAt(b) == '/') {
				nw[b] = 63;
			} else if (st.charAt(b) == '?') {
				nw[b] = 64;
			} else if (st.charAt(b) == '¿') {
				nw[b] = 65;
			} else if (st.charAt(b) == '(') {
				nw[b] = 66;
			} else if (st.charAt(b) == ')') {
				nw[b] = 67;
			} else if (st.charAt(b) == 'é') {
				nw[b] = 4;
			} else if (st.charAt(b) == 'á') {
				nw[b] = 0;
			} else if (st.charAt(b) == 'ó') {
				nw[b] = 14;
			} else if (st.charAt(b) == 'í') {
				nw[b] = 8;
			} else if (st.charAt(b) == '.') {
				nw[b] = 68;
			} else if (st.charAt(b) == ',') {
				nw[b] = 69;
			}

		}
		return nw;
	}

	public void drwGm() {
		Graphics g2 = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}

	public void imageInit() {
		txtAr = new Image[70];
		ImageIcon ii = new ImageIcon(this.getClass().getResource(
				"res/font/tx/cA.png"));
		txtAr[0] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cB.png"));
		txtAr[1] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cC.png"));
		txtAr[2] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cD.png"));
		txtAr[3] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cE.png"));
		txtAr[4] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cF.png"));
		txtAr[5] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cG.png"));
		txtAr[6] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cH.png"));
		txtAr[7] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cI.png"));
		txtAr[8] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cJ.png"));
		txtAr[9] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cK.png"));
		txtAr[10] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cL.png"));
		txtAr[11] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cM.png"));
		txtAr[12] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cN.png"));
		txtAr[13] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cO.png"));
		txtAr[14] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cP.png"));
		txtAr[15] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cQ.png"));
		txtAr[16] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cR.png"));
		txtAr[17] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cS.png"));
		txtAr[18] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cT.png"));
		txtAr[19] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cU.png"));
		txtAr[20] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cV.png"));
		txtAr[21] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cW.png"));
		txtAr[22] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cX.png"));
		txtAr[23] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cY.png"));
		txtAr[24] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cZ.png"));
		txtAr[25] = ii.getImage();

		ii = new ImageIcon(this.getClass().getResource("res/font/tx/La.png"));
		txtAr[26] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lb.png"));
		txtAr[27] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lc.png"));
		txtAr[28] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Ld.png"));
		txtAr[29] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Le.png"));
		txtAr[30] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lf.png"));
		txtAr[31] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lg.png"));
		txtAr[32] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lh.png"));
		txtAr[33] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Li.png"));
		txtAr[34] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lj.png"));
		txtAr[35] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lk.png"));
		txtAr[36] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Ll.png"));
		txtAr[37] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lm.png"));
		txtAr[38] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Ln.png"));
		txtAr[39] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lo.png"));
		txtAr[40] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lp.png"));
		txtAr[41] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lq.png"));
		txtAr[42] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lr.png"));
		txtAr[43] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Ls.png"));
		txtAr[44] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lt.png"));
		txtAr[45] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lu.png"));
		txtAr[46] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lv.png"));
		txtAr[47] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lw.png"));
		txtAr[48] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lx.png"));
		txtAr[49] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Ly.png"));
		txtAr[50] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/Lz.png"));
		txtAr[51] = ii.getImage();

		ii = new ImageIcon(this.getClass()
				.getResource("res/font/tx/cSpace.png"));
		txtAr[52] = ii.getImage();

		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n0.png"));
		txtAr[53] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n1.png"));
		txtAr[54] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n2.png"));
		txtAr[55] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n3.png"));
		txtAr[56] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n4.png"));
		txtAr[57] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n5.png"));
		txtAr[58] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n6.png"));
		txtAr[59] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n7.png"));
		txtAr[60] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n8.png"));
		txtAr[61] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n9.png"));
		txtAr[62] = ii.getImage();
		ii = new ImageIcon(this.getClass()
				.getResource("res/font/tx/zslash.png"));
		txtAr[63] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/qMark.png"));
		txtAr[64] = ii.getImage();
		ii = new ImageIcon(this.getClass()
				.getResource("res/font/tx/qMarkI.png"));
		txtAr[65] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/z(.png"));
		txtAr[66] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/z).png"));
		txtAr[67] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource(
				"res/font/tx/zPeriod.png"));
		txtAr[68] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/zComa.png"));
		txtAr[69] = ii.getImage();
	}

	int editBox = -1;

	@Override
	public void mousePressed(MouseEvent me) {
		int[] mouseLoc = { me.getX(), me.getY() };
		if (mode == 0) {
			cursor = new int[] { -1 };
			// search through the panes.
			for (int p = 0; p < panes.length; p++) {
				if (mouseLoc[0] > panes[p][0] && mouseLoc[1] > panes[p][1]
						&& mouseLoc[0] < panes[p][0] + panes[p][2]
						&& mouseLoc[1] < panes[p][1] + panes[p][3]) {
					System.out.println("PANE: " + p);
					if (p == 1) {
						pane1Butts(mouseLoc);
					} else if (p == 0) {
						pane0Butts(mouseLoc);
					}
				}
			}
		} else if (mode == 1) {
			// find which space the click is closest to.
			// if its not on a butt find which butts its between.

			// know click Y, scroll Y.

			// find the box that has the closest (y - spaceBetweenBoxes/2) to
			// the clicks y
			for (int p = 0; p < panes.length; p++) {
				if (mouseLoc[0] > panes[p][0] && mouseLoc[1] > panes[p][1]
						&& mouseLoc[0] < panes[p][0] + panes[p][2]
						&& mouseLoc[1] < panes[p][1] + panes[p][3]) {
					System.out.println("PANE: " + p);
					if (p == 1) {
						if (shiftP) {
							// if right click then delete the text box clicked
							// on.
							if (me.getButton() == MouseEvent.BUTTON1) {
								int box = 0;
								float distance = Math.abs(me.getY()
										- txtBoxInfo[0][1]);
								for (int m = 1; m < txtBoxInfo.length; m++) {
									float newDist = Math.abs(me.getY()
											- txtBoxInfo[m][1]);
									if (newDist < distance) {
										box = m;
										distance = newDist;
										if (m + 1 == txtBoxInfo.length) {
											// if the last box is the closest
											// see if the dist to the end of the
											// box is closer than the dist to
											// the beginning of the box.
											newDist = Math
													.abs(me.getY()
															- (txtBoxInfo[m][1] + txtBoxInfo[m][3]));
										}
									} else {
										// past so kill loop?
									}
								}
								addTxtBox(txtBoxInfo[box][4]);
							}
						} else {
							// normal click in edit mode.
							// Selects a text box so that user can change
							// column.
							boolean foundBox = false;
							for (int m = 1; m < txtBoxInfo.length; m++) {
								if (mouseLoc[0] > txtBoxInfo[m][0]
										&& mouseLoc[0] < txtBoxInfo[m][0]
												+ txtBoxInfo[m][2]
										&& mouseLoc[1] > txtBoxInfo[m][1]
										&& mouseLoc[1] < txtBoxInfo[m][1]
												+ txtBoxInfo[m][3]) {
									editBox = txtBoxInfo[m][4];
									foundBox = true;
								}
							}
							if (foundBox = false) {
								editBox = -1;
							}
							// System.out.println("editBox: " + editBox);
						}
					} else if (p == 0) {
						pane0Butts(mouseLoc);
					}
				}
			}
		}
		multTextBox(on, 0, 1);
		drawAll();
		drwGm();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	@Override
	public void mouseClicked(MouseEvent me) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (mode == 0) {
			boolean allGood = true;
			for (int c = 0; c < cursor.length; c++) {
				if (cursor[c] == -1) {
					allGood = false;
				}
			}
			if (allGood) {
				if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftP = true;
				} else if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					if (cursor[1] != 0) {
						int lett = cursor[1];
						int oNum = txtBoxInfo[cursor[0]][4];
						lett = addLett(lett);
						lett--;
						cursor[1]--;
						// allSts[cursor[0]]
						StringBuilder buff = new StringBuilder(on[oNum]);
						buff.deleteCharAt(lett);
						on[oNum] = buff.toString();
						// allLns[cursor[0]][cursor[2]]--;
					} else {
						if (cursor[2] != 0) {
							int lett = cursor[1];
							int oNum = txtBoxInfo[cursor[0]][4];
							lett = addLett(lett);
							lett--;
							System.out.println("lett: " + lett);
							cursor[2]--;
							cursor[1] = allLns[cursor[0]][cursor[2]][1] - 1;
							System.out.println("on[oNum]: " + on[oNum]);
							StringBuilder buff = new StringBuilder(on[oNum]);
							// if this rows [0] is not a br then only delete 1
							// char.
							if (allLns[cursor[0]][cursor[2]][0] == 1) {
								buff.delete(lett - 3, lett + 1);
							} else {
								buff.delete(lett, lett + 1);
							}
							on[oNum] = buff.toString();
							System.out.println("on[oNum]: " + on[oNum]);
						}
					}
				} else if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
					// if(cursor[1] => )
				} else if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					// if cursor is no the end of the line then move the post
					// text
					// to
					// the new line.
					// find letters after cursor.
					int lett = cursor[1];
					int oNum = txtBoxInfo[cursor[0]][4];
					lett = addLett(lett);
					StringBuilder buff = new StringBuilder(on[oNum]);
					buff.insert(lett, "<br>");
					on[oNum] = buff.toString();
					cursor[1] = 0;
					cursor[2]++;
				} else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
					if (cursor[1] == 0) {
						if (cursor[2] != 0) {
							cursor[2]--;
							cursor[1] = allLns[cursor[0]][cursor[2]][1] - 1;
						}
					} else {
						cursor[1]--;
					}
				} else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
					if (cursor[1] == allLns[cursor[0]][cursor[2]][1] - 1) {
						if (cursor[2] != allLns[cursor[0]].length - 1) {
							cursor[1] = 0;
							cursor[2]++;
						}
					} else {
						cursor[1]++;
					}
				} else if (ke.getKeyCode() == KeyEvent.VK_UP) {
					if (cursor[2] == 0) {
						cursor[1] = 0;
					} else {
						cursor[2]--;
						if (cursor[1] > allLns[cursor[0]][cursor[2]][1] - 1) {
							cursor[1] = allLns[cursor[0]][cursor[2]][1] - 1;
						}
					}
				} else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
					if (cursor[2] == allLns[cursor[0]].length - 1) {
						cursor[1] = allLns[cursor[0]][cursor[2]][1] - 1;
					} else {
						cursor[2]++;
						if (cursor[1] > allLns[cursor[0]][cursor[2]][1] - 1) {
							cursor[1] = allLns[cursor[0]][cursor[2]][1] - 1;
						}
					}
				} else if (shiftP) {
					if (ke.getKeyCode() == KeyEvent.VK_A) {
						addLet("A");
					} else if (ke.getKeyCode() == KeyEvent.VK_B) {
						addLet("B");
					} else if (ke.getKeyCode() == KeyEvent.VK_C) {
						addLet("C");
					} else if (ke.getKeyCode() == KeyEvent.VK_D) {
						addLet("D");
					} else if (ke.getKeyCode() == KeyEvent.VK_E) {
						addLet("E");
					} else if (ke.getKeyCode() == KeyEvent.VK_F) {
						addLet("F");
					} else if (ke.getKeyCode() == KeyEvent.VK_G) {
						addLet("G");
					} else if (ke.getKeyCode() == KeyEvent.VK_H) {
						addLet("H");
					} else if (ke.getKeyCode() == KeyEvent.VK_I) {
						addLet("I");
					} else if (ke.getKeyCode() == KeyEvent.VK_J) {
						addLet("J");
					} else if (ke.getKeyCode() == KeyEvent.VK_K) {
						addLet("K");
					} else if (ke.getKeyCode() == KeyEvent.VK_L) {
						addLet("L");
					} else if (ke.getKeyCode() == KeyEvent.VK_M) {
						addLet("M");
					} else if (ke.getKeyCode() == KeyEvent.VK_N) {
						addLet("N");
					} else if (ke.getKeyCode() == KeyEvent.VK_O) {
						addLet("O");
					} else if (ke.getKeyCode() == KeyEvent.VK_P) {
						addLet("P");
					} else if (ke.getKeyCode() == KeyEvent.VK_Q) {
						addLet("Q");
					} else if (ke.getKeyCode() == KeyEvent.VK_R) {
						addLet("R");
					} else if (ke.getKeyCode() == KeyEvent.VK_S) {
						addLet("S");
					} else if (ke.getKeyCode() == KeyEvent.VK_T) {
						addLet("T");
					} else if (ke.getKeyCode() == KeyEvent.VK_U) {
						addLet("U");
					} else if (ke.getKeyCode() == KeyEvent.VK_V) {
						addLet("V");
					} else if (ke.getKeyCode() == KeyEvent.VK_W) {
						addLet("W");
					} else if (ke.getKeyCode() == KeyEvent.VK_X) {
						addLet("X");
					} else if (ke.getKeyCode() == KeyEvent.VK_Y) {
						addLet("Y");
					} else if (ke.getKeyCode() == KeyEvent.VK_Z) {
						addLet("Z");
					} else if (ke.getKeyCode() == KeyEvent.VK_9) {
						addLet("(");
					} else if (ke.getKeyCode() == KeyEvent.VK_0) {
						addLet(")");
					} else if (ke.getKeyCode() == KeyEvent.VK_SLASH) {
						addLet("?");
					}
				} else {
					if (ke.getKeyCode() == KeyEvent.VK_A) {
						addLet("a");
					} else if (ke.getKeyCode() == KeyEvent.VK_B) {
						addLet("b");
					} else if (ke.getKeyCode() == KeyEvent.VK_C) {
						addLet("c");
					} else if (ke.getKeyCode() == KeyEvent.VK_D) {
						addLet("d");
					} else if (ke.getKeyCode() == KeyEvent.VK_E) {
						addLet("e");
					} else if (ke.getKeyCode() == KeyEvent.VK_F) {
						addLet("f");
					} else if (ke.getKeyCode() == KeyEvent.VK_G) {
						addLet("g");
					} else if (ke.getKeyCode() == KeyEvent.VK_H) {
						addLet("h");
					} else if (ke.getKeyCode() == KeyEvent.VK_I) {
						addLet("i");
					} else if (ke.getKeyCode() == KeyEvent.VK_J) {
						addLet("j");
					} else if (ke.getKeyCode() == KeyEvent.VK_K) {
						addLet("k");
					} else if (ke.getKeyCode() == KeyEvent.VK_L) {
						addLet("l");
					} else if (ke.getKeyCode() == KeyEvent.VK_M) {
						addLet("m");
					} else if (ke.getKeyCode() == KeyEvent.VK_N) {
						addLet("n");
					} else if (ke.getKeyCode() == KeyEvent.VK_O) {
						addLet("o");
					} else if (ke.getKeyCode() == KeyEvent.VK_P) {
						addLet("p");
					} else if (ke.getKeyCode() == KeyEvent.VK_Q) {
						addLet("q");
					} else if (ke.getKeyCode() == KeyEvent.VK_R) {
						addLet("r");
					} else if (ke.getKeyCode() == KeyEvent.VK_S) {
						addLet("s");
					} else if (ke.getKeyCode() == KeyEvent.VK_T) {
						addLet("t");
					} else if (ke.getKeyCode() == KeyEvent.VK_U) {
						addLet("u");
					} else if (ke.getKeyCode() == KeyEvent.VK_V) {
						addLet("v");
					} else if (ke.getKeyCode() == KeyEvent.VK_W) {
						addLet("w");
					} else if (ke.getKeyCode() == KeyEvent.VK_X) {
						addLet("x");
					} else if (ke.getKeyCode() == KeyEvent.VK_Y) {
						addLet("y");
					} else if (ke.getKeyCode() == KeyEvent.VK_Z) {
						addLet("z");
					} else if (ke.getKeyCode() == KeyEvent.VK_0) {
						addLet("0");
					} else if (ke.getKeyCode() == KeyEvent.VK_1) {
						addLet("1");
					} else if (ke.getKeyCode() == KeyEvent.VK_2) {
						addLet("2");
					} else if (ke.getKeyCode() == KeyEvent.VK_3) {
						addLet("3");
					} else if (ke.getKeyCode() == KeyEvent.VK_4) {
						addLet("4");
					} else if (ke.getKeyCode() == KeyEvent.VK_5) {
						addLet("5");
					} else if (ke.getKeyCode() == KeyEvent.VK_6) {
						addLet("6");
					} else if (ke.getKeyCode() == KeyEvent.VK_7) {
						addLet("7");
					} else if (ke.getKeyCode() == KeyEvent.VK_8) {
						addLet("8");
					} else if (ke.getKeyCode() == KeyEvent.VK_9) {
						addLet("9");
					} else if (ke.getKeyCode() == KeyEvent.VK_SLASH) {
						addLet("/");
					} else if (ke.getKeyCode() == KeyEvent.VK_COMMA) {
						addLet(",");
					} else if (ke.getKeyCode() == KeyEvent.VK_PERIOD) {
						addLet(".");
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
					addLet(" ");
				}
				multTextBox(on, 0, 1);
				drawAll();
				drwGm();
			}
		} else if (mode == 1) {
			if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
				shiftP = true;
			} else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
				mode = 0;

				multTextBox(on, 0, 1);
				drawAll();
				drwGm();
			} else if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
				int[] test1 = { 0, 1, 2, 3, 4, 5, 6 };
				test1 = JaMa.injectIntAr(test1, 4, 1);
				for (int t = 0; t < test1.length; t++) {
					System.out.println("test1[" + t + "]: " + test1[t]);
				}
			} else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
				if (editBox != -1) {
					if (occ[editBox][0] > 0) {
						occ[editBox][0]--;
					}
				}
			} else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (editBox != -1) {
					occ[editBox][0]++;
				}
			}
			multTextBox(on, 0, 1);
			drawAll();
			drwGm();
		}
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftP = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent me) {
		// Scrolling gains speed in which if the dT is under 3 for 20 ticks it
		// ignores more draws having less lag making it "feel" like it goes
		// faster.
		int notches = me.getWheelRotation();
		if (System.currentTimeMillis() - lastDraw > 20) {
			multTextBox(on, 0, 1);
			drawAll();
			drwGm();
			lastDraw = System.currentTimeMillis();
		}
		if (notches < 0) {
			if (scrollY <= -3) {
				scrollY += 3;
				// drawAll();
				// drwGm();
			}
		} else {
			if (canScrollDown) {
				scrollY -= 3;
			}
			// drawAll();
			// drwGm();
		}
		lastScroll = System.currentTimeMillis();
	}
}
