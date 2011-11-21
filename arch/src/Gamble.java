import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class Gamble {
	
	int id;
	int classId;
	double high_return;
	double highProb;
	double medium_return;
	double medProb;
	double low_return;
	double lowProb;
	List<Double> results = new ArrayList<Double>();

	public Gamble() {}

	@Override
	public String toString() {
		return "Gamble [id=" + id + " classId=" + classId + " highProb=" + highProb
		       + " medProb=" + medProb + " lowProb=" + lowProb 
				+ " high_return=" + high_return + " medium_return=" + medium_return + " low_return=" + low_return  + "]\n";
	}

	public String outputToFile() {
		NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

		return id + "," + classId +","
		+ nf.format(high_return) + "," + highProb + ","
		+ nf.format(medium_return) + "," + medProb + ","
		+ nf.format(low_return) + "," + lowProb;
	}

	public Gamble(int id, int classId,
			      double high_return, double highProb,
			      double medium_return, double medProb,
			      double low_return, double lowProb)
	{
		this.id = id;
		this.classId = classId;
		this.high_return = high_return;
		this.highProb = highProb;
		this.medium_return = medium_return;
		this.medProb = medProb;
		this.low_return = low_return;
		this.lowProb = lowProb;
	}
	
	public double play()
	{			
				
		double dice = Math.random();
		if(dice<highProb)
		{
			results.add(high_return);
			return high_return;
		}
		else if(dice< highProb+medProb)
		{
			results.add(medium_return);
			return medium_return;
		}
		else
		{
			results.add(low_return);
			return low_return;
		}
		
	}
	
	public double playWithNewProb(double highprob,double medprob,double lowprob)
	{
		double dice = Math.random();
		if(dice<highprob)
		{
			results.add(high_return);
			return high_return;
		}
		else if(dice< highprob+medprob)
		{
			results.add(medium_return);
			return medium_return;
		}
		else
		{
			results.add(low_return);
			return low_return;
		}
	}
	
	public double getLastResult()
	{
		return results.get(results.size()-1);
	}

}
