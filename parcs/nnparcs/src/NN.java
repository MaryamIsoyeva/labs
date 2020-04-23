import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.io.IOException;
import java.io.*;
import parcs.*;

public class NN implements AM {
    public void run(AMInfo info) {
        DataToTransf dataToTransf = (DataToTransf)info.parent.readObject();
        List<point> points = new ArrayList<>();
        List<channel> chans = new ArrayList<>();
        if(dataToTransf.trainable){
            dataToTransf.train(dataToTransf.hamstring, true);
            dataToTransf.train(dataToTransf.spamstring, false);
            float f = (double)dataToTransf.classify(dataToTransf.predict);
            info.parent.write(f);
        }
        else{
            
            int numOfSplit = 3;
            int spamlen = dataToTransf.spamstring.length() / numOfSplit;
            int hamlen =dataToTransf.hamstring.length()/ numOfSplit;
            int startspam = 0;
            int startham = 0;
            int indexofspaceinspam = 0;
            int indexofspaceinham = 0;
            for(int i = 0; i < numOfSplit; ++i){
                indexofspaceinspam = dataToTransf.spamstring.substring((i+1)*spamlen).indexOf(' ');
                indexofspaceinham = dataToTransf.hamstring.substring((i+1)*hamlen).indexOf(' ');
                DataToTransf z = new DataToTransf(dataToTransf.spamstring.substring(startspam, (i+1)*spamlen + indexofspaceinspam), dataToTransf.hamstring.substring(startham, (i+1)*hamlen + indexofspaceinham),true);
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
                if(c.readDouble() > 0.51){
                    numOfOnes +=1;
                }
                else{
                    numOfZeroes +=1;
                }
            }
            if(numOfOnes > numOfZeros){
                info.parent.write(1);
            }
            else{
                info.parent.write(0);
            }
            
        }
    }
}
