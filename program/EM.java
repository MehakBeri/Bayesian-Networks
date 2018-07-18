import java.io.*;
import java.util.*;

public class EM{
    

    //initializes k random graphs having #vertices = parameters
    public static ArrayList<Graph> init_graph(int k, int parameters){
        Random rand = new Random();
        ArrayList<Graph> graphs = new ArrayList<Graph>();
        for( int i=0; i<k;i++)
        {
             ArrayList<Edge> ar = new ArrayList<Edge>();
             int v1 = rand.nextInt(parameters); //random start vertex
             Set<Integer> s = new HashSet<Integer>(); //empty set for holding the vertices to which edges exist
             while(s.size()<parameters){ 
                 int v2 = rand.nextInt(parameters); //random vertex 2
                 int w= rand.nextInt(50)*(-1); //random weight within range 0 to 50
                 Edge e = new Edge(v1,v2,w);
                 s.add(v2);
                 v1=v2; //next edge starts from the edge to which this edge was destined 
                 ar.add(e);
             }           
             //run min-st and dfs to get a simple min weight spanning tree
             ArrayList<Edge> mstEdges=maxST_dfs.kruskalMST(ar, parameters);
             maxST_dfs.DFS(0,parameters,mstEdges);
             ArrayList<Edge> directedG= maxST_dfs.orderedG(mstEdges);
             Graph g= new Graph(directedG);
//             System.out.println(directedG);
             graphs.add(g);
        }
        return graphs;
    }
    
    public static double[] likelihood(List<int[]> data,HashMap<Edge, Double[]> cpt, Double[] zero){
        double[] b= new double[data.size()];
        int k=0; //iterator for number of rows in the data
        for(int[] row: data){
            double theta=1;
            if(row[0]==0){
                theta= theta*(zero[0]);
            }else{
                theta= theta*(zero[1]);
            }
            for(Edge e: cpt.keySet()){
                int v1= e.getVertex1();
                int v2= e.getVertex2();
                Double[] p= cpt.get(e);
                if(row[v2]==0 && row[v1]==0){
                    theta = theta*(p[0]);
                }
                if(row[v2]==0 && row[v1]==1){
                    theta = theta*(p[1]);
                }
                if(row[v2]==1 && row[v1]==0){
                    theta = theta*(p[2]);
                }
                else{
                    theta = theta*(p[3]);
                }
                
            }
            b[k]=theta;
            k++;
        }
        return b;
    }
    

    public static HashMap<Edge, Double[]> randomCPT(ArrayList<Edge> dfs){
        HashMap<Edge, Double[]> cpt = new HashMap<Edge, Double[]>();
        Random r = new Random();
        for(Edge e: dfs){
            int v1= e.getVertex1();
            int v2= e.getVertex2();    
            Double[] prob= new Double[4];
            //find conditional prob of v2 given v1 // conditional parameter p(a|b)=p(a,b)/p(b) 
            prob[0]=r.nextDouble();
            prob[1]=r.nextDouble();
            prob[2]=1-prob[0];
            prob[3]=1-prob[1];
            cpt.put(e,prob);
        }
        
        return cpt;
    }
    
    public static ArrayList<double[]> normalizeWeights(ArrayList<double[]> in){
        ArrayList<double[]> out = new ArrayList<double[]>();
        int rows = in.get(0).length;
        double[] row_sums= new double[rows];
        for(int r=0; r<rows;r++){
            double row_sum = 0;
            for(int i=0; i<in.size();i++){
                row_sum= row_sum+in.get(i)[r];
            }
            row_sums[r]=row_sum;
        }
        for(int j=0; j<in.size();j++){
            double[] dataWt= new double[rows];
            for(int k=0; k<rows; k++){
                dataWt[k]= in.get(j)[k]/row_sums[k];
            }
            out.add(dataWt);
        }
        return out;
    }
    
    public static double[] normalizeP(int k, int n, ArrayList<double[]> graph_H){
        double[] out= new double[k];
        double[] out1= new double[k];
        int g_no=0;
        double sum=0;
        for(double[] g: graph_H){
            sum=0;
            for(int i=0; i<g.length; i++){
                sum=sum+graph_H.get(g_no)[i];
            }
            out[g_no]= sum/n;
            g_no++;
        }
        sum=0;
        for(double o: out){
            sum=sum+o;
        }
        for(int j=0;j<out.length;j++){
            out1[j]=out[j]/sum;
        }
        return out;
    }
    
