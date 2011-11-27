import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Player {
	
	public Socket socket;
	private String name;
	private double wealth;
	public BufferedReader reader;
	public PrintWriter writer;
	private int score;
	private boolean cheated;
	private ArrayList<Double> returns;
	private double returnSum;
	private double variance;
	private double sharpeRatio;
	
	Player(Socket socket){
		this.socket = socket;
		this.wealth = 1.0;
		this.score = 0;
		this.cheated = false;
		returns = new ArrayList<Double>();
		variance = 0.0;
		returnSum = 0.0;
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
	
	public void caculateSharpeRatio()
	{
		double sum = 0.0;
		for(Double ret:returns)
		{
			sum += ret;
		}
		returnSum = sum;
		 //(sum of returns/sqrt variance of returns)
		double mean = sum/returns.size();
		double var = 0.0;
		for(Double ret:returns)
		{
			var += Math.pow(ret-mean, 2);
		}
		var = Math.sqrt(var/returns.size());
		this.variance = var;
		this.sharpeRatio = returnSum / var;
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

	public ArrayList<Double> getReturns() {
		return returns;
	}

	public void setReturns(ArrayList<Double> returns) {
		this.returns = returns;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getReturnSum() {
		return returnSum;
	}

	public void setReturnSum(double returnSum) {
		this.returnSum = returnSum;
	}

	public double getSharpeRatio() {
		return sharpeRatio;
	}

	public void setSharpeRatio(double sharpeRatio) {
		this.sharpeRatio = sharpeRatio;
	}

}
