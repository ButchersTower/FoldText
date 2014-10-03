package FoldText;

import javax.swing.JFrame;

public class FoldText extends JFrame {
	public FoldText() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Panel());
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setTitle("FoldText");
	}

	public static void main(String[] args) {
		new FoldText();
	}
}
