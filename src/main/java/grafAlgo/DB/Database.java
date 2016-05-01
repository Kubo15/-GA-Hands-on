package grafAlgo.DB;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import grafAlgo.Utils.FileHelper;
import grafAlgo.Utils.Parser;

public class Database {

    private GraphDatabaseService database;
    private String filename;
	private String databaseName;
	private List<Long> timesOfExecution= new ArrayList<>();
	private List<Long> memoryUsed= new ArrayList<>();
	private PathFinder<WeightedPath> dijkstra;



	public Database(File file) throws IOException{
		this.setFilename(file.getAbsolutePath());
		String dbName = "resources/neo4jdb/" + FileHelper.removeFileSuffix(file.getName());
		File f = new File(dbName);
		boolean isLoaded = false;
		if (f.isDirectory()) {
		   isLoaded = true;
		   System.out.println("Database already exists and data is loaded");
		}
		this.setDatabaseName(dbName);
    	this.setDatabase(createDB(this.getDatabaseName()));
    	registerShutdownHook(this.getDatabase());
    	if(!isLoaded){
    		addNodesFromFile();
    	}
    	
    	this.setDijkstra(getDijkstraFinder());
    }
    
    private GraphDatabaseService createDB(String databaseName){
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(databaseName));
		return graphDb;
    }
    
    public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
    
	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
    
    public GraphDatabaseService getDatabase() {
		return database;
	}

	public void setDatabase(GraphDatabaseService database) {
		this.database = database;
	}

	public List<Long> getTimesOfExecution() {
		return timesOfExecution;
	}

	public void setTimesOfExecution(List<Long> timesOfExecution) {
		this.timesOfExecution = timesOfExecution;
	}

	public List<Long> getMemoryUsed() {
		return memoryUsed;
	}

	public void setMemoryUsed(List<Long> memoryUsed) {
		this.memoryUsed = memoryUsed;
	}
	
	
	public PathFinder<WeightedPath> getDijkstra() {
		return dijkstra;
	}

	public void setDijkstra(PathFinder<WeightedPath> dijkstra) {
		this.dijkstra = dijkstra;
	}

	public void shutdown(){
		this.getDatabase().shutdown();
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
	
	public void addNodesFromFile() throws IOException{
		List<DBNode> nodes = Parser.parseFile(this.getFilename());

		for(DBNode node : nodes){
			try ( Transaction tx = this.getDatabase().beginTx() )
			{
			
				Node firstNode = getOrPutNode(node.getName());
					
				Node secondNode = getOrPutNode(node.getNeighbor());
				//System.out.println("Nodes: " + node.getName() + " -> " +  node.getNeighbor());
				//System.out.println("Nodes: " + firstNode.getId() + " -> " +  secondNode.getId());

					
				addRelationIfDoesntExist(firstNode, secondNode);	
				tx.success();
			}
		}
		System.out.println("Done adding nodes");
	}
	
	public Node getOrPutNode(int name){
		Index<Node> index = this.getDatabase().index().forNodes("dbNodes");
		
		IndexHits<Node> dbNodes = index.get("name", name);
		
		Node node;
		if(!dbNodes.hasNext()){
			node = this.getDatabase().createNode();
			node.setProperty( "name", name);
			index.add(node, "name", name);
		} else {
		    node = dbNodes.next();
		    //System.out.println("Node " + name + " exists");
		}
		
		return node;
	} 
	
	public void addRelationIfDoesntExist(Node n1, Node n2){
		
		for (Relationship rel : n1.getRelationships()) { // n1.getRelationships(type,direction)
			if (rel.getEndNode().equals(n2)){ 
				//System.out.println("Relationship already exists");
				return;
		    }
		}
		Relationship rel = n1.createRelationshipTo( n2, Relationships.IS_NEIGHBOR);
		rel.setProperty("cost", 1);
		
		//System.out.println("Adding relation: " + n1.getProperties("name") + "->" + n2.getProperties("name"));
	}
	
	public void breadthFirstSearch(int nameOfStartingNode, int maxDepth){
		try ( Transaction tx = this.getDatabase().beginTx() )
		{
			Index<Node> index = this.getDatabase().index().forNodes("dbNodes");
			
			IndexHits<Node> dbNodes = index.get("name", nameOfStartingNode);
			Node startingNode = dbNodes.next();
			
			
			Runtime runtime = Runtime.getRuntime();
			long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
			String output = "";
			long startTime = System.nanoTime();    
			Traverser traverser = prepareBreadthFirstSearch(startingNode, maxDepth);			
			int numOfNodes = 0;
			
			for ( Path path : traverser )
			{
				numOfNodes++;
				output = "At depth " + path.length() + " => " + path.endNode().getProperty( "name" ) + "\n";
				System.out.println(output);
			}
			long estimatedTime = System.nanoTime() - startTime;
			long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
			
			//System.out.println("Done traversing");
			//System.out.println("Time of breadth-first search for node: " + nameOfStartingNode + " is: " + estimatedTime + " ns");
			//System.out.println("Maximum memory used for breadth-first search for node : " + nameOfStartingNode + " is: " + (usedMemoryAfter-usedMemoryBefore) + " B");
			this.timesOfExecution.add(estimatedTime);
			if(usedMemoryAfter-usedMemoryBefore > 0){
				this.memoryUsed.add(usedMemoryAfter-usedMemoryBefore);

			}			
			//System.out.println("Number of nodes reached: " + numOfNodes);
			
			tx.success();
		}
		
		
	}
	
	private Traverser prepareBreadthFirstSearch(Node startingNode, int maxDepth )
	{
	    TraversalDescription td = this.getDatabase().traversalDescription()
	            .breadthFirst()
	            .relationships( Relationships.IS_NEIGHBOR, Direction.BOTH )
	            .evaluator( Evaluators.includingDepths(1, maxDepth));
	    return td.traverse( startingNode );
	}
	
	private PathFinder<WeightedPath> getDijkstraFinder(){
		return GraphAlgoFactory.dijkstra(
				PathExpanders.forTypeAndDirection( Relationships.IS_NEIGHBOR, Direction.BOTH ), "cost" );	
	}
	
	public void calculateDijkstraForTwoNodes(int n1, int n2){
		try ( Transaction tx = this.getDatabase().beginTx() )
		{
			Index<Node> index = this.getDatabase().index().forNodes("dbNodes");
			
			IndexHits<Node> dbNodes = index.get("name", n1);
			Node startingNode = dbNodes.next();
			
			dbNodes = index.get("name", n2);
			Node endingNode = dbNodes.next();
			
			
			Runtime runtime = Runtime.getRuntime();
			long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
			long startTime = System.nanoTime();    

			WeightedPath path = this.getDijkstra().findSinglePath( startingNode, endingNode );
			long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();

			long estimatedTime = System.nanoTime() - startTime;
			
			//System.out.println(estimatedTime);
			//System.out.println(path.weight());
			this.timesOfExecution.add(estimatedTime);
			if(usedMemoryAfter-usedMemoryBefore > 0){
				this.memoryUsed.add(usedMemoryAfter-usedMemoryBefore);

			}			
			tx.success();
		}
	}
    
}








