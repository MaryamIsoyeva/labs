package parcs;
import java.util.*;

/**
 * ����� ���������� ��� ����������� ���������� ��� ������� AM
 */

public class AMInfo {
	/**
	 * ������� ������
	 */
	public task curtask;
	/**
	 * ����� ����� � ������������ ������
	 */
	public channel parent;
	/**
	 * ������ ���� ������� �� ���� �����, ����� �������������
	 */
	public ArrayList channels;
	/**
	 * ������ ��������� ���� �����
	 */
	public HashMap data;

	/**
	 *
	 */
	public AMInfo() {
		this(null, null, null, null);
	}

	/**
	 *
	 */
	public AMInfo(task curtask, channel parent) {
		this(curtask, parent, null, null);
	}

	/**
	 *
	 */
	public AMInfo(task curtask, channel parent, ArrayList channels, HashMap data) {
		this.curtask = curtask;
		this.parent = parent;
		this.channels = channels;
		this.data = data;
	}

	public point createPoint()
	{
		if (parent == null)
			return curtask.createPoint(0);
		return curtask.createPoint(parent.from);
	}

}