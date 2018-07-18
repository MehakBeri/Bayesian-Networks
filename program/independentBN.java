import java.io.*;
import java.util.*;

public class independentBN{
    
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
    public static float[] theta(List<int[]> data,int v){
        //probaility with one -laplace smoothing
        int NoParameters= data.get(0).length;
        float noRows=data.size();
        float[] prob= new float[NoParameters];
        for(int i=0; i<NoParameters; i++){
            float count=0;
            for(int[] row: data){
                if(row[i]==v){
                    count++;
                }
            }
            float p= (count+1)/(noRows+2) ;
            prob[i]= p;
        }
//        System.out.println("Probability of "+v);
//        for(int j=0; j<NoParameters;j++){
//            System.out.println(" "+prob[j]);
//        }
        return prob;
    }
    //calculates the log likelihood of occurence of an instance of data
    public static double[] bayesianNetworkLL(List<int[]> data,float[] prob0,float[] prob1){
        double[] b= new double[data.size()];
        int k=0; //iterator for number of rows in the data
        for(int[] row: data){
            double theta=0;
            for(int i=0; i<row.length; i++){
                if(row[i]==0){
                    theta=theta+ Math.log10(prob0[i]);
                }
                else{
                    theta=theta+ Math.log10(prob1[i]);
                }
            }
            b[k]=theta;
//            for(int j=0; j<row.length;j++){
//                System.out.print(row[j]+" ");
//            }
//            System.out.println(":"+b[k]);
            k++;
        }
        return b;
    }
    //function to which each file name is provided so that it can process and produce avg log likelihoods
    public static void reportLog(String fileName_training, String fileName_test ){
        List<int[]> data=readFile(fileName_training);
        List<int[]> test=readFile(fileName_test);
        double rows= data.size();
        float[] prob_0= theta(data,0); //laplace-1 smoothed probability
        float[] prob_1= theta(data,1); //or simply 1-prob_0
        double[] bn= bayesianNetworkLL(test,prob_0,prob_1); 
        System.out.println("Training set: "+fileName_training);
        System.out.println("Log likelihood of dataset: "+fileName_test);
        double sum=0;
        for(int j=0; j<bn.length;j++){
//            System.out.println(bn[j]);
            sum=sum+bn[j];
        }
        System.out.println("Sum of all log likelihoods for all rows: "+sum);
        System.out.println("Average log likelihood (log base 10) for this data: " + (sum/rows));
        System.out.println();
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