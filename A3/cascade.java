//University of Victoria
//Samuel Navarrete
//CSC485 A3, Cascade Behaviour of a Network Graph

import java.io.File; 
import java.io.FileNotFoundException; 
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random; 

public class cascade{

	public static Random rand = new Random();
	public static String[] node_names; 

	public static class CascadeData{
		public int x; //budget
		public ArrayList<String> initialAdopters;

		public CascadeData(int x, ArrayList<String> initialAdopters){
			this.x = x; 
			this.initialAdopters = initialAdopters; 
		}

	}

	public static int randInt(int min, int max)
	{
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}


	public static ArrayList<ArrayList<Integer>> combine(int n, int k) {
        ArrayList<ArrayList<Integer>> sol = new ArrayList<ArrayList<Integer>>();
        recursion(n,k,new ArrayList<Integer>(), sol);
        return sol;
    }
     
    private static void recursion(int n, int k, ArrayList<Integer> partial,
        ArrayList<ArrayList<Integer>> sol) {
        if(partial.size() == k && !sol.contains(partial)) {
            Collections.sort(partial);
            sol.add(partial);
        } else if(partial.size() > k) {
            return;
        } else {
            for(int i = n; i >= 1; --i) {
                ArrayList<Integer> partial_sol = new ArrayList<Integer>();
                partial_sol.addAll(partial);
                partial_sol.add(i);
                recursion(i-1, k, partial_sol, sol);
            }
        }
    }

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

			node_names = new String[size+1];
			for(int i = 1; i < size; i++){
				node_names[i-1] = nodelist[i];
			}

			node_names[size] = "Super"; 
			//To store the graph
			graph = new float[size+1][size+1];

			for(int i = 0; i < size; i++)
			{
				line = sc.nextLine(); 
				String[] nodes = line.split(";");

				for(int j = 0; j < size; j++)
				{
					graph[i][j] = Float.parseFloat(nodes[j+1]); 
				}
			}

