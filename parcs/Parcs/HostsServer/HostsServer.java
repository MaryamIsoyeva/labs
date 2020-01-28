package HostsServer;

import java.io.*;
import java.util.*;
import parcs.*;
/**
 * ������ ������, ������� ���������� ������������� ������� �� ������� (����������)
 * � ������ ������ ������������ �������� �������, ����������� ��������
 * ����� ���� �������� ����������� ��� ������������ ��������� �������
 * @(#)HostsServer.java
 */
public class HostsServer {
	private List curHostList; //��������� ������ ����� (�������� ����� ������)
	private HashSet hostSet;  //��������� ���� ����� (����� ����� �� ������������)
	//private HashMap hList = new HashMap(); //������ ����� ��� ������ ������
	private HashMap taskList = new HashMap();  //������ �����, ������ �� �������
	int taskNumber = 0; //����� ��������� ��������� ������
	//int targetIndex;    //����� �������� (�������� ������������) �����
	private UpdateHandler handler;
	public boolean testHosts;   //���������, ������������� �� ������������������ �����,
							    //�.�. ����� �� �� ��������� ��� �������������� �����
	volatile PerformFunction curFunc = new PerformFunction() {
				public double function(double perf, double ping/*, double bandwidth*/){
					return perf-0.1*ping;
				}};
	public Object testsMutex = new Object(); //������, ��������������� ��� ���������� ������
						    //������ ������������ ������������ � ��� �������� ��� ����������

//	public List getHostList(int tasknum){return (List)hList.get(new Integer(tasknum));}
    /**
     * ������ ���� �� ������� ������ � ��������� ������ � ������
     */
    public HostsServer() {
        int hcount = 0;
		curHostList = Collections.synchronizedList(new LinkedList());
		hostSet = new HashSet();
        try {
			BufferedReader r = new BufferedReader(new FileReader("hosts.list"));
			String s, hname;
			while ( (s=r.readLine()) !=null) {
                hname = s.trim(); if (hname=="") continue;
                if (hostSet.add(hname))
					curHostList.add(new XHostInfo(hname));
                hcount++;

//				do; while (Benchmark.getPerfomance(host)>0.0);
/*				System.out.println("Ping "+hname+": "+Benchmark.ping(host));
                System.out.println("Connection time to "+hname+": "+Benchmark.getConnectionTime(host));
*/			}
            r.close();
        }
        catch (FileNotFoundException e)
        { System.err.println("Error: hosts file not found");}
        catch (IOException e)
        { System.err.println("Error occurred while reading hosts file");}

        if (hcount==0){
			System.out.println("Hosts list not found, using localhost...");
			HostInfo host = XHostInfo.getLocalHost();
			curHostList.add(host);
			hostSet.add(host.name);
        }
    }

	public HostsServer(List c){
		curHostList = Collections.synchronizedList(new LinkedList());
		hostSet = new HashSet();
		addHostList(c);
	}
	private XHostInfo findHost(String hname) {
		synchronized (curHostList) {
			for (Iterator i=curHostList.iterator(); i.hasNext();){
				try{XHostInfo host = (XHostInfo)i.next();
					if (host.name.equals(hname)) return host;
				} catch (ClassCastException e){}
			}
		}
		return null;
	}
	public void setHostList(List c){
		List newHostList = Collections.synchronizedList(new LinkedList());
		HashSet newHostSet = new HashSet();
		for (Iterator i = c.iterator(); i.hasNext();)
			try {
				String hname = (String)i.next();
				if (newHostSet.add(hname)) {
					XHostInfo host=null;
					if (hostSet.contains(hname)) host = findHost(hname);
					if (host==null) host = new XHostInfo(hname);
					newHostList.add(host);
				}
			} catch (ClassCastException e){}
		curHostList = newHostList; hostSet = newHostSet;
	}
	private void addHostList(List c){
		for (Iterator i = c.iterator(); i.hasNext();)
			try {
				String hname = (String)i.next();
				if (hostSet.add(hname))
					curHostList.add(new XHostInfo(hname));
			} catch (ClassCastException e){}
	}
	public List getHostList() {
		LinkedList hostList = new LinkedList();
		for (Iterator i = curHostList.iterator(); i.hasNext();) {
			XHostInfo host = (XHostInfo) i.next();
			hostList.add(host.name);
		}
		return hostList;
	}
	public void addHost(String hname){
		if (hostSet.add(hname))
			curHostList.add(new XHostInfo(hname));
	}

