package parcs;
import java.net.*;
import java.io.*;

/**
 * @(#)UDPQuery.java
 */
class UDPQuery{
    private static long seq = System.currentTimeMillis()<<16; //����� ������� ������
    long cur_seq = seq++; //����� �������� ������
    private byte[] result = null; //��������� �������
    public DatagramPacket packet;

    public UDPQuery(byte[] data, int length, InetAddress address, int port) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(length+10);  //��������� ������ ������� (���������)
        DataOutputStream dout = new DataOutputStream(out);
        try {
            dout.writeLong(cur_seq);
            dout.write(data, 0, length);
        } catch (IOException e) {}
        byte[] buf = out.toByteArray();
        packet = new DatagramPacket(buf, buf.length, address, port);
    }

    /**
     * �������� ������, �������������� �������������, ������� ������ ���� ����� ��
     * ��������, ������ ���������� ��������� ���. ��������� ����� �������� ��������
     * getResult()
     * @param sock ��������� �����
     * @return ���������� true, ���� ������� ����� �� ������, � false, ���� �����
     *   ���������� �������� ����� �� �������
     */
    public boolean sendQuery(DatagramSocket sock){
        long t1,t2;
        int sentCount=0; //������� ��� �������� ������� ������
        do {
            try {
                sock.send(packet);
				t1 = System.currentTimeMillis();
                byte[] data = new byte[512];
                DatagramPacket pack = new DatagramPacket(data, 512);

                sock.setSoTimeout(500);
                sock.receive(pack);
                t2 = System.currentTimeMillis();
                System.out.println("UDP answer received in "+(t2-t1)+"sec");

                if (!parseAnswer(pack)) continue;
                return true;
            } catch (SocketTimeoutException e){
                sentCount++; continue;
            }
            catch (IOException e) {
                System.err.println(e);
                continue;
            }
        } while(sentCount<8);
        return false;
    }

    /**
     * �������� ������, �������������� �������������, ������� ������, ���������
     * ��������� UDP-����. ���� ����� ��
     * ��������, ������ ���������� ��������� ���. ��������� ����� �������� ��������
     * getResult()
     * @return ���������� true, ���� ������� ����� �� ������, � false, ���� �����
     *   ���������� �������� ����� �� �������
     */
    public boolean sendQuery(){
        try { return sendQuery(new DatagramSocket()); }
        catch (SocketException e){
            System.err.println(e);
            return false;
        }
    }

    /**
     * ������������ ���������� �� ������� �����, ���������� true, ���� �����
     * ����������
     */
	public boolean parseAnswer(DatagramPacket pack){
        int pack_len = pack.getLength();
        final byte HEAD_SIZE = 8;
        if (pack_len < HEAD_SIZE) return false;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                pack.getData(), pack.getOffset(), pack_len));
        try {
            long pack_seq = in.readLong();
            result = new byte[pack_len - HEAD_SIZE];
            in.read(result);
            //System.arraycopy(pack.getData(), pack.getOffset() + headsize, result, 0, pack_len);
            if (pack_seq!=cur_seq) return false;
        } catch (IOException e) {
            System.err.println(e);
            return false;
        }
        return true;
    }
    public byte[] getResult(){ return result; }
}
