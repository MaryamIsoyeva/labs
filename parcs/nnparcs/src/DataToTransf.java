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

public class DataToTransf implements Serializable {
    public HashMap<List<String>, Integer> grammap;
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
    }

//    public static byte[] toByteArray(Object obj) throws IOException {
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
//    }
}
