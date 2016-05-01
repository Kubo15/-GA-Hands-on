package grafAlgo.grafAlgo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.Node;

import grafAlgo.DB.Database;
import grafAlgo.Utils.FileHelper;
import grafAlgo.Utils.Parser;
import grafAlgo.Utils.Util;

/**
 * Hello world!
 *
 */
public class App 
{

	//private static final String filesDirectory = "resources/rnd-graphs-normal-small";
	private static final String sequencesDirectory = "resources/node-sequences-small"; 

	private static final String filesDirectory = "resources/rnd-graph-pl";

	
	public static void main( String[] args ) throws IOException{
	
		List<File> files = FileHelper.getAllFileNamesFromDirectory(filesDirectory);
		List<File> sequencesFiles = FileHelper.getAllFileNamesFromDirectory(sequencesDirectory);
		

		for(int i=0;i<files.size();i++){
			Database db = new Database(files.get(i));
			System.out.println("Starting for file: " + db.getFilename());
			List<Integer> sequences = Parser.startNodesBFS(sequencesFiles.get(i).getAbsolutePath());
			System.out.println("Sequence: " + sequencesFiles.get(i).getAbsolutePath());
			for(int j=0;j<sequences.size(); j+=2){
				db.calculateDijkstraForTwoNodes(sequences.get(j), sequences.get(j+1));
			}
//			for(int j=0;j<sequences.size(); j++){
//				//db.breadthFirstSearch(sequences.get(j), 2);
//				//db.breadthFirstSearch(sequences.get(j), 3);
//			}
			System.out.println("Statistics for file: " + db.getFilename());
			System.out.println("Average time: " + Util.calculateAverage(db.getTimesOfExecution())*Math.pow(10, -6) + " ms");
			System.out.println("Max time: " + Util.calculateMax(db.getTimesOfExecution())*Math.pow(10, -6) + " ms");
			System.out.println("Average memory: " + (Util.calculateAverage(db.getMemoryUsed()))/Math.pow(1024, 2)+ " MB");
			System.out.println("Max memory: " + (Util.calculateMax(db.getMemoryUsed()))/Math.pow(1024, 2) + " MB");
			db.shutdown();
		}
		
		
		
		
//		List<Node> nodes = Parser.parseFile("resources/rnd-graphs-normal-small/rnd-graph-100-smallD.txt");

		
    }
}