	public void setUpdateHandler(UpdateHandler hand) {handler=hand;}

	public void resolveHostNames(List hostList) {
		for (Iterator i = hostList.iterator(); i.hasNext();) {
			XHostInfo h = (XHostInfo)i.next();
			if (!h.resolveName()) {
				System.err.println("Host "+h+" is not responding");
				i.remove();
		    }
		}
	}

	/**
	 * ���������� ������ ��� �������� ����� �����. ������� ���������� ������� �
	 * �������� ����� ��� ���������� ����� � ������ hostsList � ��������� �����
	 * ���� ������ � ����� ��������� �����
	 *
	 * @param tasknum ����� ������
	 * @param ipaddr IP-����� ������, ������� �������� ������
	 * @return ����� ������ � ����� �����
	 */
	PointInfo createPoint(int tasknum, int parentNumber, byte[] ipaddr){
		TaskInfo t = (TaskInfo)taskList.get(new Integer(tasknum));
		boolean res; XHostInfo target;
        do { target = t.getTargetHost(ipaddr);
		    if (target==null) return null;
		    res = target.resolveName();
			if (!res) {
				System.err.println("createPoint: Invalid host: " + target);
				target.ping = -1;
			}
		} while(!res);

		PointInfo p;
		synchronized (t){
			p = new PointInfo(target, parentNumber);
			t.addPoint(p);
		}

		target.pointsNumber++;

		//System.out.println("pNumber: "+target.pointsNumber);
		if (handler!=null)
			handler.updatePointsNumber(target.index, target.pointsNumber);
		return p;
    }

/**
 * �������� �����
 */
    void deletePoint(int tasknum, int pointnum) {
		TaskInfo t = (TaskInfo)taskList.get(new Integer(tasknum));
		if (t==null) return;
		synchronized (t){
			PointInfo p = t.getPoint(pointnum);
			if (p==null) {
				//System.err.println("deletePoint: Point " + tasknum +
				//		":" + pointnum + " already deleted");
				return;
			}

			if (t.recovery != null)
				t.recovery.DeletePointEvent(p.number);

			XHostInfo h = p.host;
			if (h == null)
				System.err.println("deletePoint: Host for point " + tasknum +
						":" + pointnum + " not found");
			else if (h.pointsNumber == 0);
				//System.err.println("Error: Received request to delete point on the host that contains no one");
			else h.pointsNumber--;
			t.removePoint(pointnum);

			//System.out.println(h.pointsNumber);
			if (handler!=null)
				handler.updatePointsNumber(h.index, h.pointsNumber);
		}
    }

	public void setPerfFunc(int tasknum, PerformFunction func) {
		TaskInfo t = (TaskInfo)taskList.get(new Integer(tasknum));
		if (t!=null) t.func = func;
	}

	/**
	 * ������� ����� ������, ���������� �� �����
	 * @return ����� ������� ������
	 */
	int beginTask(IRecovery recovery) {
		TaskInfo t = new TaskInfo(++taskNumber, recovery);
		taskList.put(new Integer(taskNumber), t);

		if (t.recovery != null)
			t.recovery.BeginTaskEvent(taskNumber);

		return taskNumber;
    }

