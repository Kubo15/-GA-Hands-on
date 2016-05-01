package grafAlgo.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import grafAlgo.DB.DBNode;

public class Parser {
	public static List<DBNode> parseFile(String filename) throws IOException{
		List<DBNode> data = new ArrayList<DBNode>();
		
		FileInputStream fstream = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		while ((strLine = br.readLine()) != null)   {
			Scanner scanner = new Scanner(strLine);
			data.add(new DBNode(scanner.nextInt(), scanner.nextInt()));
			scanner.close();
		}
		System.out.println("Done parsing");

		br.close();

		return data;
	}
	
	public static List<Integer> startNodesBFS(String filename) throws IOException{
		List<Integer> data = new ArrayList<Integer>();
		FileInputStream fstream = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		while ((strLine = br.readLine()) != null)   {
			Scanner scanner = new Scanner(strLine);
			data.add(scanner.nextInt());
			scanner.close();
		}
		System.out.println("Done parsing Sequences");

		br.close();

		return data;
	}
}
