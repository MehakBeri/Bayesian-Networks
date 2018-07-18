import java.io.*;
import java.util.*;

public class maxST_dfs{

    public static ArrayList<Integer> order;
    
    //finds maximum spanning tree by negating all edges and then applying kruskal's
    public static ArrayList<Edge> mst(HashMap<String,Double> mi, String[] parameters){
        
        int nodeCount = parameters.length;		//how many nodes
        
        ArrayList<Edge> graphEdges = new ArrayList<Edge>();	
        for (String p: mi.keySet()) {
            String[] k = p.split("\\.");
            String[] a= k[0].split("_");
            String[] b= k[1].split("_");
            int src= Integer.valueOf(a[1]);
            int dest= Integer.valueOf(b[1]);
            double w= mi.get(p)*(-1);
            graphEdges.add(new Edge(src, dest, w));
        }
//        System.out.println("Graph with mutual information as edges created");
       	ArrayList<Edge> mstEdges=kruskalMST(graphEdges, nodeCount);
//        System.out.println();
//        System.out.println("Giving directions to MST by choosing Node_0 as head for DFS");
        
        DFS(0,nodeCount,mstEdges);
        //stored dfs ordering in the array list called 'order', if a node number appears before another then a edge goes from that node to the dest
        ArrayList<Edge> directedG= orderedG(mstEdges);
//        System.out.println();
//        System.out.println(directedG);
        return directedG;
    }
    
    public static ArrayList<Edge> orderedG(ArrayList<Edge> mstE){
        ArrayList<Edge> d = new ArrayList<Edge>();
        Edge a;
        for(Edge e: mstE){
            int v1 = e.getVertex1();
            int v2 = e.getVertex2();
            if(order.indexOf(v1)< order.indexOf(v2))
            {
                a = new Edge(v1,v2,e.getWeight());
            }
            else{
                a = new Edge(v2,v1,e.getWeight());
            }
            d.add(a);
        }
        return d;
    }
    
        // A function used by DFS
    public static void DFSUtil(int v,boolean visited[], ArrayList<Edge> mstEdges)
    {
        // Mark the current node as visited and print it
        visited[v] = true;
//        System.out.print(v+" ");
        order.add(v);
        // Recur for all the vertices adjacent to this vertex
        for(Edge edge:mstEdges ){
            if(edge.getVertex1() == v)
            {
                int n = edge.getVertex2();
                if (!visited[n])
//                    System.out.println("from "+v+" to "+n);
                    DFSUtil(n, visited, mstEdges);
            }
            else if(edge.getVertex2() == v){
                int n = edge.getVertex1();
                if (!visited[n])
//                    System.out.println("from "+v+" to "+n);
                    DFSUtil(n, visited, mstEdges);
            }
        }
    }
 
    // The function to do DFS traversal. It uses recursive DFSUtil()
    public static void DFS(int v, int n, ArrayList<Edge> mstEdges)
    {
        // Mark all the vertices as not visited(set as
        // false by default in java)
        boolean visited[] = new boolean[n];
        // Call the recursive helper function to print DFS traversal
        order = new ArrayList<Integer>();
        DFSUtil(v, visited, mstEdges);
    }
    