	/**
	 * ��������� ������ � ��������� �������
	 */
	void endTask(int number) {	//����� ������
		TaskInfo t=(TaskInfo) taskList.remove(new Integer(number));
		t.time = System.currentTimeMillis() - t.time;

		if (t.recovery != null)
			t.recovery.EndTaskEvent();

		if (handler!=null)
		for (Iterator i=t.pointList.values().iterator(); i.hasNext();){
		    PointInfo p = (PointInfo)i.next();
			p.host.pointsNumber--;
			handler.updatePointsNumber(p.host.index, 0);
		}

		System.out.println("Task No"+t.number+" finished. Execution time: "+t.time/1000.0+"sec");
		t.printCreatingLog();
	}

private volatile int counter;
class Test extends Thread {
	private XHostInfo h;
	public Test(XHostInfo h) { this.h = h;}
	public void run(){
		//System.out.println("Testing "+h+"...");
		h.ping = Benchmark.pingSeq(h, 4);
		if (handler!=null) handler.updateHostInfo(h.name);
		if (h.ping>=0) {
			double perf = Benchmark.getPerformance(h);
			h.perform = perf;
			if (handler!=null) handler.updateHostInfo(h.name);
		}
		if (--counter==0) {
			handler.testsFinished();
			synchronized (testsMutex){ testsMutex.notifyAll(); }
		}
	}
}
	public synchronized void testHosts() {
		synchronized (curHostList) {
			counter = 0;
			for (Iterator i = curHostList.iterator(); i.hasNext();) {
				counter++;
				XHostInfo h = (XHostInfo) i.next();
				(new Test(h)).start();
			}
		}
	}
	public void testUntestedHosts() { testUntestedHosts(curHostList);}
	private synchronized void testUntestedHosts(List hostList) {
		synchronized (hostList) {
			counter = 0;
			for (Iterator i = hostList.iterator(); i.hasNext(); ) {
				XHostInfo h = (XHostInfo) i.next();
				if (h.ping >= 0 && h.perform <= 0) {
					counter++;
					(new Test(h)).start();
				}
			}
		}
	}
	public void waitTestingEnd() {
		synchronized (testsMutex) {
			if (counter > 0) try {testsMutex.wait();}
				catch (InterruptedException e) {}
		}
	}

	public XHostInfo getHostInfo(String hname) {
		synchronized(curHostList) {
			for (Iterator i=curHostList.iterator(); i.hasNext();) {
				XHostInfo h = (XHostInfo)i.next();
				if (h.name.equals(hname))
					return (XHostInfo)h.copy();
			}
		}
		return null;
	}

	/**
	 * ����������� �������� ������������������ ������ ���, ����� �� �������� ������
	 * � ������� �� 0 �� 1
	 * @param hostList ������ ������
	 */
	private void normalize(List hostList){
	double maxperf = 0.0; int maxping = 0;
	synchronized (hostList) {
		for (Iterator i=hostList.iterator(); i.hasNext();) {
			XHostInfo h = (XHostInfo)i.next();
			if (h.perform>maxperf) maxperf=h.perform;
			if (h.ping>maxping) maxping=h.ping;
		}
		for (Iterator i=hostList.iterator(); i.hasNext();) {
			XHostInfo h = (XHostInfo)i.next();
			if (maxperf==0) h.nperform=0;
		    	else h.nperform = h.perform/maxperf;
			if (maxping<0 || h.ping<=0) h.nping = h.ping;
				else h.nping = h.ping/maxping;
		}
	}
	}
	public void sortList(){	sortList(curHostList, curFunc); }
	private void sortList(List hostList, PerformFunction func){
		normalize(hostList);
		synchronized (hostList) {
			for (Iterator i = hostList.iterator(); i.hasNext(); ) {
				XHostInfo h = (XHostInfo) i.next();
				if (h.ping < 0) h.resPerf = Double.NEGATIVE_INFINITY;
				else h.resPerf = func.function(h.perform, h.ping);
			}
			Collections.sort(hostList);
		}
	}

/**
 * ���������� � ������
 */
class TaskInfo {
	public int number; //����� ������
	int processNumber; //����� ��������� ��������� �����(��������) ������� ������
	private HashMap pointList = new HashMap(); //������ ����� (������� PointInfo) �� �������
	private List[] creatingLog; //������ �������� ����� (��������, ����������� ������ ���� �������)
	List hostList=curHostList; //������ ����� ��� ������ ������
	long time = System.currentTimeMillis(); //����� �������� ������ ��� ������� ������� ���������
	volatile PerformFunction func = curFunc; //������� ����������� ������������������

	public IRecovery recovery;

	public TaskInfo(int num, IRecovery recovery) {
		number = num;
		this.recovery = recovery;
		creatingLog = new LinkedList[hostList.size()];
	}
	 public int addPoint(PointInfo p) {
		 p.number = ++processNumber;
		 if (recovery != null)
			 p.number = recovery.CreatePointEvent(p.host, p.number, p.parentNumber);
		 pointList.put(new Integer(processNumber), p);
		 int targetIndex = p.host.index;
		 if (creatingLog[targetIndex]==null) creatingLog[targetIndex]=new LinkedList();
		 creatingLog[targetIndex].add(new Integer(processNumber));
		 return processNumber;
	 }
	 public PointInfo getPoint(int pointnumber) {
		 return (PointInfo)pointList.get(new Integer(pointnumber));
	 }
	 public void removePoint(int pointnumber) {
		 pointList.remove(new Integer(pointnumber));
	 }
	 public void printCreatingLog() {
		for (int l=0; l<creatingLog.length; l++) {
			System.out.print((l+1)+"  ");
			if (creatingLog[l]!=null)
				for (Iterator i=creatingLog[l].iterator(); i.hasNext(); )
	    			System.out.print((Integer)i.next()+" ");
			System.out.println();
		}
	}
	/**
	 * ������� �������� ����������� ���� �� ���������� ����������� �����
	 */
	private XHostInfo getTargetHost(byte[] ipaddr){
		if (testHosts) return getTargetHostByPerf();
		XHostInfo target = null;
		int min = Integer.MAX_VALUE; int ind=0;
		synchronized (hostList) {
			for (Iterator i = hostList.iterator(); i.hasNext(); ind++){
				XHostInfo h = (XHostInfo)i.next();
				if (h.ping<0) continue;
				if (h.pointsNumber < min) {
					min = h.pointsNumber;
					target = h;
					target.index = ind;
				} else if (h.pointsNumber==min && h.ipEquals(ipaddr)) {
					target = h; target.index = ind;
				}
			}
		}
		return target;
	}
	/**
	 * ������� �������� ����������� ����
	 * ���� ��� ���������, ������� ���� � ���������� �������������������
	 * � ������� �� ���� ������� (�����)
	 */
	private XHostInfo getTargetHostByPerf(){
		sortList(hostList, func);
		int ind = 0; XHostInfo target = null;
		synchronized (hostList) {
			for (Iterator i = hostList.iterator(); i.hasNext(); ind++){
				XHostInfo h = (XHostInfo)i.next();
				if (h.ping<0) continue;
				if (h.pointsNumber==0) {h.index=ind; return h;}
			}
			double maxperf = 0.0, perf; ind = 0;
			for (Iterator i = hostList.iterator(); i.hasNext(); ind++){
				XHostInfo h = (XHostInfo)i.next();
				if (h.ping<0) continue;
				perf = h.perform/(h.pointsNumber+1);
				if (perf > maxperf) {
					maxperf = perf;
					target = h; target.index = ind;
				}
			}
		}
		return target;
	}
}
}
/**
 * ���������� � �����
 */
class PointInfo {
	public int number; //����� �����
	public int parentNumber;
	public XHostInfo host; //���� (���������), �� ������� ��� ���������
	public PointInfo(XHostInfo h, int parentNumber){ host=h; this.parentNumber = parentNumber;}
	public byte[] getHostIP() { return host.ipAddress.getAddress();}
}