    public static Double[] thetaEM(List<int[]> data, int v,double[] graph_H){
        int NoParameters= data.get(0).length;
        double noRows=data.size();
        Double[] prob= new Double[NoParameters];
        for(int i=0; i<NoParameters; i++){
            double count=0;
            double sum=0;
            for(int j=0; j<data.size();j++){
                if(data.get(j)[i]==v){
                    count=count+graph_H[j];
                }
                sum=sum+graph_H[j];
            }
            Double p= (count+1)/(sum+2) ;
            prob[i]= p;
        }
//        System.out.println("Probability of "+v);
//        for(int j=0; j<NoParameters;j++){
//            System.out.println(" "+prob[j]);
//        }
        return prob;
    }
    
    public static double getJP(List<int[]> data,Integer i1,Integer i2,int v1, int v2,double[] graph_H){
        
        double sum=0;       
        double count=0;
        for(int j=0; j<data.size();j++){
            if(data.get(j)[i1]==v1 && data.get(j)[i2]==v2){
                count=count+graph_H[j];
            }
            sum=sum+graph_H[j];
        }
        double p= (count+1)/(sum+4) ;
        return p;        
    }
    
    public static HashMap<String, Double> jointDistEM(List<int[]> data,String[] parameters,double[] graph){
        HashMap<String, Double> hmap = new HashMap<String, Double>();
        String key="";
        Double prob;
        for (String p1: parameters){
            //index of p1
            Integer index_p1=chowliu.getIndex(p1);
            for(String p2: parameters){
                if(p2!=p1){
                    Integer index_p2=chowliu.getIndex(p2);
                    //p1=0; p2=0
                    key=p1+"."+p2+".00";
                    prob= getJP(data,index_p1,index_p2,0,0,graph);
                    hmap.put(key,prob);
                    //p1=0, p2=1
                    key=p1+"."+p2+".01";
                    prob= getJP(data,index_p1,index_p2,0,1,graph);
                    hmap.put(key,prob);
                    //p1=1, p2=0
                    key=p1+"."+p2+".10";
                    prob= getJP(data,index_p1,index_p2,1,0,graph);
                    hmap.put(key,prob);
                    //p1=1, p2=1
                    key=p1+"."+p2+".11";
                    prob= getJP(data,index_p1,index_p2,1,1,graph);
                    hmap.put(key,prob);
                }
            }
        }
//        for(String key1: hmap.keySet()){
//            System.out.println(key1+" : "+hmap.get(key1));
//        }
        return hmap;
    }
    
    public static ArrayList<HashMap<String, Double>> mutualInfo(List<int[]> data,ArrayList<double[]> graph_H){
        
        ArrayList<HashMap<String, Double>> res= new ArrayList<HashMap<String, Double>>();
        
        for(double[] graph: graph_H){        
            
            Double[] prob_0= thetaEM(data,0,graph);
            Double[] prob_1 = new Double[prob_0.length];
            for(int j=0; j<prob_0.length; j++){
                prob_1[j]= 1-prob_0[j];
            }   
            String[] parameters= new String[prob_0.length];
            HashMap<String, Double> prob0= new HashMap<String, Double>();
            HashMap<String, Double> prob1= new HashMap<String, Double>();
            for(int i=0; i<prob_0.length; i++){
                parameters[i]="Node"+"_"+Integer.toString(i);
                prob0.put(parameters[i],prob_0[i]);
                prob1.put(parameters[i],prob_1[i]);
            } 
            
            HashMap<String, Double> jd = jointDistEM(data,parameters,graph);
            HashMap<String, Double> mi = chowliu.mutualInformation(jd, parameters, prob0, prob1);
            res.add(mi);
        }
        return res;
    }
    
