import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;


public class Client {
	

	
	public static void main(String argv[]) throws UnknownHostException, IOException
	{
		//host port mode name
		if(argv.length!=6)
		{
			System.out.println("Usage: host port mode gambelNum classNum name ");
			System.exit(1);
		}
		
		String host = argv[0];
		int port = Integer.parseInt(argv[1]);
		int mode = Integer.parseInt(argv[2]);
		int gambelNum = Integer.parseInt(argv[3]);
		int classNum = Integer.parseInt(argv[4]);
		String name = argv[5];
		Socket clientSocket = new Socket(host,port);
		BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(),true);
		
		writer.println(name);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		
        String command;
        StringBuffer state = new StringBuffer();
        try {
            while ((command = reader.readLine())!= null) {
          
                if (command.equals("Give allocation:")) {
                	state.append(command);
                	System.out.println(state);
        			String output = in.readLine();
                	//String output = simpleGuess(gambelNum);
                	System.out.println("Allocation: "+output);
        			writer.println(output);
                    state.delete(0, state.length());
                    continue;
                }
                if(command.startsWith("END"))
                {
                	System.out.println(command);
                	break;
                }
                state.append(command+"\n");
            }
        }
        catch (IOException io) {
            System.err.println(io.getMessage());
        }
        
        writer.close();
        try {
            in.close();
            clientSocket.close();
         
        } catch (IOException io) {
            System.err.println(io.getMessage());
        }
       
		
		
	}
	public static String simpleGuess(int gambelNum)
	{
		StringBuilder sb = new StringBuilder();
		
		double total = 1.0;
		for(int i=0;i<gambelNum-1;i++)
		{
			double rand = 1.0/gambelNum;
			NumberFormat nbf=NumberFormat.getInstance(); 
			nbf.setMaximumFractionDigits(2);
			String output = nbf.format(rand);
			sb.append(output+",");
			total -= Double.valueOf(output);
		}
		sb.append(total);
		return sb.toString();
	}
}
