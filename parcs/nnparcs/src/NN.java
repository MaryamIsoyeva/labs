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

        if(dataToTransf.exec == false){
            String x = dataToTransf.s;
            int numOfPoints = 3;
            int len = x.length() / numOfPoints;
            List<channel> chans = new ArrayList<>();
            HashMap<List<String>, Integer> ml = new HashMap<List<String>, Integer>();
        int pos = 0;
        int nextPos = 0;
        for(int i = 0; i < numOfPoints; ++i){
            point p = info.createPoint();
            channel c = p.createChannel();
            nextPos = (i+1)*len; //x.substring(pos, (i+1)*len).indexOf(" ", pos + len);
            p.execute("NN");

            if(/*nextPos == -1*/ i == numOfPoints -1){
                c.write(x.substring(pos));
                System.out.println(nextPos);
//                pos = (i+1)*len;
            }
            else {
                System.out.println(nextPos);
                c.write(x.substring(pos, nextPos));
                pos = nextPos + 1;
            }
            chans.add(c);
        }
            for(channel chan: chans){
//            int length = chan.readInt();
//            System.out.println("length");
//            System.out.println(length);
//            byte[] m = new byte[length];
//            chan.read(m);
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                DataToTransf dat = (DataToTransf)chan.readObject();

//                HashMap<List<String>, Integer> d = (HashMap)DataToTransf.toObject(m); //DataToTransf.toObject(m);
                dat.grammap.forEach((l, v) -> ml.merge(l, v, Integer::sum));

//            DataToTransf d = (DataToTransf)chan.readObject();


            }
            DataToTransf objDat = new DataToTransf(ml);
            info.parent.write(objDat);
        }
        else{
            String[] words = dataToTransf.s.split("\\W+");
            HashMap<List<String>, Integer> ml = new HashMap<List<String>, Integer>();
            int n = 3;
            int count = 0;
            for(int i = 0; i <= words.length - n; ++i){
                List<String> l = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(words, i, i+n)));
                count = ml.getOrDefault(l, 0);
                ml.put(l, count + 1);
            }

            DataToTransf d = new DataToTransf(ml);
//            byte[] b= DataToTransf.toByteArray(ml);
//            info.parent.write(b.length);
            info.parent.write(d);

        }


//        String x = (String)info.parent.readObject();


//        byte n [] = info.parent.read
//        String[] words = s.split("\\W+");
//        HashMap<List<String>, Integer> ml = new HashMap<List<String>, Integer>();
//        int n = 3;
//        int count = 0;
//        for(int i = 0; i <= words.length - n; ++i){
//            List<String> l = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(words, i, i+n)));
//            count = ml.getOrDefault(l, 0);
//            ml.put(l, count + 1);
//        }
//
//            DataToTransf d = new DataToTransf(ml);
////            byte[] b= DataToTransf.toByteArray(ml);
////            info.parent.write(b.length);
//            info.parent.write(d);
//            System.out.println(ByteUtil.toObject(b));


//        DataToTransf d = new DataToTransf(ml);
//        info.parent.write(d);



//        System.out.println("[" + n.getId() + "] Build started.");
//
//        List<point> points = new ArrayList<>();
//        List<channel> chans = new ArrayList<>();
//        for (Node d: n.getDeps()) {
//            point p = info.createPoint();
//            channel c = p.createChannel();
//            p.execute("DFS");
//            c.write(d);
//            points.add(p);
//            chans.add(c);
//        }
//        long sum = n.getTime();
//        for (channel c: chans) {
//            sum += c.readLong();
//        }
//        try {
//            Thread.sleep(n.getTime());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return;
//        }
//        System.out.println("[" + n.getId() + "] Build finished.");
//        info.parent.write(sum);
    }
}