    public static boolean converged(ArrayList<double[]> oldg,ArrayList<double[]> newg){
        boolean res=true;
        int rows = oldg.get(0).length;
        int k= oldg.size();
        ArrayList<Double> avg_old=new ArrayList<Double>();
        ArrayList<Double> avg_new=new ArrayList<Double>();
        //average cluster probabilities for each graph
        for(int j=0; j<k;j++)
        {
            //for each graph
            double sum_old=0;
            double sum_new=0;
            for(int i=0; i<rows;i++)
            {
                sum_old = sum_old + oldg.get(j)[i];
                sum_new = sum_new + newg.get(j)[i];
            }
            avg_old.add(sum_old/rows);
            avg_new.add(sum_new/rows);
        }
        //check difference between corresponding avg values for each graph
        for(int a=0; a<avg_old.size(); a++){
            if(Math.abs(avg_old.get(a)-avg_new.get(a)) > 0.001){
                res=false;
            } 
        }
        return res;
    }
    
    public static double[] addMatrices(double[] a, double[] b){
        double[] res=new double[a.length];
        for(int i=0; i<a.length; i++){
            res[i] = a[i]+b[i];
        }
        return res;
    }
    
    public static void em_start(String train, String test, int k){
        System.out.println("================================");
        System.out.println("Considering data: "+train);
        List<int[]> data=chowliu.readFile(train);
        int param = data.get(0).length;
        //initialize k random graphs
        System.out.println("Initializing "+k+" random graphs");
        ArrayList<Graph> graphs = init_graph(k,param);
        //initialize p values to be same(random)
        double[] p= new double[k]; 
        for(int i=0; i<k; i++){
            p[i]=(1/(double)(k));
        }
        System.out.println();
        //initialize cpt values for the trees(random)
        Random r = new Random();
        ArrayList<double[]> graph_H = new ArrayList<double[]>();
        int graph_no=0;
        for(Graph g: graphs){
            //this means that the matrices graph_h are taken vertically, so one double[] for each graph is of length=no of rows in data
            Double[] zero= new Double[2];
            zero[0]=r.nextDouble();
            zero[1]=1-zero[0];
            HashMap<Edge, Double[]> cpt = randomCPT(g.getGraph());
            //calculate likelihood for this graph
            double[] ll= likelihood(data,cpt,zero);
            //calculate weights H fr  this graph
            double[] H= new double[ll.length];
            for(int iter=0; iter<ll.length; iter++){
                H[iter]=p[graph_no]*ll[iter];
            }
            graph_H.add(H);           
            graph_no++;
        }
        //normalize the weights H 
        graph_H = normalizeWeights(graph_H);
//        System.out.println("first e-step using random trees and random cpts completed");
        // first m-step
        int n= data.size(); //number of rows in data
        p=normalizeP(k,n,graph_H);
        //calculate mutual infos for each graph
        ArrayList<HashMap<String, Double>> mi = mutualInfo(data,graph_H);
        //make trees out of mi's calculated
        ArrayList<Graph> trees = new ArrayList<Graph>();
        String[] parameters= new String[param];
        for(int i=0; i<param; i++){
                parameters[i]="Node"+"_"+Integer.toString(i);
            }
        for(HashMap<String, Double> m: mi){
            ArrayList<Edge> tree = maxST_dfs.mst(m,parameters);
            Graph g= new Graph(tree);
            trees.add(g);
        }
//        System.out.println("first m-step complete. created trees based on MI calculated from e-step");
        //second loop
        ArrayList<double[]> graph_H_1 = new ArrayList<double[]>();
        graph_no=0;
        HashMap<String, Double> jd_1 = chowliu.jointDistribution(data,parameters);
        Double[] zero_1= new Double[2];
        Double[] prob_0= chowliu.theta(data,0);
        Double[] prob_1= new Double[prob_0.length];
        for(int q=0; q<prob_0.length;q++){
            prob_1[q]=1-prob_0[q];
        }
        zero_1[0]=prob_0[0];
        zero_1[1]=1-zero_1[0];
        for(Graph g: trees){
            HashMap<Edge, Double[]> cpt_1 = chowliu.conditionalProbTable(g.getGraph(),jd_1,prob_0,prob_1);
            double[] ll_1= likelihood(data,cpt_1,zero_1);
            double[] H_1= new double[ll_1.length];
            for(int it=0; it<ll_1.length; it++){
                H_1[it]=p[graph_no]*ll_1[it];
            }
            graph_H_1.add(H_1);           
            graph_no++;
        }
        //normalize the weights H 
        graph_H_1 = normalizeWeights(graph_H_1);
        int c=0;
        ArrayList<HashMap<Edge, Double[]>> graph_cpts=new ArrayList<HashMap<Edge, Double[]>>();;
        while(!converged(graph_H,graph_H_1)){
            
            //since not converged, make a tree again
            p=normalizeP(k,n,graph_H_1);
            //calculate mi and cpt again
            mi = mutualInfo(data,graph_H_1);
            //make trees out of mi's calculated
            ArrayList<Graph> trees_1 = new ArrayList<Graph>();
            for(HashMap<String, Double> m: mi){
                ArrayList<Edge> tree = maxST_dfs.mst(m,parameters);
                Graph g= new Graph(tree);
                trees_1.add(g);
            }
            //find this graph's H vals
            ArrayList<double[]> this_h = new ArrayList<double[]>();
            graph_no=0;
            HashMap<Edge, Double[]> cpt_1= new HashMap<Edge, Double[]>();
            graph_cpts = new ArrayList<HashMap<Edge, Double[]>>();
            for(Graph g: trees_1){
                cpt_1 = chowliu.conditionalProbTable(g.getGraph(),jd_1,prob_0,prob_1);                
                double[] ll_1= likelihood(data, cpt_1, zero_1);
                double[] H_2= new double[ll_1.length];
                for(int t=0; t<ll_1.length; t++){
                    H_2[t]=p[graph_no]*ll_1[t];
                }
                this_h.add(H_2); 
                graph_cpts.add(cpt_1);
                graph_no++;
            }
            //normalize the weights H 
            this_h = normalizeWeights(this_h);            
            graph_H=graph_H_1;
            graph_H_1= this_h;//this graph's H values
            c++;
            if(c>99)
            {
                break;
            }
        }
        System.out.println("Converged after "+c+" loops");
        
        //test model on test or validation data ..
        List<int[]> test_file=chowliu.readFile(test);
        double[] sum_H=new double[test_file.size()];
        // calculate likelihood values for test set, then sum p1cpt1+p2cpt2 etc. 
        for(int i=0; i<k ;i++){
            double[] l = likelihood(test_file,graph_cpts.get(i),zero_1);
            double[] prod = new double[l.length];
            
            for(int val=0; val<l.length; val++){
                prod[val]=p[i]*l[val];
            }
            sum_H = addMatrices(sum_H,prod) ;
        }
        //sum over those values n store in sum_H
        double loglikelihood_H=0;
        for(int val=0; val<test_file.size(); val++){
            loglikelihood_H = loglikelihood_H + Math.log10(sum_H[val]); 
        }
        
        System.out.println("\n\nSum of all log likelihoods for all rows: "+loglikelihood_H);
        System.out.println("Average log likelihood (log base 10) for this data: " + (loglikelihood_H/n));
        System.out.println();
        System.out.println("___________________________________________________");
    }
        
