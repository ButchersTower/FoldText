package FoldText;

public class JaMa {

	static int[] injectIntAr(int[] ar, int app, int loc) {
		int[] buff = new int[ar.length + 1];
		boolean added = false;
		for (int a = 0; a < buff.length; a++) {
			if (a == loc) {
				buff[a] = app;
				added = true;
			} else {
				if (added) {
					buff[a] = ar[a - 1];
				} else {
					buff[a] = ar[a];
				}
			}
		}
		if (!added) {
			buff[loc] = app;
		}
		return buff;
	}

	static int[][] injectIntArAr(int[][] ar, int[] app, int loc) {
		int[][] buff = new int[ar.length + 1][];
		boolean added = false;
		for (int a = 0; a < buff.length; a++) {
			if (a == loc) {
				buff[a] = app;
				added = true;
			} else {
				if (added) {
					buff[a] = ar[a - 1];
				} else {
					buff[a] = ar[a];
				}
			}
		}
		if (!added) {
			buff[loc] = app;
		}
		return buff;
	}

	static String[] injectStringAr(String[] ar, String app, int loc) {
		String[] buff = new String[ar.length + 1];
		boolean added = false;
		for (int a = 0; a < buff.length; a++) {
			if (a == loc) {
				buff[a] = app;
				added = true;
			} else {
				if (added) {
					buff[a] = ar[a - 1];
				} else {
					buff[a] = ar[a];
				}
			}
		}
		if (!added) {
			buff[loc] = app;
		}
		return buff;
	}

	static int[][] removeIntArAr(int[][] ar) {
		int[][] buff = new int[ar.length - 1][];
		return buff;
	}

	static int[] appendIntAr(int[] st, int appendage) {
		int[] temp = new int[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static int[][] appendIntArAr(int[][] st, int[] appendage) {
		int[][] temp = new int[st.length + 1][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static int[][][] appendIntArArAr(int[][][] st, int[][] appendage) {
		int[][][] temp = new int[st.length + 1][][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static String[] appendStringAr(String[] st, String appendage) {
		String[] temp = new String[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}
}
