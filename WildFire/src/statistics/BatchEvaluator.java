package statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class BatchEvaluator {
	public static final int tickColumn = 5;
	public static final int ticks = 2001;
	public static final int runs = 100;
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		MyEntry[] entries = new MyEntry[ticks];
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("output/evaluation.txt"));
		    String headline = br.readLine().replace(',', ';');
		    String line = br.readLine();

		    while (line != null) {
		    	MyEntry next = new MyEntry( line.split(","));
		    	MyEntry before = entries[(int)next.getValue(tickColumn)];
		    	if(before == null)
		    		entries[(int)next.getValue(tickColumn)]=next;
		    	else
		    		before.addEntry(next);
		        line = br.readLine();
		    }
		    PrintWriter writer = new PrintWriter("output.csv", "UTF-8");
		    writer.println(headline);
		    for(int i = 1;i<ticks;i++){
		    	entries[i].average(runs);
		    	writer.println(entries[i].toLine());
		    }
		    writer.close();
		    br.close();
		} catch (IOException e) {
		} 
		
	}
}
class MyEntry{
	private double[] values;
	public MyEntry(String[] split){
		values = new double[split.length];
		for(int i = 0;i<split.length;i++)
			values[i] = Double.parseDouble(split[i]);
	}
	public void addEntry(MyEntry add){
		for(int i = 0;i<values.length;i++){
			if(i!=BatchEvaluator.tickColumn)
				this.values[i] += add.values[i];
		}
	}
	public void average(int runs){
		for(int i = 0;i<values.length;i++)
			if(i!=BatchEvaluator.tickColumn)
				this.values[i] /=runs;
	}
	public double getValue(int index){
		return values[index];
	}
	public String toLine(){


		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator(',');
		DecimalFormat f = new DecimalFormat("#0.00",dfs); 
		
		String s = ""+values[0];
		for(int i = 1;i<values.length;i++){
			s+=";"+f.format(values[i]);
		}
		return s;
	}
}
