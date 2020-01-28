package parcs;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * �����. ����� ����� ����������� �������� � ��������� ������ ����
 * ��������������� ������. ������ point ������������ � �� ��� �������������
 * ��������� �����, �������� � ��� ������ ������� createChannel(), ������� � ���
 * �� ������� execute(String). ����� ����� ���� ������� � �� �������
 * task.createPoint(), ���� ������������ point(task).
 *
 * @(#)point.java
 */
public class point {
	/**
	 * ��������� � �����, � �������� ��������� �����
	 */
	public HostInfo host;
	/**
	 * ����� ��� ����� � ���� ������, ���� ��� ���������, ��� ����� ��� ����� �
	 * ������������ ������, ���� ��� ���������
	 */
	public channel chan;
	/**
	 * ������ ��� ����� ���� ������ � ������� �������
	 */
	public task curTask;
	/**
	 * ����� ����� (������������� �������� ������ ��� ��������)
	 */
	public int number;

	public int parentNumber;

//	private byte[] sendQuery(){(){(){

	/*
	 * ������� ��������� ����� �� ������� ������, ��������� ��� �������������
	 * ��������� ������
	 */
	public point(Socket sock, task curtask, int pointnum) {
		host = new HostInfo(sock.getInetAddress()/*.getHostName()*/);
		host.socket = sock;
		curTask = curtask; number = pointnum;
	}

	/**
	 * ������� ������ �����, �������� asklocation ���������, ���������� �� ���
	 * �������� � ������� ������ ��� �����������
	 * @param asklocation ���� true, ���������� � �������
	 */
	public point(boolean asklocation, task curtask, int parentNumber) {
        if (!asklocation) return;
		curTask = curtask;
		HostInfo serv = curTask.getServer();
		this.parentNumber = parentNumber;

		final int ip_length = serv.ipAddress.getAddress().length;
		ServerQuery query = new ServerQuery();
		byte[] ip;
		do {
			DataOutputStream dout = query.getOutputStream(5);
			try {
				dout.writeByte(Const.HS_CREATE_POINT);
				dout.writeInt(curTask.number);
				dout.writeInt(parentNumber);
			} catch (IOException e) { System.err.println(e); }

			if (query.send(serv, ip_length+4)==null) return;

			DataInputStream dinp = query.getInputStream();
			ip = new byte[ip_length];
			try {
				number = dinp.readInt();
				dinp.read(ip);
			} catch (IOException e) { System.err.println(e); }
		} while (number==Const.HS_WAIT);

		try { host = new HostInfo(InetAddress.getByAddress(ip)); }
		catch (UnknownHostException e) {
            System.err.println("Host which address received from server doesn't exist");
        }
    }

	/**
	 * ������� ������ �����, ��������� ��� �������� � ������� ������ ���
	 * �����������, �� ��, ��� point(true, curtask)
	 */
	public point(task curtask, int parentNumber) { this(true, curtask, parentNumber); }

	/**
	 * ������� �����, ������� ��������������� ��������� �������
	 */
	public void delete(){
		HostInfo serv = curTask.getServer();

		ServerQuery query = new ServerQuery();
		DataOutputStream dout = query.getOutputStream(9);
		try {
			dout.writeByte(Const.HS_DELETE_POINT);
			dout.writeInt(curTask.number);
			dout.writeInt(number);
		} catch (IOException e) { System.err.println(e); }

		query.send(serv, 0);

   }

    /**
     * ������� ����� � ������ ������, ���������� � ������� �� ��������� �����.
     * ���� ���������� ���������� �� ������, ���������� null
     * @return ������ �� ��������� �����, ��� null, ���� ������� ����������
     */
    public channel createChannel() {
		if (!host.isConnected())
			if (!host.cycleConnect()) {
				System.err.println(
						"createChannel: Cannot connect to daemon on " + host);
				return null;
			}
		BufferedInputStream inp;
		BufferedOutputStream out;
		try {
			inp = new BufferedInputStream(host.socket.getInputStream());
			out = new BufferedOutputStream(host.socket.getOutputStream());

			chan = new channel(out, inp, number, -1, curTask.recovery);
			chan.works = host.isConnected();

			if (curTask.recovery != null)
				curTask.recovery.CreateChildEvent(parentNumber, number);
		}
		catch (IOException e) {
			System.err.println("initChannel: " + e);
			return null;
		}

		try {
			chan.dout.writeByte(Const.DM_RECEIVE_TASK);
			chan.objout.writeObject(curTask);
			chan.dout.writeInt(number);
			//�������� jar-����(�)
			Collection fNames = curTask.getFileNames();
			if (!fNames.isEmpty()) {
				chan.dout.writeByte(Const.DM_LOAD_JARFILES);
				chan.dout.writeInt(fNames.size());
				chan.dout.flush();
				for (Iterator i = fNames.iterator(); i.hasNext(); )
					sendFile( (String) i.next());
			}
			else
				chan.dout.flush();
		}
		catch (IOException e) {
			System.err.println("initChannel: " + e);
			return null;
		}

		chan.loader = curTask.loader;
		return chan;
    }

	private static boolean canRead(String file){
		if (file==null) {System.err.println("execute: filename==null"); return false;}
		if (new File(file).canRead()) return true;
		if (!file.endsWith(".class") && !file.endsWith(".jar") && !file.endsWith(".zip"))
			if (new File(file+".class").canRead()) return true;

		System.err.println("Cannot read file "+file);
		return false;
	}

	/**
	 * ��������� � ������ ����� ��������������� ������ (��)
	 * @param AMname ��� ������, ����������� ��
	 */
	public void execute(String AMname) {
		if (!chan.works) {System.err.println("Error: channel broken");return;}

		executeClass(AMname);
    }
	public void setp(String AMname) {execute(AMname);}

	void execute(String[] classes) {
		//for(int i=0; i<classes.length; i++)
		//	if (!canRead(classes[i])) return;

		if (!chan.works) {System.err.println("Error: channel broken");return;}
		try {
			chan.dout.writeByte(Const.DM_LOAD_CLASSES);
			chan.dout.writeInt(classes.length);
		} catch (IOException e) {System.err.println("execute: "+e);return;}
		for(int i=0; i<classes.length; i++) {
			sendFile(classes[i].replace('.', '/').concat(".class"));
		}
		executeClass(classes[0]);
	}

	private boolean executeClass(String name) {
		try {
			chan.dout.writeByte(Const.DM_EXECUTE_CLASS);
			chan.objout.writeObject(name);
			chan.out.flush();
		}
		catch (IOException e) {System.err.println("executeClass: "+e);return false;}
		return true;
	}

	/**
	 * ������� ���� � ������ name � ����� out
	 */
	private boolean sendFile(String fullname) {
        try {
			File f = new File(fullname);
	        int size=(int)f.length();

			String name = f.getName();
			chan.objout.writeObject(name);
			chan.out.flush();
			if (!chan.din.readBoolean()) return true;
			chan.dout.writeInt(size);

	        byte[] buf = new byte[size<8192? size: 8192];
			int len;
			InputStream fin = new FileInputStream(f);
			while ( (len=fin.read(buf)) != -1) {
				chan.out.write(buf,0,len);
			}
			chan.out.flush();
			System.out.println("File "+fullname+" was sent to host "+host);
			fin.close();
			return true;
        }
        catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * ������� ����� ����� � ������ �
	 * @param p ����� � ������� ��������� ����� �����
	 * @return true, ���� ����� ������� ����������. false, �����.
	 */
	public boolean connectPoint(point p) {
		return connectPoint(null, p, null);
	}

	/**
	 * ������� ����� ����� � ������ �
	 * @param name ��� ������ �� ������� ������ �����. �������� null ������������
	 * @param p ����� � ������� ��������� ����� �����
	 * @param pname ��� ������ �� ������� ����� �. �������� null ������������
	 * @return true, ���� ����� ������� ����������. false, �����.
	 */
	public boolean connectPoint(Object name, point p, Object pname) {
		try {
			chan.dout.writeByte(Const.DM_CONNECT_POINT);
			chan.objout.writeObject(p.host.ipAddress);
			chan.objout.writeObject(name);
			chan.dout.writeByte((byte)number);
			chan.objout.writeObject(pname);
			chan.dout.writeByte((byte)p.number);
			chan.dout.flush();
			return chan.din.readBoolean();
		}
		catch (IOException e) {
			System.err.println("createChannel: " + e);
			return false;
		}
	}

	/**
	 * ��������� ������ � ������ �����, ������� ����� �������� � AM.run
	 * @param key ����
	 * @param value ��������
	 */
	public void AddData(Object key, Object value)
	{
		try {
			chan.dout.writeByte(Const.DM_ADD_POINT_DATA);
			chan.objout.writeObject(new Integer(number));
			chan.objout.writeObject(key);
			chan.objout.writeObject(value);
			chan.dout.flush();
		}
		catch (IOException e) {
			System.err.println("createChannel: " + e);
			return;
		}
	}
}