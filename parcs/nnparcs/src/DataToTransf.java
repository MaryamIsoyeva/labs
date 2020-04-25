import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.HashMap;
import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataToTransf implements Serializable {
    
    public String spamstring;
        public String hamstring;
    public boolean trainable;
    
    public String predict;
    public float predictedProbability;
    
        
        public Map<String, Integer> good = new HashMap<String, Integer>();
        public Map<String, Integer> bad = new HashMap<String, Integer>();
        
        static String[] specialCharacters = { ",", "#", ";", "\"", "\'", };
        static String empty = "";
    
    DataToTransf(String m, String n, boolean trainbool,String pre,float f){
        this.spamstring=m;
        this.hamstring = n;
        this.trainable=trainbool;
        this.predict = pre;
        this.predictedProbability=f;
    }
    DataToTransf(String m, String n, boolean trainbool){
        this.spamstring=m;
        this.hamstring = n;
        this.trainable=trainbool;
    }
        static void print(String s) {
            System.out.println(s);
        }
        
        public void store(String fname, String data) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(fname));
                fos.write(data.getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        static String getFile(String path) {
            try {
                FileInputStream fis = new FileInputStream(new File(path));
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                fis = null;
                return new String(b);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
//        static String getURL(String url) {
//            try {
//                URL u = new URL(url);
//                URLConnection uc = u.openConnection();
//                InputStream is = uc.getInputStream();
//                byte[] b=  new byte[is.available()];
//                is.read(b);
//                is.close();
//                return new String(b);
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
    
        static String takeOutSpecialCharacters(String s) {
            for (String p : specialCharacters) {
                s = s.replaceAll(p, empty);
            }
            return s.toLowerCase();
        }
        
        static int count(Map<String, Integer> map) {
            int count = 0;
            for (Integer i : map.values()) {
                count += i;
            }
            // System.out.println(count);
            return count;
        }
        
        public void train(String text, boolean ham) {
            String[] tokens = text.split(" ");
            
            Map<String, Integer> map = ham ? good : bad;
            
            for (String token : tokens) {
                token = takeOutSpecialCharacters(token);
                if (map.containsKey(token)) {
                    map.put(token, map.get(token) + 1);
                } else {
                    map.put(token, 1);
                }
            }
        }
        
        public float calculateProbability(String s) {
            s = takeOutSpecialCharacters(s);
            
            float pSpam = 0f;
            
            float goodRatio = 0;
            if (good.containsKey(s)) {
                goodRatio = (float) (((float) good.get(s)) / (float) count(good));
            }
            float badRatio = 0;
            if (bad.containsKey(s)) {
                badRatio = (float) (((float) bad.get(s)) / (float) count(bad));
            }
            if (goodRatio + badRatio > 0) {
                pSpam = (badRatio / (goodRatio + badRatio));
            }
            
            if (pSpam > 0.99f)
                pSpam = 0.99f;
            if (pSpam < 0.1f)
                pSpam = 0.1f;
            
            return pSpam;
            
        }
        
        public float interesting(Float f) {
            return Math.abs(0.5f - f);
        }
        
        public float mul(Float[] f, boolean oneMinus) {
            float a = 1.0f;
            for (float f1 : f) {
                if (oneMinus) {
                    a = a * (1 - f1);
                } else {
                    a = a * f1;
                }
            }
            return a;
        }
        
        public float classify(String text) {
            String[] tokens = text.split(" ");
            
            Set<String> set = new HashSet<String>();
            
            for (String token : tokens) {
                set.add(takeOutSpecialCharacters(token));
            }
            
            List<Float> problist = new ArrayList<Float>();
            
            
            for (String token : set) {
                
                float f = calculateProbability(token);
                
                if (f > 0.5f) {
                    // System.out.println( ""+ feature + ":"+ f);
                    problist.add(f);
                }
            }
            
            
//            Collections.sort(problist, new Comparator<Float>() {
//                @Override
//                public int compare(Float f1, Float f2) {
//                    return (int) (100 * interesting(f2) - 100 * interesting(f1)); //descending
//                }
//            });
//            
            int probablisize = problist.size();
            Float[] probabilities = problist.toArray(new Float[probablisize]);
            
            float product = mul(probabilities, false);
            float oneMinusTerm = mul(probabilities, true);
            
            print("[]" + product + "," + oneMinusTerm);
            
            return (product / (product + oneMinusTerm));
        }
        
//        static String[] pullNStore(String nub, String[] files){
//            List<String> names = new ArrayList<String>();
////            names.add(
//            for(String file : files){
//                String name = new File(file).getName();
//                name = name.substring( 0, name.indexOf("."));
//                names.add(nub+name);
//                store( nub + name, Counter.getURL(file));
//                print( "downloading :"+file +" as " + nub+name);
//            }
//            return names.toArray( new String[names.size()]);
//        }
//        
        /*public static void main(String[] args) {
            
            String[] dickens = {"http://www.gutenberg.org/ebooks/1400.txt.utf8",
                "http://www.gutenberg.org/ebooks/98.txt.utf8",
                "http://www.gutenberg.org/ebooks/730.txt.utf8",
                "http://www.gutenberg.org/ebooks/766.txt.utf8",
                "http://www.gutenberg.org/ebooks/580.txt.utf8",
                "http://www.gutenberg.org/ebooks/1023.txt.utf8",
                "http://www.gutenberg.org/ebooks/786.txt.utf8",
                "http://www.gutenberg.org/files/564/564-0.txt",
                "http://www.gutenberg.org/ebooks/967.txt.utf8",
                "http://www.gutenberg.org/ebooks/963.txt.utf8",
                "http://www.gutenberg.org/ebooks/883.txt.utf8",
                "http://www.gutenberg.org/ebooks/700.txt.utf8",
                "http://www.gutenberg.org/ebooks/821.txt.utf8"};
            
            String[] twain = {   "http://www.gutenberg.org/ebooks/76.txt.utf8",
                "http://www.gutenberg.org/ebooks/74.txt.utf8",
                "http://www.gutenberg.org/ebooks/10947.txt.utf8",
                "http://www.gutenberg.org/ebooks/19640.txt.utf8",
                "http://www.gutenberg.org/ebooks/30165.txt.utf8",
                "http://www.gutenberg.org/ebooks/86.txt.utf8",
                "http://www.gutenberg.org/ebooks/1837.txt.utf8",
                "http://www.gutenberg.org/ebooks/3176.txt.utf8",
                "http://www.gutenberg.org/ebooks/10135.txt.utf8",
                "http://www.gutenberg.org/ebooks/245.txt.utf8",
                "http://www.gutenberg.org/ebooks/119.txt.utf8",
                "http://www.gutenberg.org/ebooks/3177.txt.utf8",
                "http://www.gutenberg.org/ebooks/8525.txt.utf8",
                "http://www.gutenberg.org/ebooks/70.txt.utf8",
                "http://www.gutenberg.org/ebooks/3186.txt.utf8",
                "http://www.gutenberg.org/ebooks/2895.txt.utf8",
                "http://www.gutenberg.org/ebooks/142.txt.utf8",
                "http://www.gutenberg.org/ebooks/3200.txt.utf8",
                "http://www.gutenberg.org/ebooks/3178.txt.utf8",
                "http://www.gutenberg.org/ebooks/3250.txt.utf8",
                "http://www.gutenberg.org/ebooks/102.txt.utf8",
                "http://www.gutenberg.org/ebooks/3190.txt.utf8",
                "http://www.gutenberg.org/ebooks/26203.txt.utf8",
                "http://www.gutenberg.org/ebooks/18381.txt.utf8",
                "http://www.gutenberg.org/ebooks/3187.txt.utf8"};
            
            
            String[] storedHam = {"ham.txt"}; //pullNStore( "dickens", Arrays.copyOfRange(dickens,0,1));
            String[] storedSpam = {"spam.txt"}; //pullNStore( "twain", Arrays.copyOfRange(twain,0,1));
//            pullNStore( "twain", Arrays.copyOfRange(twain,6,7));
            
            for(String s : storedDickens) {
                train( getFile(s), true);
                System.out.println( "trained,dickens:"+ s);
            }
            
            for(String s : storedTwain) {
                train( getFile(s), false);
                System.out.println( "trained,twain:"+ s);
            }
            
            float f = classify("LOL ... Have you made plans for new years?");
            System.out.println(f);
        }*/

   /* public HashMap<List<String>, Integer> grammap;
    public HashMap<String, HashMap<String, Integer>> bigram;
    public String s;
    public boolean exec;
    private int id;
    private int time;

    public DataToTransf(int id) {
        this.id = id;
        this.time = 0;
        this.grammap = new HashMap<>();
    }

    public DataToTransf(HashMap<List<String>, Integer> gram_map){
        this.grammap = gram_map;
        this.exec = false;
    }
    public DataToTransf(HashMap<String, HashMap<String, Integer>> bi_gram){
        this.bigram = bi_gram;
        this.exec = false;
    }
    public DataToTransf(String str){
        this.s = str;
        this.exec = false;
    }

    public int getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }*/

/* //    public static byte[] toByteArray(Object obj) throws IOException {
//        byte[] bytes = null;
//        ByteArrayOutputStream bos = null;
//        ObjectOutputStream oos = null;
//        try {
//            bos = new ByteArrayOutputStream();
//            oos = new ObjectOutputStream(bos);
//            oos.writeObject(obj);
//            oos.flush();
//            bytes = bos.toByteArray();
//        } finally {
//            if (oos != null) {
//                oos.close();
//            }
//            if (bos != null) {
//                bos.close();
//            }
//        }
//        return bytes;
//    }
//
//    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
//        Object obj = null;
//        ByteArrayInputStream bis = null;
//        ObjectInputStream ois = null;
//        try {
//            bis = new ByteArrayInputStream(bytes);
//            ois = new ObjectInputStream(bis);
//            obj = ois.readObject();
//        } finally {
//            if (bis != null) {
//                bis.close();
//            }
//            if (ois != null) {
//                ois.close();
//            }
//        }
//        return obj;
//    }*/
}
