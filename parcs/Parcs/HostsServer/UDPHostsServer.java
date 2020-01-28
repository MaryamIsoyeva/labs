package HostsServer;

import java.io.*;
import java.net.*;
import java.util.*;
import parcs.Const;
/**
 * UDP-������ ��� ������ �������� (��� ������������ TCPHostsServer'�, �� � ������� �������������������)
 * ����������� ��������������� �������� ���������, �� ������ �� �������
 * ������ UDPHostsServer.java � UDPReceiver.java ����� ������� �� ������ ��� ����������
 * �� ������������� �����
 */
public class UDPHostsServer extends Thread {
	boolean quit = false;
	private static HostsServer hserver;

	public UDPHostsServer() { hserver = new HostsServer(); }
	public UDPHostsServer(List c) { hserver = new HostsServer(c); }

	public void run() {
		DatagramSocket sock;
		try { sock = new DatagramSocket(Const.DAEMON_UDP_PORT); }
		catch (IOException e) {
			System.err.println(e);
			return;
		}

		System.out.println("Accepting UDP packets on port " + Const.DAEMON_UDP_PORT + "...");
		while (!quit) {
			UDPReceiver obj = new UDPReceiver();
			byte[] data = obj.receiveNew(sock);
			//System.out.println("Connection accepted from "+s.getInetAddress());

			byte[] result;
			DataInputStream dinp = new DataInputStream(new ByteArrayInputStream(data,1,data.length-1));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);
			try { int tasknum, pointnum;
				switch (data[0]) {
				case Const.HS_CREATE_POINT: //�������� ����� ����� (�������� ��� ��� ������)
					tasknum = dinp.readInt();
					PointInfo p = hserver.createPoint(tasknum, 0, sock.getInetAddress().getAddress());
					dout.write(p.getHostIP());
					dout.writeInt(p.number);
					result = out.toByteArray();
					break;
				case Const.HS_DELETE_POINT: //�������� �����
					tasknum = dinp.readInt();
					pointnum = dinp.readInt();
					hserver.deletePoint(tasknum, pointnum);
					result = null;
					break;
				case Const.HS_BEGIN_TASK: //������ ������
					dout.writeInt(hserver.beginTask(null));
					result = out.toByteArray();
					break;
				case Const.HS_END_TASK: //����� ������
					hserver.endTask(dinp.readInt());
					result = null;
					break;
				default:
					continue;
			    }
			obj.sendAnswer(sock, result);
			} catch (BindException e){
			System.err.println("UDP port "+Const.HOSTSSERVER_UDP_PORT+" was already bound.\n"
					+"May be another copy of UDPHostsServer already launched.\n"
					+"Try to use command line option --hsudpport <port>");
			} catch (IOException e) {e.printStackTrace();}
		} //while
		sock.close();
	}

	public static void main(String[] args){
		UDPHostsServer udp_hserver = new UDPHostsServer();
		udp_hserver.start();
	}
}