    public static ArrayList<Edge> kruskalMST(ArrayList<Edge> graphEdges, int nodeCount){
		String outputMessage="";

		Collections.sort(graphEdges);		//sort edges with smallest weight 1st
		ArrayList<Edge> mstEdges = new ArrayList<Edge>();	//list of edges included in the Minimum spanning tree (initially empty)

		DisjointSet nodeSet = new DisjointSet(nodeCount+1);		//Initialize singleton sets for each node in graph. (nodeCount +1) to account for arrays indexing from 0

		for(int i=0; i<graphEdges.size() && mstEdges.size()<(nodeCount-1); i++){		//loop over all edges. Start @ 1 (ignore 0th as placeholder). Also early termination when number of edges=(number of nodes-1)
			Edge currentEdge = graphEdges.get(i);
			int root1 = nodeSet.find(currentEdge.getVertex1());		//Find root of 1 vertex of the edge
			int root2 = nodeSet.find(currentEdge.getVertex2());
//			outputMessage+="find("+currentEdge.getVertex1()+") returns "+root1+", find("+currentEdge.getVertex2()+") returns "+root2;		//just print, keep on same line for union message
			String unionMessage=",\tNo union performed\n";		//assume no union is to be performed, changed later if a union DOES happen
			if(root1 != root2){			//if roots are in different sets the DO NOT create a cycle
				mstEdges.add(currentEdge);		//add the edge to the graph
				nodeSet.union(root1, root2);	//union the sets
				unionMessage=",\tUnion("+root1+", "+root2+") done\n";		//change what's printed if a union IS performed
			}
//			outputMessage+=unionMessage;
		}

		outputMessage+="\nFinal Spanning Tree ("+mstEdges.size()+" edges)\n";
		double mstTotalEdgeWeight=0;
		for(Edge edge: mstEdges){
			outputMessage+=edge +"\n";		//print each edge
			mstTotalEdgeWeight += edge.getWeight();
		}
//		outputMessage+="\nTotal weight of all edges in MST = "+ (mstTotalEdgeWeight*(-1));
//		System.out.println(outputMessage);
//        System.out.println("\nTotal weight of all edges in MST = "+ (mstTotalEdgeWeight*(-1)));
        return mstEdges;
//        System.out.println("\nTotal weight of all edges in MST= Likelihood = "+ (mstTotalEdgeWeight*(-1)));

//		try (PrintWriter outputFile = new PrintWriter( new File("06outputMST.txt") ); ){
//			outputFile.println(outputMessage);
//			System.out.println("\nOpen \"06outputMST.txt\" for backup copy of answers");
//		} catch (FileNotFoundException e) {
//			System.out.println("Error! Couldn't create file");
//		}
	}
}


class Edge implements Comparable<Edge>{
	private int vertex1;	//an edge has 2 vertices & a weight
	private int vertex2;
	private double weight;

	public Edge(int vertex1, int vertex2, double weight){
		this.vertex1=vertex1;
		this.vertex2=vertex2;
		this.weight=weight;
	}

	public int getVertex1(){
		return vertex1;
	}

	public int getVertex2(){
		return vertex2;
	}

	public double getWeight(){
		return weight;
	}

	@Override
	public int compareTo(Edge otherEdge) {				//Compare based on edge weight (for sorting)
        if(this.getWeight()<otherEdge.getWeight())
          return -1;
        else if(otherEdge.getWeight()<this.getWeight())
          return 1;
        return 0;
	}

	@Override
	public String toString() {
		return "Edge ("+getVertex1()+", "+getVertex2()+") weight="+getWeight();
	}
}


// DisjointSet class
//
// CONSTRUCTION: with int representing initial number of sets
//
// ******************PUBLIC OPERATIONS*********************
// void union(root1, root2) --> Merge two sets
// int find(x)              --> Return set containing x
// ******************ERRORS********************************
// No error checking is performed
// http://users.cis.fiu.edu/~weiss/dsaajava3/code/DisjSets.java

/**
 * Disjoint set class, using union by rank and path compression
 * Elements in the set are numbered starting at 0
 * @author Mark Allen Weiss
 */
class DisjointSet{
	private int[] set;		//the disjoint set as an array

	public int[] getSet(){		//mostly debugging method to print array
		return set;
	}

	/**
	 * Construct the disjoint sets object.
	 * @param numElements the initial number of disjoint sets.
	 */
	public DisjointSet(int numElements) {		//constructor creates singleton sets
		set = new int [numElements];
		for(int i = 0; i < set.length; i++){		//initialize to -1 so the trees have nothing in them
			set[i] = -1;
		}
	}

	/**
	 * Union two disjoint sets using the height heuristic.
	 * For simplicity, we assume root1 and root2 are distinct
	 * and represent set names.
	 * @param root1 the root of set 1.
	 * @param root2 the root of set 2.
	 */
	public void union(int root1, int root2) {
		if(set[root2] < set[root1]){		// root2 is deeper
			set[root1] = root2;		// Make root2 new root
		}
		else {
			if(set[root1] == set[root2]){
				set[root1]--;			// Update height if same
			}
			set[root2] = root1;		// Make root1 new root
		}
	}

	/**
	 * Perform a find with path compression.
	 * Error checks omitted again for simplicity.
	 * @param x the element being searched for.
	 * @return the set containing x.
	 */
	public int find(int x) {
		if(set[x] < 0){		//If tree is a root, return its index
			return x;
		}
		int next = x;		
		while(set[next] > 0){		//Loop until we find a root
			next=set[next];
		}
		return next;
	}
	
}


