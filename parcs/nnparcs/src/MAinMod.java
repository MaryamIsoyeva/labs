import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.util.HashMap;
import parcs.*;

public class MAinMod {
    public static void main(String[] args) throws Exception {
        task curtask = new task();
        curtask.addJarFile("NN.jar");
        AMInfo info = new AMInfo(curtask, null);
        byte[] n = fromFile(curtask.findFile("input"));
        String x = new String(n);
        int numOfPoints = 3;
        int len = x.length() / numOfPoints;
        List<channel> chans = new ArrayList<>();
        HashMap<List<String>, Integer> ml = new HashMap<List<String>, Integer>();
        int pos = 0;
        int nextPos = 0;
        for(int i = 0; i < numOfPoints; ++i){
            point p = info.createPoint();
            channel c = p.createChannel();
            nextPos = x.substring(pos, (i+1)*len).indexOf(' ', pos + len);
            p.execute("NN");
            if(nextPos == -1){
                c.write(x.substring(pos));
                pos = (i+1)*len;
            }
            else {
                c.write(x.substring(pos, nextPos));
                pos = nextPos + 1;
            }
            chans.add(c);

        }


//        Node n = fromFile(curtask.findFile("input"));

//        AMInfo info = new AMInfo(curtask, null);
//        point p = info.createPoint();
//        channel c = p.createChannel();
//        p.execute("NN");
//        c.write(x);
//        c.write(n);

        System.out.println("Waiting for result...");
        for(channel chan: chans){
            int length = chan.readInt();
            byte[] m = new byte[length];
            chan.read(m);
            try {
                HashMap<List<String>, Integer> d = (HashMap)DataToTransf.toObject(m); //DataToTransf.toObject(m);
                d.forEach((l, v) -> ml.merge(l, v, Integer::sum));

//            DataToTransf d = (DataToTransf)chan.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        try{
            PrintWriter out = new PrintWriter(new FileWriter(info.curtask.addPath("NN.res")));
            out.println(ml);
            out.close();
        } catch (IOException e) {e.printStackTrace(); return;}
        System.out.println("Result: " + ml.keySet().toArray()[0]);
        curtask.end();
    }

    public static byte[] fromFile(String filename) throws Exception {
        Path path = Paths.get(filename);
        try {
            byte[] data = Files.readAllBytes(path);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
//        Scanner sc = new Scanner(new File(filename));
//        int m = sc.nextInt();
//        int s = sc.nextInt();
//        List<Node> nodes = new ArrayList<>();
//        for (int i = 0; i < m; i++) {
//            nodes.add(new Node(i + 1));
//        }
//        for (Node n: nodes) {
//            n.setTime(sc.nextInt());
//            int k = sc.nextInt();
//            for (int j = 0; j < k; j++) {
//                n.addDep(nodes.get(sc.nextInt() - 1));
//            }
//        }
//        return nodes.get(s - 1);
    }
}