    public static void main(String[] args){
        
        int[] k_arr = {3,5,10,15,20};
        for(int i=0; i<k_arr.length; i++){
//        for(int i=0; i<10; i++){
//            Scanner s = new Scanner(System.in);
//            
            int k= k_arr[i];
            System.out.println("Value of k: "+k);
       int k=5;

            em_start("../hw4-datasets/small-10-datasets/accidents.ts.data","../hw4-datasets/small-10-datasets/accidents.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/baudio.ts.data","../hw4-datasets/small-10-datasets/baudio.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/bnetflix.ts.data","../hw4-datasets/small-10-datasets/bnetflix.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/dna.ts.data","../hw4-datasets/small-10-datasets/dna.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/jester.ts.data","../hw4-datasets/small-10-datasets/jester.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/kdd.ts.data","../hw4-datasets/small-10-datasets/kdd.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/msnbc.ts.data","../hw4-datasets/small-10-datasets/msnbc.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/plants.ts.data","../hw4-datasets/small-10-datasets/plants.test.data",k);
            em_start("../hw4-datasets/small-10-datasets/nltcs.ts.data","../hw4-datasets/small-10-datasets/nltcs.test.data",k);

        }
        
    }

}

class Graph{
    private ArrayList<Edge> edges;
    //default starting node is 0
    public Graph(ArrayList<Edge> edges){
        this.edges=edges;
    }
    public ArrayList<Edge> getGraph(){
        return edges;
    }
}