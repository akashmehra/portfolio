import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;


class MyPanel extends JPanel {
	
	
    private boolean flag = true;
    private final int SIZE = 100;
    private final int CAKY_WIDTH = 400;
    private final int STEP = 10;
    private String xTitle;
    private String yTitle;
    private static List<String> elem = new ArrayList<String>();
    private static List<Double> value = new ArrayList<Double>();
    private static List<Boolean> state = new ArrayList<Boolean>();
	private static DecimalFormat df = new DecimalFormat("####.######");
    
    public MyPanel() {
        this.xTitle = "X";
        this.yTitle = "Y";
    }
    public MyPanel(String x, String y) {
        this.xTitle = x;
        yTitle = y;
    }
    public static void insert(String aElem, double aValue, boolean status) {
        elem.add(aElem);
        value.add(aValue);
        state.add(status);
    }
    public static void update(String aElem, double aValue,boolean status)
    {
    	int index = elem.indexOf(aElem);
    	value.set(index, aValue);
    	state.set(index, status);
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clearRect(0, 0, Dealer.WIDTH, Dealer.HEIGHT);
        drawHistogram(g);
    }

    public void drawHistogram(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("SANS_SERIF", Font.PLAIN, 15));
        g.drawString("Wealth Histogram", SIZE + 200, 30);
        g.setColor(Color.GREEN);
        //vertical line
        g.drawLine(SIZE, Dealer.HEIGHT - SIZE, SIZE, SIZE);
        //horizontal line
        g.drawLine(SIZE, Dealer.HEIGHT - SIZE, Dealer.WIDTH - SIZE,
                   Dealer.HEIGHT - SIZE);
        //arrow
        g.setColor(Color.RED);
        int[] x1 = {SIZE - 6, SIZE, SIZE + 6};
        int[] y1 = {SIZE + 8, SIZE, SIZE + 8};
        g.drawPolyline(x1, y1, 3);
        int[] x2 = {Dealer.WIDTH - SIZE - 8, Dealer.WIDTH - SIZE,
                   Dealer.WIDTH - SIZE - 8};
        int[] y2 = {Dealer.HEIGHT - SIZE - 6, Dealer.HEIGHT - SIZE,
                   Dealer.HEIGHT - SIZE + 6};
        g.drawPolyline(x2, y2, 3);
        //title
        g.drawString(this.yTitle, SIZE - 20, SIZE - 6);
        g.drawString(this.xTitle, Dealer.WIDTH - SIZE - 20,
                     Dealer.HEIGHT - SIZE + 20);
        
        //compute width
        int wigth = (int) ((Dealer.WIDTH - 3 * SIZE) / (value.size() * 2));

        double max = 0;
        for (Double elem : value) {
            if (max < elem) {
                max = elem;
            }
        }
        
        double num = (double) (Dealer.HEIGHT - 2 * (SIZE + 10)) / (double) (1.2*(max+0.001));

        for (int i = 0; i < elem.size(); i++) {
            int height = (int) (value.get(i) * num);
            // g.drawRect(wigth*(i*2+2),Main.HEIGHT-SIZE-height,wigth,height);
            g.setColor(new java.awt.Color(Digit.getDigit(255),
                                          Digit.getDigit(255),
                                          Digit.getDigit(255)));
            //fill color
            g.fillRect(wigth * (i * 2 + 1) + SIZE, Dealer.HEIGHT - SIZE - height,
                       wigth, height);
            g.setColor(Color.RED);
            
            if(state.get(i)==false)
            {
            	g.drawString(df.format(value.get(i)),
                         wigth * (i * 2 + 1) + SIZE,
                         Dealer.HEIGHT - SIZE - 20 - height);
            }else
            {
                g.drawString("Cheated",
                        wigth * (i * 2 + 1) + SIZE,
                        Dealer.HEIGHT - SIZE - 20 - height);
            	
            }
            
            g.drawString(elem.get(i), wigth * (i * 2 + 1) + SIZE,
                         Dealer.HEIGHT - SIZE + 20);
            
            g.drawString(df.format(value.get(i)), SIZE - 40,
            					Dealer.HEIGHT - SIZE - height + 5);
   
            g.drawLine(SIZE, Dealer.HEIGHT - SIZE - height, SIZE + 3,
                       Dealer.HEIGHT - SIZE - height);
        }
    }

    public void setHistogramTitle(String y, String x) {
        xTitle = x;
        yTitle = y;
    }

}

class Digit {
    public Digit() {
    }
    public static int getDigit(int digit) {
        java.util.Random ran = new Random();
        return (int) (ran.nextDouble() * digit);
    }
}
