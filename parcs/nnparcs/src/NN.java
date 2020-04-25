import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.io.IOException;
import java.io.*;
import java.util.*;
import parcs.*;

public class NN implements AM {
    public void run(AMInfo info) {
        
        
        //        info.parent.write(0);
        try{
            
        DataToTransf dataToTransf = (DataToTransf)info.parent.readObject();
        List<point> points = new ArrayList<>();
        List<channel> chans = new ArrayList<>();
            try{
                
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                
                
                BufferedWriter writerf = new BufferedWriter(new FileWriter("train", true));
                String trainable =dataToTransf.trainable?"trainable":"module";
                writerf.append(trainable);
                writerf.append("train");
                writerf.close();
            }catch(Exception exc){ System.out.println("");}

//            info.parent.write(0);
        if(dataToTransf.trainable){
            
            
            
            
            
            
            
//                BufferedWriter writerbuf = new BufferedWriter(new FileWriter("stacktrace", true));
//                writerbuf.append("Predict");
//                writerbuf.append(dataToTransf.predict);
//                
//                writerbuf.close();
            dataToTransf.train(dataToTransf.hamstring, true);
            dataToTransf.train(dataToTransf.spamstring, false);
//            double f = 0.1;
            double f = (double)dataToTransf.classify(dataToTransf.predict);
            info.parent.write(f);
        }
        else{
            
            int numOfSplit = 2;
            int spamlen = dataToTransf.spamstring.length() / numOfSplit;
            int hamlen =dataToTransf.hamstring.length()/ numOfSplit;
            int startspam = 0;
            int startham = 0;
            int indexofspaceinspam = 0;
            int indexofspaceinham = 0;
            BufferedWriter writer = new BufferedWriter(new FileWriter("predict", true));
            writer.append(dataToTransf.predict);
            
            writer.close();
            
            for(int i = 0; i < numOfSplit; ++i){
                indexofspaceinspam = dataToTransf.spamstring.substring((i+1)*spamlen).indexOf(' ');
                indexofspaceinham = dataToTransf.hamstring.substring((i+1)*hamlen).indexOf(' ');
                DataToTransf z = new DataToTransf(dataToTransf.spamstring.substring(startspam, (i+1)*spamlen + indexofspaceinspam), dataToTransf.hamstring.substring(startham, (i+1)*hamlen + indexofspaceinham),true, dataToTransf.predict, 0);
                startspam = (i+1)*spamlen + indexofspaceinspam;
                startham = (i+1)*hamlen + indexofspaceinham;
                point p = info.createPoint();
                channel c = p.createChannel();
                p.execute("NN");
                c.write(z);
                points.add(p);
                chans.add(c);
            }
            int numOfOnes = 0; //spam
            int numOfZeros = 0;
            for (channel c: chans) {
//                try{BufferedWriter writerbuf = new BufferedWriter(new FileWriter("stacktrace", true));
//                writerbuf.append("Predict");
//                writerbuf.append(dataToTransf.predict);
//                writerbuf.close();
//                }catch(Exception excep){ System.out.println("");}
                if(c.readDouble() > 0.51){
                    numOfOnes +=1;
                }
                else{
                    numOfZeros +=1;
                }
            }
            int predictedLabel =1;
            if(numOfOnes > numOfZeros){
                info.parent.write(predictedLabel);
            }
            else{
                predictedLabel=0;
                info.parent.write(predictedLabel);
            }
            
        }
        }
        catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("stacktrace", true));
            writer.append(sw.toString());
            
                writer.close();}catch(Exception exception){ System.out.println("");}
        }
    }
}
