package HostsServer;

import java.io.*;
import java.net.*;
import java.util.*;
import parcs.*;

/**
 * ������ ������������ ��������� ���������� ������������������ �����.
 */
public class Benchmark {
	final static int bw_count = 2000; //���������� ������������ ������ � getBandwidth
	final static int bw_blocksize = 2500; //������ ����� � getBandwidth
	public static int threshold = 30; //����� ������� (��� ping'�, ����� �� ������� ��������, ��������� �������

	/**
	 * ������� ��������� UDP-���� (������) �� ������ host, ���� �� �� ��������
	 * ��������� ����� (1 ���), ������������ -1
	 * @return ����� � �������������, � ������� �������� ������ �����, ��� -1,
	 *   ���� �� �� ������
	 */
	public static int ping (HostInfo host){
		if (!host.resolveName()){
			System.err.println("Ping: cannot resolve hostname "+host.name);
			return -1;
		}
		long t1, t2;
		try{
			DatagramSocket sock = new DatagramSocket();
			sock.setSoTimeout(1000);
			byte[] data = new byte[9];
			(new Random()).nextBytes(data); data[0]=Const.DM_UDP_PING;
			DatagramPacket pack1 = new DatagramPacket(data, data.length,
					host.ipAddress,	Const.DAEMON_UDP_PORT);
			byte[] result = new byte[8];
			DatagramPacket pack2 = new DatagramPacket(result, result.length);

			t1 = System.currentTimeMillis();
			sock.send(pack1);
			try { sock.receive(pack2); }
			catch (SocketTimeoutException e) {
				//System.err.println("Ping: timeout expired, answer not received from "+host.ipAddress);
				return -1;
			}
			t2 = System.currentTimeMillis();
			int t = (int)(t2-t1);
			if (t <= threshold) return 0;
			else return t;
		} catch( IOException e) {
			System.err.println("Ping: "+e);
			return -1;
		}
	}
	public static int pingSeq(HostInfo host, int num) {
		if (!host.resolveName()){
			//System.err.println("Ping: cannot resolve hostname "+host.name);
			return -1;
		}
		int sum=0, c=0;
		for (int i=0; i<num; i++) {
			int p = ping(host);
			if (p<0) {p=1000; c++;}
			sum += p;
		}
		if (c==num) sum = -1;
		return sum;
	}

	/**
	 * ���������� ����� TCP-���������� (� �������������) � ������� host
	 * ��������� ����� (1 ���), ������������ -1
	 * @return ����� � �������������, � ������� �������� ���������� ����������,
	 * ��� -1, ���� �� ����������
	 */
	public static int getConnectionTime(HostInfo host){
		long t1, t2;
		try {
			t1 = System.currentTimeMillis();
			host.connect(true);
			t2 = System.currentTimeMillis();
		} catch (Exception e) { return -1; }
		return (int)(t2-t1);
	}

	/**
	 * �������� ������ �� ����������� ������������������ ���������� ������ host, ��
	 * ��� ����������� ����, ����� ���� ������������ ��������� ��� ���� ������
	 * @return ������������������ ���������� � Mflops (���������� �������� �
	 *   ��������� ������ � �������), ��� 0.0, ���� ���� �� ��������
	 */
	public static double getPerformance(HostInfo host){
		double result = 0.0;
		try { host.connect();}
		catch (Exception e) { return result;}
		try {
			OutputStream out = host.socket.getOutputStream();
			InputStream in = host.socket.getInputStream();
			channel chan = new channel(out, in, true);
			chan.dout.writeByte(Const.DM_PERFORMANCE); out.flush();
			if (!Const.NO_TIMEOUTS) host.socket.setSoTimeout(20000);
			try{ result = chan.din.readDouble(); }
			catch (SocketTimeoutException e) {
				System.err.println("getPerfomance: timeout");
			}
		}
		catch (IOException e) {	System.err.println("getPerfomance: "+e);	}
		finally { host.closeSocket(); }
		return result;
	}

	/**
	 * ���������� ���������� ����������� ������ � ��������� ������� host
	 * @return ����������� ���������� ����������� � ��/���, ��� 0, ���� ���� ��
	 *   ��������
	 */
	public static double getBandwidth(HostInfo host){
		double result = 0.0;
		try { host.connect();}
		catch (Exception e) { return result;}
		try {
			OutputStream out = new BufferedOutputStream(host.socket.getOutputStream());
			InputStream inp = new BufferedInputStream(host.socket.getInputStream());
			channel chan = new channel(out, inp, true);

			out.write(Const.DM_ECHO);
			byte[] buf = new byte[bw_blocksize];
			if (!Const.NO_TIMEOUTS) host.socket.setSoTimeout(15000);
			Reader r = new Reader(inp); r.start();

			long t1 = System.currentTimeMillis();
			for (int i=0; i<bw_count; i++) {
//				System.out.println(i);
				out.write(buf);
			}
			out.flush();

			//���� ��������� ������ (� ������� 20 ���)
			synchronized (r) {
				if (!r.ok)
					try { if (!Const.NO_TIMEOUTS) r.wait(20000);
						  else r.wait();
				    } catch (InterruptedException e) {}
			}
			long t2 = System.currentTimeMillis();
			//System.out.println("getBandwidth: time1=" +(t2-t1)/1000.0);
			if (r.ok) result = ((double)bw_blocksize*bw_count)/1e6/(t2-t1);
		}
		catch (IOException e) {	System.err.println("getBandwidth: "+e);	}
		finally { host.closeSocket(); }
		return result;
	}

}

class Reader extends Thread {
	public boolean ok = false;
	private InputStream inp;

	public Reader(InputStream inp){	this.inp = inp;}
	public void run() {
		try {
			byte[] buf = new byte[2500];
			int total = 0, c;
			while (total < Benchmark.bw_count*Benchmark.bw_blocksize) {
				c=inp.read(buf);
				if (c<0) break;
				total += c;
//				System.out.println("total="+total);
			}
			ok = (total>=Benchmark.bw_count*Benchmark.bw_blocksize);
		}
		catch (SocketTimeoutException e) { System.err.println("getBandwidth: timeout");}
		catch (IOException e) {System.err.println("getBandwidth: "+e);}
		finally { synchronized (this) {notifyAll();} }
	}
}
