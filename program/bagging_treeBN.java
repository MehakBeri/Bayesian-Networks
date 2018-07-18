import java.io.*;
import java.util.*;

public class bagging_treeBN{
    
    public static ArrayList<List<int[]>> bootstrap_data(List<int[]> data, int k){
        ArrayList<List<int[]>> b = new ArrayList<List<int[]>>();
        Random r= new Random();
        for(int i=0; i<k; i++){
            int n=r.nextInt(data.size()-1)+1; //number of samples
            Set<Integer> s = new HashSet<Integer>();
            List<int[]> dat= new ArrayList<int[]>();
            while(s.size()<n){
                s.add(r.nextInt(data.size()));
            }
            for(int sample: s){
                dat.add(data.get(sample));
            }
            b.add(dat);
        }
        return b;
    }

    public static void bag_tree(String train, String test, int k){
        System.out.println("================================");
        System.out.println("Considering data: "+train);
        List<int[]> data=chowliu.readFile(train);
        int param = data.get(0).length;
        int n= data.size();
        Double[] zero= new Double[2];
        //get k bootstrap samples
        ArrayList<List<int[]>> sampled = bootstrap_data(data,k); 
        //for each sample calculate chow liu tree and corresponding log likelihood  on test
        ArrayList<double[]> ll_graphs= new ArrayList<double[]>();
        double p_val= 1/(double)k;
        ArrayList<HashMap<Edge, Double[]>> graph_cpt = new ArrayList<HashMap<Edge, Double[]>>();
        for(int i=0; i<k; i++)
        {
            List<int[]> sample= sampled.get(i);
            double rows= sample.size();
            Double[] prob_0= chowliu.theta(sample,0); 
            Double[] prob_1= chowliu.theta(sample,1);
            String[] parameters= new String[prob_0.length];
            HashMap<String, Double> prob0= new HashMap<String, Double>();
            HashMap<String, Double> prob1= new HashMap<String, Double>();
            for(int ii=0; ii<prob_0.length; ii++){
                parameters[ii]="Node"+"_"+Integer.toString(ii);
                prob0.put(parameters[ii],prob_0[ii]);
                prob1.put(parameters[ii],prob_1[ii]);
            } 
            HashMap<String, Double> jd = chowliu.jointDistribution(sample,parameters);
            HashMap<String, Double> mi = chowliu.mutualInformation(jd, parameters, prob0, prob1);
    //        System.out.println("Mutual information calculated between parameters");
            ArrayList<Edge> dfs = maxST_dfs.mst(mi,parameters);
            zero= new Double[2];
            zero[0]=prob_0[0];
            zero[1]=prob_1[0];

            //calculate cpt for the dfs 
            HashMap<Edge, Double[]> cpt = chowliu.conditionalProbTable(dfs,jd,prob_0,prob_1);
            double[] ll_1= EM.likelihood(sample, cpt, zero);
            graph_cpt.add(cpt);
        }
        
        
        //average log likelihood for all samples
        List<int[]> test_file=chowliu.readFile(test);
        double[] sum_H=new double[test_file.size()];
        // calculate likelihood values for test set, then sum p1cpt1+p2cpt2 etc. 
        for(int i=0; i<k ;i++){
            double[] l = EM.likelihood(test_file,graph_cpt.get(i),zero);
            double[] prod = new double[l.length];
            
            for(int val=0; val<l.length; val++){
                prod[val]=(1/(double)k)*l[val];
            }
            sum_H = EM.addMatrices(sum_H,prod) ;
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
        for(int i=0; i<10; i++){
//            Scanner s = new Scanner(System.in);
//            int k=20;
            int k= k_arr[i];
            System.out.println("Value of k: "+k);
    
           bag_tree("../hw4-datasets/small-10-datasets/accidents.ts.data","../hw4-datasets/small-10-datasets/accidents.test.data",k);
           bag_tree("../hw4-datasets/small-10-datasets/baudio.ts.data","../hw4-datasets/small-10-datasets/baudio.test.data",k);
            bag_tree("../hw4-datasets/small-10-datasets/bnetflix.ts.data","../hw4-datasets/small-10-datasets/bnetflix.test.data",k);
           bag_tree("../hw4-datasets/small-10-datasets/dna.ts.data","../hw4-datasets/small-10-datasets/dna.test.data",k);
            bag_tree("../hw4-datasets/small-10-datasets/jester.ts.data","../hw4-datasets/small-10-datasets/jester.test.data",k);
            bag_tree("../hw4-datasets/small-10-datasets/kdd.ts.data","../hw4-datasets/small-10-datasets/kdd.test.data",k);
            bag_tree("../hw4-datasets/small-10-datasets/msnbc.ts.data","../hw4-datasets/small-10-datasets/msnbc.test.data",k);
           bag_tree("../hw4-datasets/small-10-datasets/plants.ts.data","../hw4-datasets/small-10-datasets/plants.test.data",k);
            bag_tree("../hw4-datasets/small-10-datasets/nltcs.ts.data","../hw4-datasets/small-10-datasets/nltcs.test.data",k);
        }
    }

}
