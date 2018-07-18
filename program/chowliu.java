import java.io.*;
import java.util.*;

public class chowliu{
    
    //reads input files and stores in list of arrays
    public static List<int[]> readFile(String fileName){
        String line = null;
        List<int[]> dataSet = new ArrayList<int[]>();
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] cols = line.split(",");
                int[] rows = new int[cols.length];
                for (int i = 0; i < cols.length; i++){
                    if (!cols[i].equals("?")){
                        rows[i] = Integer.parseInt(cols[i]);
                    }
                    else {
                        rows[i] = 0;
                    }
                    
                }
                dataSet.add(rows);                
            }   

            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");    
        }
        return dataSet;
    }
    
    //calculates probability of occurence of integer v
    public static Double[] theta(List<int[]> data,int v){
        //probability with one -laplace smoothing
        int NoParameters= data.get(0).length;
        double noRows=data.size();
        Double[] prob= new Double[NoParameters];
        for(int i=0; i<NoParameters; i++){
            double count=0;
            for(int[] row: data){
                if(row[i]==v){
                    count++;
                }
            }
            Double p= (count+1)/(noRows+2) ;
            prob[i]= p;
        }
//        System.out.println("Probability of "+v);
//        for(int j=0; j<NoParameters;j++){
//            System.out.println(" "+prob[j]);
//        }
        return prob;
    }
    
    public static Integer getIndex(String p1){
        String[] p1_a = p1.split("_");
        Integer index_p1 = Integer.valueOf(p1_a[1]);
        return index_p1;
    }
    
    public static double getJProb(List<int[]> data,Integer i1,Integer i2,int v1, int v2){
        
        double noRows=data.size();       
        double count=0;
        for(int[] row: data){
            if(row[i1]==v1 && row[i2]==v2){
                count++;
            }
        }
        double p= (count+1)/(noRows+4) ;
        return p;        
    }
    
    //function to calculate joint distribution between two parameters 
    public static HashMap<String, Double> jointDistribution(List<int[]> data, String[] parameters){
        HashMap<String, Double> hmap = new HashMap<String, Double>();
        String key="";
        Double prob;
        for (String p1: parameters){
            //index of p1
            Integer index_p1=getIndex(p1);
            for(String p2: parameters){
                if(p2!=p1){
                    Integer index_p2=getIndex(p2);
                    //p1=0; p2=0
                    key=p1+"."+p2+".00";
                    prob= getJProb(data,index_p1,index_p2,0,0);
                    hmap.put(key,prob);
                    //p1=0, p2=1
                    key=p1+"."+p2+".01";
                    prob= getJProb(data,index_p1,index_p2,0,1);
                    hmap.put(key,prob);
                    //p1=1, p2=0
                    key=p1+"."+p2+".10";
                    prob= getJProb(data,index_p1,index_p2,1,0);
                    hmap.put(key,prob);
                    //p1=1, p2=1
                    key=p1+"."+p2+".11";
                    prob= getJProb(data,index_p1,index_p2,1,1);
                    hmap.put(key,prob);
                }
            }
        }
//        for(String key1: hmap.keySet()){
//            System.out.println(key1+" : "+hmap.get(key1));
//        }
        return hmap;
    }
    
    //calculates mutual information between two parameters of the data; it is 1-laplace smoothed
    public static HashMap<String, Double> mutualInformation(HashMap<String, Double> jDist, String[] parameters,HashMap<String, Double> prob0,HashMap<String, Double> prob1){
        HashMap<String, Double> mi = new HashMap<String, Double>();
        String key="";
        Double val;
        Double val00;
        Double val10;
        Double val01;
        Double val11;
        for (String p1: parameters){
            for(String p2: parameters){
                if(p2!=p1){
                    val00= jDist.get(p1+"."+p2+".00")*Math.log10((jDist.get(p1+"."+p2+".00"))/(prob0.get(p1)*prob0.get(p2)));
                    val01= jDist.get(p1+"."+p2+".01")*Math.log10((jDist.get(p1+"."+p2+".01"))/(prob0.get(p1)*prob1.get(p2)));
                    val10= jDist.get(p1+"."+p2+".10")*Math.log10((jDist.get(p1+"."+p2+".10"))/(prob1.get(p1)*prob0.get(p2)));
                    val11= jDist.get(p1+"."+p2+".11")*Math.log10((jDist.get(p1+"."+p2+".11"))/(prob1.get(p1)*prob1.get(p2)));
                    val= val00+val01+val10+val11;
                    key=p1+"."+p2;
                    mi.put(key,val); 
                }                
            }
        }
        return mi;
    }
    
        //calculates the log likelihood of occurence of an instance of data
    public static double[] logLikelihood(List<int[]> data,HashMap<Edge, Double[]> cpt, Double[] zero){
        double[] b= new double[data.size()];
        int k=0; //iterator for number of rows in the data
        for(int[] row: data){
            double theta=0;
            if(row[0]==0){
                theta= theta+ Math.log10(zero[0]);
            }
            else{
                theta= theta+ Math.log10(zero[1]);
            }
            for(Edge e: cpt.keySet()){
                int v1= e.getVertex1();
                int v2= e.getVertex2();
                Double[] p= cpt.get(e);
                if(row[v2]==0 && row[v1]==0){
                    theta = theta + Math.log10(p[0]); 
                }
                else if(row[v2]==0 && row[v1]==1){
                    theta = theta + Math.log10(p[1]);
                }
                else if(row[v2]==1 && row[v1]==0){
                    theta = theta + Math.log10(p[2]);
                }
                else{
                    theta = theta + Math.log10(p[3]);
                }              
            }
            b[k]=theta;
            k++;
        }
        return b;
    }
    
    public static HashMap<Edge, Double[]> conditionalProbTable(ArrayList<Edge> dfs,HashMap<String, Double> jd, Double[] prob_0, Double[] prob_1){
        HashMap<Edge, Double[]> cpt = new HashMap<Edge, Double[]>();
        //remember that my dfs always starts with node 0!
        
        
        for(Edge e: dfs){
            int v1= e.getVertex1();
            int v2= e.getVertex2();    
            Double[] prob= new Double[4];
            //find conditional prob of v2 given v1 // conditional parameter p(a|b)=p(a,b)/p(b) 
            String s="Node_"+v2+".Node_"+v1+".00";
            double p00= jd.get(s)/prob_0[v1];
            prob[0]=p00;
            s="Node_"+v2+".Node_"+v1+".01"; //v2=0 given v1=1
            double p01= jd.get(s)/prob_1[v1];
            prob[1]=p01;
            String s1="Node_"+v2+".Node_"+v1+".10";
            double p10= jd.get(s1)/prob_0[v1];
            prob[2]=p10;
            s="Node_"+v2+".Node_"+v1+".11";
            double p11= jd.get(s)/prob_1[v1];
            prob[3]=p11;
            cpt.put(e,prob);
        }
        
        return cpt;
    }
    
    
    //function to which each file name is provided so that it can process and produce avg log likelihoods
    public static void reportLog(String fileName_training, String fileName_test){
        System.out.println("================================");
        System.out.println("Considering data: "+fileName_training);
        List<int[]> data=readFile(fileName_training);
        double rows= data.size();
        Double[] prob_0= theta(data,0); //laplace-1 smoothed probability
        Double[] prob_1= theta(data,1); //or simply 1-prob_0
        //name variables/parameters of the data and place corresponding probabilities in hashmaps
        String[] parameters= new String[prob_0.length];
        HashMap<String, Double> prob0= new HashMap<String, Double>();
        HashMap<String, Double> prob1= new HashMap<String, Double>();
        for(int i=0; i<prob_0.length; i++){
            parameters[i]="Node"+"_"+Integer.toString(i);
            prob0.put(parameters[i],prob_0[i]);
            prob1.put(parameters[i],prob_1[i]);
        } 
        HashMap<String, Double> jd = jointDistribution(data,parameters);
        HashMap<String, Double> mi = mutualInformation(jd, parameters, prob0, prob1);
//        System.out.println("Mutual information calculated between parameters");
        ArrayList<Edge> dfs = maxST_dfs.mst(mi,parameters);
        Double[] zero= new Double[2];
        zero[0]=prob_0[0];
        zero[1]=prob_1[0];
        
        //calculate cpt for the dfs 
        HashMap<Edge, Double[]> cpt = conditionalProbTable(dfs,jd,prob_0,prob_1);
        List<int[]> test=readFile(fileName_test);
        double[] ll= logLikelihood(test,cpt,zero);
        double sum=0;
        for(int j=0; j<ll.length;j++){
            sum=sum+ll[j];
        }
        System.out.println("\n\nSum of all log likelihoods for all rows: "+sum);
        System.out.println("Average log likelihood (log base 10) for this data: " + (sum/rows));
        System.out.println();
        System.out.println("___________________________________________________");
        
    }
    
    public static void main(String[] args){
    
        reportLog("../hw4-datasets/small-10-datasets/accidents.ts.data","../hw4-datasets/small-10-datasets/accidents.test.data");
        reportLog("../hw4-datasets/small-10-datasets/baudio.ts.data","../hw4-datasets/small-10-datasets/baudio.test.data");
        reportLog("../hw4-datasets/small-10-datasets/bnetflix.ts.data","../hw4-datasets/small-10-datasets/bnetflix.test.data");
        reportLog("../hw4-datasets/small-10-datasets/dna.ts.data","../hw4-datasets/small-10-datasets/dna.test.data");
        reportLog("../hw4-datasets/small-10-datasets/jester.ts.data","../hw4-datasets/small-10-datasets/jester.test.data");
        reportLog("../hw4-datasets/small-10-datasets/kdd.ts.data","../hw4-datasets/small-10-datasets/kdd.test.data");
        reportLog("../hw4-datasets/small-10-datasets/msnbc.ts.data","../hw4-datasets/small-10-datasets/msnbc.test.data");
        reportLog("../hw4-datasets/small-10-datasets/nltcs.ts.data","../hw4-datasets/small-10-datasets/nltcs.test.data");
        reportLog("../hw4-datasets/small-10-datasets/plants.ts.data","../hw4-datasets/small-10-datasets/plants.test.data");
    }

}