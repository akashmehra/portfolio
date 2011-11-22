import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Dealer extends JFrame{
	
	
    public Dealer() {
        this.setTitle("Portfolio Management");
        this.setSize(this.WIDTH, this.HEIGHT);
        this.setLocation((dim.width - this.WIDTH) / 2,
                         (dim.height - this.HEIGHT) / 2);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);

        winnerLabel.setFont(new Font("Dialog",1,20));
        wealthLabel.setFont(new Font("Dialog",1,20));
        roundLabel.setFont(new Font("Dialog",1,20));
        winnerLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        wealthLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        roundLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        
        infoPanel.add(winnerLabel,BorderLayout.LINE_START);
        infoPanel.add(wealthLabel,BorderLayout.CENTER);
        infoPanel.add(roundLabel,BorderLayout.LINE_END);
        
        this.add(infoPanel,BorderLayout.PAGE_START);
        //this.add(panel);
    }

	public static String HOST = "localhost";
	public static int PORT = 8888;
	public static int playerCount = 1;
	public static int ROUND = 200;
	public static String filePath = "gambles";
	public static HashMap<Integer, Integer> classProp = new HashMap<Integer, Integer>();
	public static int MODE = 1;
	public static int classNum = 16;
	public static int CLASSCHANGETIMES = 10;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private static MyPanel panel = new MyPanel();
    private static JPanel infoPanel = new JPanel();
    private static JLabel winnerLabel = new JLabel("Winner is Nobody");
    private static JLabel wealthLabel = new JLabel("Wealth is 0");
    private static JLabel roundLabel = new JLabel("Round 0");
    java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit()
            .getScreenSize();


	private ServerSocket server;
	private List<Player> playerList = new ArrayList<Player>();
	private List<Gamble> gambleList = new ArrayList<Gamble>();
	private List<Integer> gambleOrder = new ArrayList<Integer>();
	private int linkedMatrix[][] ;
	private List<Double> result = new ArrayList<Double>();
	private List<String> outputToClient = new ArrayList<String>();
	private HashMap<String,String> clientResult = new HashMap<String,String>();
	private String tables;
	private List<Player> winners = new ArrayList<Player>();


	private List<Integer> roundToChangeType = new ArrayList<Integer>();
	
	public void readGamblesFromFile(String path)
	{
	  FileReader fr = null;
	  BufferedReader br = null;
	  StringBuilder sb = new StringBuilder();
	  int category = 0;
	  try {
		fr = new FileReader(path);
		String line;
		br = new BufferedReader(fr);
		while ((line = br.readLine()) != null) {
			
			sb.append(line+"\n");
	
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			if (line.equalsIgnoreCase("gamble")) {
				category = 0;
				continue;
			}
			/*
			if (line.equalsIgnoreCase("gambleatts")) {
				category = 1;
			}*/
			if (line.equalsIgnoreCase("link")) {
				category = 2;
				linkedMatrix= new int[gambleList.size()+1][gambleList.size()+1];
				continue;
			}
			String[] parts = line.split(",");
			switch (category) {
			case 0:
				Gamble gamble = new Gamble(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
						                   Double.parseDouble(parts[2]), Double.parseDouble(parts[3]),
						                   Double.parseDouble(parts[4]), Double.parseDouble(parts[5]),
						                   Double.parseDouble(parts[6]), Double.parseDouble(parts[7]));
				gambleList.add(gamble);
				break;
			case 1:
				break;
			case 2:
				int row = Integer.parseInt(parts[0]);
				int col = Integer.parseInt(parts[1]);
				linkedMatrix[row][col] = 1;
				linkedMatrix[col][row] = 1;
				break;
			}
		}
	  } catch (Exception e) {
		// TODO: handle exception
	  } finally {
		  if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  if (fr != null) {
			try {
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	  }
	  
	  tables = sb.toString();
	  System.out.println(tables);
	  

	}

	public void genreateGambleOrder() {
		List<Integer> inOrder = new ArrayList<Integer>();
		gambleOrder.clear();
		for (int i = 0; i < gambleList.size(); i++) {
			inOrder.add(i);
		}
		for (int i = 0; i < gambleList.size(); i++) {
			int pos = (int) (Math.random() * inOrder.size());
			gambleOrder.add(inOrder.get(pos));
			inOrder.remove(pos);
		}
	}

	public List<Double> getListFromString(String allocation) {
		String[] parts = allocation.split(",");
		List<Double> newList = new ArrayList<Double>();
		for (String part : parts) {
			Double value = Double.parseDouble(part);
			newList.add(value);
		}
		return newList;
	}

	public String convertListToString(List<Double> list) {
		StringBuilder sb = new StringBuilder();
		
		DecimalFormat nf = new DecimalFormat("####.######");
		for (int i = 0; i < list.size() - 1; i++)
		{
			sb.append(nf.format(list.get(i))+ ",");
		}
		sb.append(nf.format(list.get(list.size() - 1)));
		return sb.toString();

	}

	public boolean verifyAllocation(List<Double> allocation, double max) {
		
		double sum = 0.0;
		for (Double value : allocation) {
			if(value<0)
				return false;
			sum += value;
		}
		if (sum > max+0.001)
			return false;
		else
			return true;
	}
	
	public void assignClassType()
	{
		for(int i=0;i<classNum;i++)
		{
			double rand = Math.random();
			if(rand<1.0/3)
			{
				classProp.put(i, 1);
			}else if (rand< 2.0/3)
			{
				classProp.put(i, 0);
			}else
				classProp.put(i, -1);
		}
	}

	public void playARound() {

		genreateGambleOrder();

		for (int i = 0; i < gambleOrder.size(); i++) {

			/*
			 * Update probabilities based on its class
			 */

			Gamble current = gambleList.get(gambleOrder.get(i));

			double highProb = current.highProb;
			double medProb = current.medProb;
			double lowProb = current.lowProb;

			if (classProp.get(current.classId) == 1) {
				highProb = current.highProb + current.lowProb / 2;
				medProb = current.medProb;
				lowProb = current.lowProb / 2;

			} else if (classProp.get(current.classId) == -1) {
				highProb = current.highProb / 2;
				medProb = current.medProb;
				lowProb = current.highProb / 2 + current.lowProb;
			}

			int Hi = 0;
			int Mi = 0;
			int Li = 0;

			for (int j = 0; j < i; j++) {
				Gamble previous = gambleList.get(gambleOrder.get(j));
				if (linkedMatrix[current.id][previous.id] == 1) {
					if (previous.getLastResult() == previous.high_return) {
						Hi++;
					} else if (previous.getLastResult() == previous.medium_return) {
						Mi++;
					} else {
						Li++;
					}
				}
			}

			/*
			 * If Hi > Mi + Li, then halve gilowprob (from the value that it
			 * might have already been assigned based on its class) the value
			 * that and add that probability to gihiprob. If Li > Hi + Mi, then
			 * halve gihiprob and add that probability to gilowprob
			 */

			if (Hi > Mi + Li) {
				highProb = highProb + lowProb / 2;
				lowProb = lowProb / 2;

			} else if (Li > Hi + Mi) {
				lowProb = lowProb + highProb / 2;
				highProb = highProb / 2;
			}

			current.playWithNewProb(highProb, medProb, lowProb);

		}

	}

	public ArrayList<Double> computeOutcome(List<Double> alloc) {
		ArrayList<Double> outcome = new ArrayList<Double>();
		for (int i = 0; i < gambleList.size(); i++) {
			Double amount = alloc.get(i);
			Double returnValue = gambleList.get(i).getLastResult();
			Double newValue = amount * returnValue;
			outcome.add(newValue);
		}
		return outcome;
	}

	public String readTablesFromFile() throws IOException {
		
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			sb.append(readData);
			buf = new char[1024];
		}

		reader.close();
		return sb.toString();

	}

	public double sum(List<Double> outcome) {
		
		double total = 0.0;

		for (Double value : outcome) {
			total += value;
		}

		return total;
	}
	
	
	public void generateTypeChangeList()
	{
		for(int i =0;i<CLASSCHANGETIMES;i++)
		{
			int round = 0;
			do{
				round = (int)(Math.random()*ROUND);
			}while(roundToChangeType.contains(round));
			
			roundToChangeType.add(round);
			
		}
	}
	
	public void classTypeChange()
	{
		//random pick one class
		int classId = (int)(Math.random()*classNum)%classNum;
		int classType = classProp.get(classId);
		if(classType == -1)
		{
			int newType = (Math.random()>0.5?0:1);
			classProp.put(classId, newType);
		}else if(classType == 1)
		{
			int newType = (Math.random()>0.5?0:-1);
			classProp.put(classId, newType);
		}else
		{
			int newType = (Math.random()>0.5?1:-1);
			classProp.put(classId, newType);
		}
	}
	
	public String getEveryoneWealth()
	{
		StringBuilder positions = new StringBuilder();
		DecimalFormat formatter = new DecimalFormat("#####.######"); 
		for(int j =0;j< playerCount-1;j++)
		{
			Player  player = playerList.get(j);
			positions.append(player.getName()+":"+formatter.format(player.getWealth())+",");
		}
		positions.append(playerList.get(playerList.size()-1).getName()+":"+formatter.format(playerList.get(playerList.size()-1).getWealth()));
		return positions.toString();
		
	}
	
	public void loadHistogram(List<Player> playerList, int mode)
	{
        Dealer.panel.setHistogramTitle("Wealth", "Player");
        if(mode == 1)
        {
        	for(Player p:playerList)
        	{
        		MyPanel.insert(p.getName(), p.getScore(),p.isCheated());
        	}
        }else
        {
        	for(Player p:playerList)
        	{
        		MyPanel.insert(p.getName(), p.getWealth(),p.isCheated());
        	}
        	
        }  
        
        Dealer.panel.updateUI();
	}
	
	
	
	public void updateHistogram(List<Player>playerList,int mode)
	{
        if(mode == 1)
        {
        	for(Player p:playerList)
        	{
        		MyPanel.update(p.getName(), p.getScore(),p.isCheated());
        	}
        }else
        {
        	for(Player p:playerList)
        	{
        		MyPanel.update(p.getName(), p.getWealth(),p.isCheated());
        	}
        	
        }
        
        Dealer.panel.updateUI();
		
	}
	
	public void updateInfo(List<Player>playerList,int mode,int round)
	{
		
		DecimalFormat df = new DecimalFormat("####.######");
        if(mode == 1)
        {
        	StringBuilder sb = new StringBuilder();
        	
        	for(Player p:playerList)
        	{
        		sb.append(p.getName()+" ");
        	}
        	
        	winnerLabel.setText("Winner : "+sb.toString());
        	wealthLabel.setText("Score : "+df.format(playerList.get(0).getScore()));
        	roundLabel.setText("Round "+String.valueOf(round));
        	
        }else
        {
        	StringBuilder sb = new StringBuilder();
        	
        	for(Player p:playerList)
        	{
        		sb.append(p.getName()+" ");
        	}
        	
        	winnerLabel.setText("Winner :"+ sb.toString());
        	wealthLabel.setText("Wealth : "+df.format(playerList.get(0).getWealth()));
        	roundLabel.setText("Round "+" "+String.valueOf(round));
     	
        }
        
        Dealer.infoPanel.updateUI();
		
	}
	
	
	public void reportResult(List<Player>playerList,int mode)
	{
		
		DecimalFormat df = new DecimalFormat("####.######");
        if(mode == 1)
        {
        	StringBuilder sb = new StringBuilder();
        	
        	for(Player p:playerList)
        	{
        		sb.append(p.getName()+" ");
        	}
        	
        	winnerLabel.setText("Winner : "+sb.toString());
        	wealthLabel.setText("Score : "+df.format(playerList.get(0).getScore()));
        	roundLabel.setText("GAME END");
        	
        }else
        {
        	StringBuilder sb = new StringBuilder();
        	
        	for(Player p:playerList)
        	{
        		sb.append(p.getName()+" ");
        	}
        	
        	winnerLabel.setText("Winner :"+ sb.toString());
        	wealthLabel.setText("Wealth : "+df.format(playerList.get(0).getWealth())+" : "+df.format(playerList.get(0).getSharpeRatio()));
        	roundLabel.setText("GAME END");
     	
        }
        
        Dealer.infoPanel.updateUI();
		
	}
	
	public void playGameOne() {

		try {
			
			server = new ServerSocket(PORT);

			for (int i = 0; i < playerCount; i++) {
				Socket socket = server.accept();
				Player gambler = new Player(socket);
				String name = gambler.readFromClient();
				gambler.setName(name);
				playerList.add(gambler);
			}

			readGamblesFromFile(filePath);
			assignClassType();
			this.add(panel,BorderLayout.CENTER);
			loadHistogram(playerList,1);


			// Send three tables
			for (int i = 0; i < playerCount; i++) {

				Player player = playerList.get(i);

				player.writeToClient(tables);
				player.writeToClient("Give allocation:");
			}

			for (int i = 0; i < ROUND; i++) {
				
				outputToClient.clear();
				
				playARound();
				
				System.out.print("Round "+i+" :");
		        String ticker = "";
				for(int k=0;k<gambleList.size()-1;k++)
				{
					ticker += gambleList.get(k).getLastResult()+",";
				}
				ticker += gambleList.get(gambleList.size()-1).getLastResult();
				
				System.out.println(ticker);
				
				for (int j = 0; j < playerCount; j++) {

					Player player = playerList.get(j);
					
					if(player.isCheated() == true)
						continue;
					
					String allocation = player.readFromClient();

					List<Double> allocList = getListFromString(allocation);
					boolean valid = verifyAllocation(allocList, 1.0);
					player.setCheated(!valid);
					
					if(player.isCheated() == true)
					{
						System.out.println(player.getName()+" cheat: sum of your input is "+sum(allocList));
						player.writeToClient("END Cheat!");
						continue;
					}
					
					double outcome = 0;

					result = computeOutcome(allocList);

					outcome = sum(result);

					if (outcome >= 2) {
						player.increaseScoreByOne();
					}

					String toClient = convertListToString(result);
					clientResult.put(player.getName(), toClient);
					//outputToClient.add(toClient);
					//player.writeToClient(toClient);
				}
				
				double maxScore = 0;
				for(int j =0;j< playerCount;j++)
				{
					Player player = playerList.get(j);
					if(player.isCheated() == true)
						continue;
					
					if(player.getScore()>maxScore)
					{
						maxScore = player.getScore();
						winners.clear();
						winners.add(player);
					} else if(player.getScore() == maxScore)
					{
						winners.add(player);
					}
					
					player.writeToClient(ticker);
					player.writeToClient(clientResult.get(player.getName()));
					if(i != ROUND-1)
					player.writeToClient("Give allocation:");
				}
				
				updateHistogram(playerList,1);
				updateInfo(winners,1,i+1);
				
			}

			
			System.out.println("Game End");
			double maxScore = 0;

			for (int j = 0; j < playerCount; j++) {

				Player player = playerList.get(j);
				if(player.isCheated() == false)
				{
					if(player.getScore() > maxScore)
					{
						maxScore = player.getScore();
						winners.clear();
						winners.add(player);
					}else if(player.getScore() == maxScore)
					{
						winners.add(player);
					}
					System.out.println("Player " + player.getName() + " : "
						+ player.getScore());
					player.writeToClient("END YOUR SCORE IS " + player.getScore());
				}else
				{
					System.out.println("Player " + player.getName() + " : Cheated");
				}

			}
			
			reportResult(winners,1);
			
			System.out.println("Winner:");
			for(Player p:winners)
			{
				System.out.println(p.getName()+" : "+p.getScore());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void playGame() {

		try {
			server = new ServerSocket(PORT);

			for (int i = 0; i < playerCount; i++) {
				Socket socket = server.accept();
				Player gambler = new Player(socket);
				String name = gambler.readFromClient();
				gambler.setName(name);
				playerList.add(gambler);
			}

			readGamblesFromFile(filePath);
			assignClassType();
			this.add(panel,BorderLayout.CENTER);
			loadHistogram(playerList,2);

			// Send three tables
			for (int i = 0; i < playerCount; i++) {

				Player player = playerList.get(i);

				player.writeToClient(tables);
				player.writeToClient("Give allocation:");
			}

			for (int i = 0; i < ROUND; i++) {
				
				outputToClient.clear();
				
				if(roundToChangeType.contains(i))
				{
					classTypeChange();
				}
				
				playARound();
				
				System.out.print("Round "+i+" :");
				
				
		        String ticker = "";
				for(int k=0;k<gambleList.size()-1;k++)
				{
					ticker += gambleList.get(k).getLastResult()+",";
				}
				ticker += gambleList.get(gambleList.size()-1).getLastResult();
				
				System.out.println(ticker);

				for (int j = 0; j < playerCount; j++) {
					Player player = playerList.get(j);
					
					if(player.isCheated() == true)
						continue;
					
					String allocation = player.readFromClient();

					List<Double> allocList = getListFromString(allocation);
					boolean valid = verifyAllocation(allocList, player.getWealth());
					player.setCheated(!valid);
					
					if(player.isCheated() == true)
					{
						System.out.println(player.getName()+" cheat: current wealth is "+player.getWealth()+" Invest "+sum(allocList));
						player.writeToClient("END Cheat! Your current wealth is "+player.getWealth()+" but you invest "+ sum(allocList));
						continue;
					}
					
					double cost = sum(allocList);
					player.setWealth(player.getWealth()-cost);
					
					// boolean valid = verifyAllocation(allocList,
					// player.getWealth());
					double outcome = 0;

					result = computeOutcome(allocList);
					outcome = sum(result);
					
					double gamblesReturn = outcome - cost;

					player.setWealth(player.getWealth()+outcome);
					player.getReturns().add(gamblesReturn);

					String toClient = convertListToString(result);
					clientResult.put(player.getName(), toClient);
					//outputToClient.add(toClient);
					
					//player.writeToClient(toClient);
				}
				
				String position = getEveryoneWealth();
				double maxPosition = Double.MIN_VALUE;
				for(int j =0;j< playerCount;j++)
				{
					
					Player player = playerList.get(j);
					if(player.isCheated() == true)
						continue;
					
					if(player.getWealth()>maxPosition)
					{
						maxPosition = player.getWealth();
						winners.clear();
						winners.add(player);
					} else if(player.getWealth() == maxPosition)
					{
						winners.add(player);
					}
					player.writeToClient(ticker);
					player.writeToClient(clientResult.get(player.getName()));
					player.writeToClient(position);
					if(i != ROUND-1)
						player.writeToClient("Give allocation:");
				}
				
				updateHistogram(playerList,2);
				updateInfo(winners,2,i+1);

				

			}

			System.out.println("Game End");
			double maxRatio = Double.MIN_VALUE;
			DecimalFormat df = new DecimalFormat("####.######");
			
			for (int j = 0; j < playerCount; j++) {

				Player player = playerList.get(j);
				player.caculateSharpeRatio();
				
				if(player.isCheated() == false)
				{
					if(player.getSharpeRatio() > maxRatio)
					{
						maxRatio = player.getSharpeRatio();
						winners.clear();
						winners.add(player);
					}
					else if(player.getSharpeRatio() == maxRatio)
					{
						winners.add(player);
					}
					System.out.println("Player " + player.getName() + " : "
						+ df.format(player.getWealth())+" : "+ df.format(player.getSharpeRatio()));
					player.writeToClient("END YOUR WEALTH IS " + df.format(player.getWealth())+ " SHARPERATION : " + df.format(player.getSharpeRatio()));
				}else
				{
					System.out.println("Player " + player.getName() + " : Cheated");
				}

			}
			
			reportResult(winners,2);

			
			System.out.println("Winner:");
			for(Player p:winners)
			{
				System.out.println(p.getName()+" : "+df.format(p.getWealth())+" : "+df.format(p.getSharpeRatio()));
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void test()
	{
		readGamblesFromFile("gambles");
		System.out.println(gambleList.size());
		System.out.println(tables);
		for(int i = 0; i< gambleList.size();i++)
		{
			System.out.println(gambleList.get(i));
		}
		
		genreateGambleOrder();
		for(int i=0;i<gambleOrder.size();i++)
		{
			System.out.print(gambleOrder.get(i)+" ");
		}
		System.out.println();
		List<Double> list = getListFromString("0.3,0.4,0.3");
		printList(list);
		System.out.println(convertListToString(list));
		System.out.println(verifyAllocation(list,0.6));
		assignClassType();
		playARound();
		for(int i=0;i<gambleList.size();i++)
		{
			System.out.println(gambleList.get(i).id+" : "+gambleList.get(i).getLastResult());
		}
		ArrayList<Double> result = computeOutcome(list);
		printList(result);
		double total = sum(result);
		System.out.println(total);
		generateTypeChangeList();
		printList(roundToChangeType);
	}
	
	void printList(List list)
	{
		for(int i=0;i<list.size();i++)
			System.out.println(list.get(i));
	}
	public static void main(String args[]) {

		
		if (args.length != 6) {
			
			System.out.println("Usage: port mode playerCount rounds classNum filepath");
			System.exit(1);
		}
		
		Dealer.PORT = Integer.parseInt(args[0]);
		Dealer.MODE = Integer.parseInt(args[1]);
		Dealer.playerCount = Integer.parseInt(args[2]);
		Dealer.ROUND = Integer.parseInt(args[3]);
		Dealer.classNum = Integer.parseInt(args[4]);
		Dealer.filePath = args[5];
		
		Dealer dealer = new Dealer();
		dealer.setVisible(true);
		
		if(Dealer.MODE == 1)
		{
			dealer.playGameOne();
		}else{
			dealer.playGame();
		}
		
		try {
			
			dealer.server.close();
			for(int i= 0;i< dealer.playerList.size();i++)
			{
				Player player = dealer.playerList.get(i);
				player.reader.close();
				player.writer.close();
				player.socket.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		Dealer dealer = new Dealer();
		dealer.test();
		*/
		// dealer.playGame();
	}

}
