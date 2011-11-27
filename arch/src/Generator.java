import java.util.ArrayList;
import java.util.List;


public class Generator {
	
	public static List<Gamble> gambles = new ArrayList<Gamble>();
	public static int linkedMatrix[][] = null;
	
	
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
	
	public static void outputToFile() {

			System.out.println("gamble");
			for (Gamble gamble : gambles) {
				System.out.println(gamble.outputToFile());
			}
			System.out.println("link");
			System.out.println("link");
			for (int i = 1; i < linkedMatrix.length; i++) {
				for (int j = i + 1; j < linkedMatrix.length; j++) {
					if (linkedMatrix[i][j] == 1) {
						System.out.println(i + "," + j);
					}					
				}
			}
	}

}
