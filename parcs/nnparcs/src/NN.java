import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.io.IOException;
import java.io.*;
import parcs.*;

public class NN implements AM {
    public void run(AMInfo info) {

        String s = (String)info.parent.readObject();

//        byte n [] = info.parent.read
        String[] words = s.split("\\W+");
        HashMap<List<String>, Integer> ml = new HashMap<List<String>, Integer>();
        int n = 3;
        int count = 0;
        for(int i = 0; i <= words.length - n; ++i){
            List<String> l = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(words, i, i+n)));
            count = ml.getOrDefault(l, 0);
            ml.put(l, count + 1);
        }
        try {
            byte[] b= DataToTransf.toByteArray(ml);
            info.parent.write(b.length);
            info.parent.write(b);
//            System.out.println(ByteUtil.toObject(b));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

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
