//rmi-��������� �������
import java.rmi.*;
import java.util.*;

public interface ParcsLauncherInt extends Remote{

	/**
	 * �������� �� ������ ���� ������ ��� ���������
	 * @param filename ��� �����
	 * @param buf ���������� �����
	 */
	void saveInputFile(String filename, byte[] buf) throws RemoteException;

	/**
	 * ��������� � ������� ���� ����������
	 * @param filename ��� �����
	 * @return ���������� �����
	 */
	byte[] loadOutputFile(String filename) throws RemoteException;

	/**
	 * ��������� ����� �� jar-������, ���������� �������� ��������� � ������� buf,
	 * ��� ������ ����������� �� ����� ��������� META-INF/manifest.mf, ������������
	 * ������ �� ������ hosts_list
	 * @param filename ��� jar-����� (��� �����)
	 * @param buf ���������� jar-�����
	 * @param hosts_list ������ �����. ���� null, �� ������������ ������ ������ ��
	 *   �������
	 * @param async ���� true, ������� ������������, �� ��������� ���������� ������
	 */
	void executeClass(String filename, byte[] buf, List hosts_list, boolean async) throws RemoteException;
	/**
	 * ��������� ����� �� jar-������, ���������� �������� ���������
	 * � ������� buf, ��� ������ ����������� �� ����� ���������
	 * �� ��, ��� executeClass(filename, buf, null, false)
	 */
	void executeClass(String filename, byte[] buf) throws RemoteException;
}
