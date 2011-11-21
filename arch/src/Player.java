import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Player {
	
	public Socket socket;
	private String name;
	private double wealth;
	public BufferedReader reader;
	public PrintWriter writer;
	private int score;
	private boolean cheated;
	
	Player(Socket socket){
		this.socket = socket;
		this.wealth = 1.0;
		this.score = 0;
		this.cheated = false;
		try {
			this.writer = new PrintWriter(socket.getOutputStream(),true);
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String readFromClient()
	{
		String line ="";
		try {
			line = reader.readLine();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return line;
	}
	public void writeToClient(String content)
	{
		writer.println(content);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getWealth() {
		return wealth;
	}

	public void setWealth(double wealth) {
		this.wealth = wealth;
	}
	
	public void increaseScoreByOne()
	{
		this.score++;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public boolean isCheated() {
		return cheated;
	}

	public void setCheated(boolean cheated) {
		this.cheated = cheated;
	}

}
