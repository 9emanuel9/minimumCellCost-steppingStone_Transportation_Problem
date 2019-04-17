
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileHelper {
	//method reads all the information from a nxn table and puts each info into an arraylist line by line
	protected static ArrayList<String> readFromFile (String filename){
		ArrayList <String> fileString=new ArrayList ();
		try {
			BufferedReader buffer = new BufferedReader (new FileReader(new File (filename)) );
			String st="";
			while ((st=buffer.readLine ())!=null){
				fileString.add (st);
			}
		}catch (FileNotFoundException e){
			System.out.println ("file not found");
			System.exit (1);
		}catch (IOException e ){
			System.out.println ("IOException");
		}
		return fileString;
	}
	//method returns number of occurances of the empty string in the string array
	protected static int occuranceOfEmptyStr (String[] string){
		int count = 0;
		for (int i = 0; i < string.length; i++) {
    		if (string[i].equals ("")) {
        		count++;
    		}
		}
		return count;
	}
	protected static ArrayList <String> extractWarehouseNames (ArrayList<String> file){
		String [] s=file.get (0).replaceAll("\\t", " ").split (" ");
		int emptyStr=occuranceOfEmptyStr (s);//checks how many occurances of the empty string we have
		ArrayList <String> temp= new ArrayList ();
		int j=1;
		for (int i =0;i <(s.length-emptyStr)-2;i++){
			//if statment checks if there is a blank string. 
			//If so it increments arr s and decrements arr temp cause we need to occupy the index
			if (s[j].equals ("")){
				System.out.println ("IGNORE:del later:Main class(extractWarehouseNames method)");
				j++;
				i--;
				continue;
			}
			temp.add (i,s[j]);
			j++;
		}
		return temp;
	}

	protected static ArrayList <String> extractFactoryNames (ArrayList <String> file){
		ArrayList<String []> al = new ArrayList ();
		ArrayList<String> factNames= new ArrayList ();
		for (int i=0; i <file.size();i++){
			al.add (file.get (i).replaceAll("\\t", " ").split (" "));
		}
		int j=1;
		for (int i = 0; i <al.size ()-2;i ++){
			factNames.add (i,al.get (j) [0]);
			j++;
		}
		return factNames;
	}
	protected static ArrayList extractDemand (ArrayList <String> file){
		String [] s=file.get (file.size ()-1).replaceAll("\\t", " ").split (" ");
		ArrayList demand=new ArrayList ();
		for (int i=0; i <s.length;i++){
			if (isNumeric (s[i])){
				demand.add (Integer.parseInt (s[i]));
			}
		}	

		return demand;
	}
	protected static ArrayList extractProduction (ArrayList<String> file){
		ArrayList<String []> al = new ArrayList ();
		ArrayList production= new ArrayList ();
		for (int i=0; i <file.size();i++){
			al.add (file.get (i).replaceAll("\\t", " ").split (" "));
		}
		int j=1;
		for (int i = 0; i <al.size ()-2;i ++){
			production.add (i,Integer.parseInt (al.get (j) [al.get (j).length-1]));
			j++;
		}
		return production;
	}
	protected static Transportation extractTranspoCosts (ArrayList <String> file){
		ArrayList<String []> al = new ArrayList ();
		Transportation transpoCost;
		for (int i=1; i <file.size()-1;i++){
			al.add (file.get (i).replaceAll("\\t", " ").split (" "));
		}
		int emptyStr=occuranceOfEmptyStr (al.get(0));//since we are checking individial indexes we need to see how many blank slates we are dealing with
		transpoCost = new Transportation (al.size (), (al.get (0).length-emptyStr)-2);//we subtract the number of empty strings by the length and subtract 
																					  //by two because we dont want the first or last index
																					  //(first index holds the factry names and warehouse names last index holds the demand and production)
		for (int i=0;i<transpoCost.getRowSize ();i++){
			int k=1;
			for (int j=0;j<transpoCost.getColumnSize ();j++){
				if (!isNumeric (al.get (i)[k])){
					k++;
					j--;
					continue;
				}
				transpoCost.addCostAt (i,j, new TranspoCost (Integer.parseInt (al.get (i)[k])));
				k++;
			}
		}
		return transpoCost;
	}
	//public because generally any class outside this folder could use it does not hold valuable information 
	public static boolean isNumeric (String s){
		boolean numeric=true;
		try {
			Integer.parseInt (s);
		}
		catch (NumberFormatException e){
			numeric=false;
		}
		return numeric;
	}
}