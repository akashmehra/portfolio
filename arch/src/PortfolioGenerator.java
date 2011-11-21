import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PortfolioGenerator {

	public static List<Gamble> gambles = new ArrayList<Gamble>();

	public static List<Integer> favourates = new ArrayList<Integer>();

	public static List<Integer> unfavourates = new ArrayList<Integer>();

	public static int linkedMatrix[][] = null;

	public static void assignCategoryForGambles(int classNum) throws Exception {
		int seedF = (int)getARandomNumInRange(0, 10);
		int seedU = (int)getARandomNumInRange(0, 10);
		int seedN = (int)getARandomNumInRange(0, 10);
		int sum = seedF + seedU + seedN;
		int favourateNum = (int)getARandomNumInRange(0, (int)(classNum * seedF / sum));
		int unfavourateNum = (int)getARandomNumInRange(0, (int)(classNum * seedU / sum));
		List<Integer> classList = new LinkedList<Integer>();
		List<Integer> indexList = new LinkedList<Integer>();
		for (int i = 1; i <= classNum; i++) {
			classList.add(i);
			indexList.add(i);
		}
//		System.out.println("========favourates=========");
		for (int i = 0; i < favourateNum; i++) {
			int fid = (int)(Math.random() * indexList.size() - 1);
			favourates.add(classList.get(indexList.get(fid)));
			indexList.remove(fid);
		}
//		System.out.println("========unfavourates=========");
		for (int i = 0; i < unfavourateNum; i++) {
			int fid = (int)(Math.random() * indexList.size() - 1);
			unfavourates.add(classList.get(indexList.get(fid)));
			indexList.remove(fid);
		}
	}
	
	public static void gen(int gambleNum, int classNum, double expectedReturn) throws Exception {
		List<Double> rates = new ArrayList<Double>();
		double sum = 0;
		for (int i = 0; i < gambleNum; i++) {
			double r = getARandomNumInRange(1, 10);
			rates.add(r);
			sum += r;
		}
		for (int i = 0; i < gambleNum; i++) {
			double expect = rates.get(i) / sum * expectedReturn;
			Gamble gamble = generate(classNum, expect);
			gambles.add(gamble);
		}
	}

	public static Gamble generate(int classNum, double expectedReturn) throws Exception {
		Gamble gamble = new Gamble();
		int classId = (int)getARandomNumInRange(0, classNum - 1);
		gamble.classId = classId;
		gamble.id = gambles.size() + 1;

		BigDecimal bigDecimal = null;
		while (true) {
			while (true) {
				gamble.medProb = getARandomNumInRange(0.4, 1);
				bigDecimal = new BigDecimal(gamble.medProb);
				gamble.medProb = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

				if (gamble.medProb != 1) {
					break;
				}
			}
			
			while (true) {
				gamble.lowProb = getARandomNumInRange(0, 1 - gamble.medProb);
				bigDecimal = new BigDecimal(gamble.lowProb);
				gamble.lowProb = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				if (gamble.lowProb != 0 && gamble.lowProb != 1 - gamble.medProb) {
					break;
				}
			}
			
			gamble.highProb = 1 - gamble.medProb - gamble.lowProb;
			bigDecimal = new BigDecimal(gamble.highProb);
			gamble.highProb = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			if (gamble.highProb != 0) {
				break;
			}
		}
		
		
//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setMaximumFractionDigits(2);
//		while (true) {
//			double expectedReturnCopy = expectedReturn;
//			gamble.high_return = getARandomNumInRange(0, expectedReturnCopy / gamble.highProb);
//			//String hr = nf.format(gamble.high_return);
//
//			break;
//		}
		while (true) {
			double expectedReturnCopy = expectedReturn;
			gamble.high_return = getARandomNumInRange(0, expectedReturnCopy / gamble.highProb);
			//String hr = nf.format(gamble.high_return);
			//gamble.high_return = Double.parseDouble(hr);

			expectedReturnCopy -= gamble.high_return * gamble.highProb;

			gamble.medium_return = getARandomNumInRange(0, Math.min(gamble.high_return, expectedReturnCopy / gamble.medProb));
            //String mr = nf.format(gamble.medium_return);
           // gamble.medium_return = Double.parseDouble(mr);

			expectedReturnCopy -= gamble.medium_return * gamble.medProb;

			gamble.low_return = expectedReturnCopy / gamble.lowProb;
			//String lr = nf.format(gamble.low_return);
			//gamble.low_return = Double.parseDouble(lr);

			if (gamble.high_return > gamble.medium_return
					&& gamble.medium_return > gamble.low_return
					&& gamble.high_return > 1
					&& gamble.low_return < 1
					&& gamble.low_return > 0.09) {
				break;
			}
		}
	    //System.out.println(gamble);
		return gamble;
	}

	public static double getARandomNumInRange(double l, double h) throws Exception {
		if (h < l) {
			throw new Exception("the high bound should be no less than the low bound!");
		}
		return Math.random() * (h - l) + l;
	}

	public static double getARandomNumLargerThanAPositive(double n) throws Exception {
		if (n < 0) {
			throw new Exception("N should be no smaller than 0!");
		}
		double highBound = getARandomNumInRange(n, Double.MAX_VALUE);
		return getARandomNumInRange(n, highBound);
	}
	
	public static boolean checkGambles(double expect) throws Exception {
		boolean b = false;
		double expectedReturn = 0;
		for (Gamble gamble : gambles) {
			if (gamble.medProb < .4) {
				throw new Exception("medProb should < 0.4");
			}
			if (!(gamble.high_return > gamble.medium_return
					&& gamble.medium_return > gamble.low_return)) {
				System.err.println("high_return=" +  gamble.high_return
						           + "\nmed_return=" + gamble.medium_return
						           + "\nlow_return=" + gamble.low_return);
				throw new Exception("return are not in a proper order");
			}
			expectedReturn += (gamble.high_return * gamble.highProb
					         + gamble.medium_return * gamble.medProb
					         + gamble.low_return * gamble.lowProb);
		}
		if (Math.abs(expect - expectedReturn) > 1) {
			System.err.println(expectedReturn);
			throw new Exception("wrong!");
		}
		return b;
	}

	public static boolean checkCategory(int classNum) throws Exception {
//		System.out.println("favourates num=" + favourates);
//		for (Gamble gamble : gambles) {
//			if (favourates.contains(gamble.classId)) {
//				System.out.println("favourate " + gamble);
//			}
//		}
//		System.out.println("unfavourates num=" + unfavourates);
//		for (Gamble gamble : gambles) {
//			if (unfavourates.contains(gamble.classId)) {
//				System.out.println("unfavourate" + gamble);
//			}
//		}
		if (favourates.size() + unfavourates.size() > classNum) {
			throw new Exception("category wrong!");
		}
		return true;
	}
	
	//public static 

	public static void outputToFile() {
		FileWriter fw = null;
		try {
			fw = new FileWriter("output");
			fw.write("gamble\n");
			for (Gamble gamble : gambles) {
				fw.write(gamble.outputToFile() + "\n");
			}
			fw.write("\nlink\n");
			for (int i = 1; i < linkedMatrix.length; i++) {
				for (int j = i + 1; j < linkedMatrix.length; j++) {
					if (linkedMatrix[i][j] == 1) {
						fw.write(i + "," + j + "\n");
					}					
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void genLinks(int gambleNum) {
		linkedMatrix = new int[gambleNum + 1][gambleNum + 1];
		for (int i = 1; i <= gambleNum; i++) {
			for (int j = i + 1; j <= gambleNum; j++) {
				double dice = Math.random();
				if (dice < 0.8) {
					linkedMatrix[i][j] = 0;
					linkedMatrix[j][i] = 0;
				} else {
					linkedMatrix[i][j] = 1;
					linkedMatrix[j][i] = 1;
				}
			}
		}
	}

	public static boolean checkLinkMatrix() throws Exception {
		for (int i = 1; i < linkedMatrix.length; i++) {
			for (int j = 1; j < linkedMatrix.length; j++) {
				if (linkedMatrix[i][j] != linkedMatrix[j][i]) {
					throw new Exception("matrix not symmetric");
				}
			}
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		gen(200, 16, 200 * 2);
		checkGambles(400);
	    assignCategoryForGambles(16);
	    checkCategory(16);
	    genLinks(200);
	    checkLinkMatrix();
	    outputToFile();
	}
}