			//Adding the supernode
			for(int i = 0; i < size; i++){
				graph[size][i] = 1;
				graph[i][size] = 1;
			}
		}

		catch (FileNotFoundException e)
		{
			graph = null; 
			System.out.println("File IO Error: could not read file");
		}

		return graph; 
	}

	//Initializes a list of adopting nodes for the graph 
	public static Boolean[] createAdoptGraph(float[][] graph, ArrayList<Integer> budget_nodes)
	{
		Boolean[] adopt = new Boolean[graph[0].length];

		//Initialize all nodes to not adopting (false)
		for(int i = 0; i < graph[0].length; i++){
			adopt[i] = false; 
		}

		//Use budget on given set of nodes
		for (Integer i : budget_nodes){
			adopt[i] = true; 
		}
		return adopt; 
	}

	//Starts a cascade on graph given the initial budget nodes and a threshold q
	//Returns number of adopting nodes after the cascade halts (no longer changes or is complete) 
	public static int startCascade(float[][] graph, int x, float q, ArrayList<Integer> budget_nodes)
	{
		int size = graph[0].length; 
		int ret_val = 0; 
		double p = 0; 

		Boolean[] adopt; 
		adopt = createAdoptGraph(graph, budget_nodes); 

		boolean graph_changed = true; 
		while(graph_changed) // Keep going until graph stops changing (no more adopting nodes)
		{
			graph_changed = false; 
			for(int i = 0; i < graph[0].length; i++){
				if(adopt[i] == false){
					int neighbour_count = 0; //all neighbours
					int adpt_neighbours = 0; //adopting neighbours
					for(int j = 0; j < graph[0].length; j++){
						if(graph[i][j] == 1 || graph[j][i] == 1){
							neighbour_count++; 
							if(adopt[j] == true){
								adpt_neighbours++;
							}
						}
					}
					p = (double)((double)adpt_neighbours/(double)neighbour_count); 
					if(p >= q){
						adopt[i] = true; 
						graph_changed = true; 
						ret_val++; 
					}
				}
			}
		}
		return ret_val; 
	}

	public static void printAdjMatrix(float[][] graph)
	{
		for(int i = 0; i < graph[0].length; i++){
			for(int j = 0; j < graph[0].length; j++){
				System.out.print((int)graph[i][j]);
			}
			System.out.println(); 
		}
	}

	public static ArrayList<String> getNodeNames(ArrayList<Integer> nodes)
	{
		ArrayList<String> names = new ArrayList<String>(); 
		for(int i : nodes)
		{
			names.add(node_names[i]); 
		}

		return names; 
	}

	public static CascadeData simpleCascadeAlg(float[][] graph, float q)
	{
		System.out.println(); 
		System.out.println("Running SIMPLE Cascade Algorithm: \n   Uses the initial nodes as budget nodes"); 
		System.out.println("-------------------------------------------");
		System.out.println("   Ommitting non-complete cascades");
		System.out.println("   Looking for a complete cascade...");

		for(int i = 0; i <= graph[0].length; i++){
			//use budget on initial nodes
			ArrayList<Integer> budget_nodes = new ArrayList<Integer>(); 
			for(int j = 0; j < i; j++){
				budget_nodes.add(j); 
			}

			//get total number of adopting nodes after cascade halts
			int total_adopt = i + startCascade(graph, i, q, budget_nodes); 
			System.out.print("x = " + String.format("%2d", i) + ", total adopting after: " + total_adopt); 
			if(total_adopt == graph[0].length){
				System.out.println(" [COMPLETE CASCASDE FOUND]"); 
				CascadeData returnCascade = new CascadeData(i, getNodeNames(budget_nodes)); 	
				return returnCascade; 
			}
			System.out.println(); 
		}

		return null; 
	}

	public static CascadeData bruteCascadeAlg(float[][] graph, float q)
	{
		System.out.println(); 
		System.out.println("Running BRUTE FORCE Cascade Algorithm: \n   Uses all possible combinations of initial nodes to find a cascade"); 
		System.out.println("-------------------------------------------");
		System.out.println("   Ommitting non-complete cascades");
		System.out.println("   Looking for a complete cascade..."); 

		for(int i = 0; i <= graph[0].length; i++){
			ArrayList<ArrayList<Integer>> combinations = combine(graph[0].length-1, i);
			for(ArrayList<Integer> budget_nodes : combinations){
				int total_adopt = i + startCascade(graph, i, q, budget_nodes);
				if(total_adopt == graph[0].length){
					System.out.print("x = " + String.format("%2d", i) + ", total adopting after: " + total_adopt); 
					System.out.println(" [COMPLETE CASCADE FOUND]"); 
					CascadeData returnCascade = new CascadeData(i, getNodeNames(budget_nodes)); 
					return returnCascade; 
				}				
			}	
			System.out.println(); 
		}	
		return null; 
	}

	public static CascadeData heuristicCascadeAlg(float[][] graph, float q)
	{
		System.out.println(); 
		System.out.println("Running HEURISTIC Cascade Algorithm: \n   Uses random budget nodes"); 
		System.out.println("-------------------------------------------");
		System.out.println("   Ommitting non-complete cascades");
		System.out.println("   Looking for a complete cascade...");

		int size = graph[0].length; 
		for(int i = 0; i < size; i++){
			//use budget on random nodes
			ArrayList<Integer> budget_nodes = new ArrayList<Integer>(); 
			for(int j = 0; j < i; j++){
				budget_nodes.add(randInt(0, size-1)); 
			}

			//get total number of adopting nodes after cascade halts
			int total_adopt = i + startCascade(graph, i, q, budget_nodes); 
			System.out.print("x = " + String.format("%2d", i) + ", total adopting after: " + total_adopt); 
			if(total_adopt == size){
				System.out.println(" [COMPLETE CASCASDE FOUND]"); 
				CascadeData returnCascade = new CascadeData(i, getNodeNames(budget_nodes)); 
				return returnCascade;
			}
			System.out.println(); 
		}
		return null;
	}

	public static void main(String[] args)
	{
		String filepath;  //Path of file to open
		float[][] graph;  //Adjacency matrix of graph

		//Threshold
		float q = 0.2f; 

		//User Input
		if(args.length < 1){
			Scanner in = new Scanner(System.in);
			System.out.println("Enter path of .csv file: ");
			filepath = in.nextLine(); 
		}
		else{
			filepath = args[0];
		}

		//Generate Adjacency Matrix of graph from file
		if((graph = parseFile(filepath)) == null){
			System.out.println("Error parsing file, aborting.");
			return; 
		} 		

		System.out.println(filepath + " has " + graph[0].length + " nodes, analysing.."); 

		//Testing a Simple Algorithm
		CascadeData simpleCascade = simpleCascadeAlg(graph, q); 
		if(simpleCascade != null){
			System.out.println("Initial Adopters: " + simpleCascade.initialAdopters); 
		}else{System.out.println("---No Complete Cascade Found---");}

		//Testing the heuristic Algorithm
		CascadeData heuristicCascade = heuristicCascadeAlg(graph, q);
		if(heuristicCascade != null){
			System.out.println("Initial Adopters: " + heuristicCascade.initialAdopters); 
		}else{System.out.println("---No Complete Cascade Found---");}

		//Taking the Brute Force Algorithm
		CascadeData bruteCascadeAlg = bruteCascadeAlg(graph, q); 
		if(bruteCascadeAlg != null){
			System.out.println("Initial Adopters: " + heuristicCascade.initialAdopters); 
		}else{System.out.println("---No Complete Cascade Found---");}
	}
}