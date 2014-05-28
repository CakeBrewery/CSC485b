
import java.io.File; 
import java.io.FileNotFoundException; 
import java.util.*;


public class diameter{

	//Parses a file with a graph in .csv format and generates an adjacency list
	public static float[][] parseFile(String filepath)
	{
		File file = new File(filepath);
		float[][] graph; 
		int size = 0; 

		try{
			Scanner sc = new Scanner(file); 
			String line = sc.nextLine(); 

			//First line of the CSV files generated by Gephi
			//contain a list of all the nodes
			String[] nodelist = line.split(";");
			size = nodelist.length-1; //graph size = # of nodes

			//To store the graph
			graph = new float[size][size];

			for(int i = 0; i < size; i++)
			{
				line = sc.nextLine(); 
				String[] nodes = line.split(";");

				for(int j = 0; j < size; j++)
				{
					graph[i][j] = Float.parseFloat(nodes[j+1]); 
				}
			}
		}

		catch (FileNotFoundException e)
		{
			graph = null; 
			System.out.println("File IO Error");
		}

		return graph; 
	}

	//Runs the Floyd Warshall algorithm on the given graph 
	//Parameters: graph - an adjacency matrix with the graph
	public static void floydWarshall(float[][] graph){
		int size = graph[0].length; //Get size of graph

		//Initialize all zeroes as infinite distances
		for(int i = 0; i < size; i ++){
			for(int j = 0; j < size; j++){
				if(graph[i][j] == 0)
					graph[i][j] = Float.POSITIVE_INFINITY;

			}
		}

		for(int k = 0; k < size; k++){
			for(int i = 0; i < size; i++){
				for(int j = 0; j < size; j++){
					graph[i][j] = Math.min(graph[i][j], graph[i][k] + graph[k][j]);
				}
			}
		}
	}

	//Gets the diameter of a graph given the adjacency list of
	//the graph and its size
	//Parameters:	graph - an adjacency matrix with the graph
	public static int getDiameter(float[][] graph)
	{

		int size = graph[0].length;  //Get size of graph
		float diameter = 0; 

		//Make a copy of graph to apply floyd warshall to
		float[][] graph_copy = graph.clone(); 

		//Apply Floyd Warshall algorithm to copy of graph
		floydWarshall(graph_copy); 

		//Look for longest of all the shortest paths
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(graph[i][j] != Float.POSITIVE_INFINITY){
					if (diameter < graph[i][j])
						diameter = graph[i][j];
				}
			}
		}

		return (int)diameter;
	}

	//Counts the number of isolated nodes in the graph
	//Parameters:	graph - an adjacency matrix with the graph
	public static int checkIsolated(float[][] graph)
	{

		int size = graph[0].length;  //Get size of graph
		int count = 0; 

		//Iterate through each node and see if they have neighbours
		for(int i = 0; i < size; i++){
			Boolean has_beighbour = false; 
			for(int j = 0; j < size; j++){
				if(graph[i][j] == 1){
					//Found a neighbour
					has_beighbour = true; 
				}
			}

			// If the node has no neightbours
			//count it as isolated
			if (!has_beighbour) 
				count++; 
		}

		return count; 
	}

	public static void main(String[] args)
	{
		//Declarations
		String filepath = "./graph.csv";
		float[][] graph;

		//Generate Adjacency Matrix of graph from file
		graph = parseFile(filepath); 
		
		//get diameter
		int diameter = getDiameter(graph); 
		int isolated = checkIsolated(graph); 	

		//Print information 
		System.out.println("Diameter of " + "\"" + filepath + "\"" + " is: " + diameter); 
		System.out.println("Number of Isolated Nodes: " + isolated); 
	}
}