package parcs;

import java.net.*;
import java.io.*;

/**
 * ��������� ������� ��� �������� ��������� � ��������� jar-������
 */
public class JarClassLoader extends URLClassLoader {
    private static JarClassLoader loader;

    /**
     * ������� JarClassLoader ��� ���������� url.
     */
    public JarClassLoader(URL url) {
        super(new URL[] { url });
    }
	/**
	 * ������� JarClassLoader ��� ���������� �����.
	 */
	public JarClassLoader(String filename) {
		this(filenameToURL(filename));
		String s=new File(filename).getParent();
		//System.out.println(s+File.separator);
		if (s!=null)
			addURL(filenameToURL(s+File.separator));
	}

	private static URL filenameToURL(String filename) {
		try { return new URL("file:" + filename);
		} catch (MalformedURLException e) {
			System.err.println("JarClassLoader. Wrong file name:"+filename);
			return null;
		}
	}

/*	protected Class loadClass(String name, boolean resolve)
					   throws ClassNotFoundException {
		System.out.print(name);
		URL[] urls = getURLs();
		for (int i=0; i<urls.length; i++) System.out.print(" "+urls[i]);
	    Class cl = super.loadClass(name, true);
		System.out.println(" "+cl);
		return cl;
	}*/

    /**
     * Adds the jar file with the following url into the class loader. This can be
     * a local or network resource.
     * @param url The url of the jar file i.e. http://www.xxx.yyy/jarfile.jar
     *            or file:c:\foo\lib\testbeans.jar
     */
    public void addJarFile(URL url) { addURL(url); }

    /**
     * Adds a jar file from the filesystems into the jar loader list.
     * @param jarfile The full path to the jar file.
     */
	public void addJarFile(String jarfile)  {
		addURL(filenameToURL(jarfile));
		String s=new File(jarfile).getParent();
		//System.out.println(s+File.separator);
		if (s!=null)
			addURL(filenameToURL(s+File.separator));
	}

	/**
	 * ��������� ���� � ������������ ���������� loader, ��� ������� loader, ���� ��
	 * ��� �� ������
	 * @param jarfile ��� jar-�����
	 */
	public static JarClassLoader addJarFileToLoader(String jarfile)  {
		if (loader == null)
			loader = new JarClassLoader(jarfile);
			else loader.addJarFile(jarfile);
		return loader;
	}

    /**
     * ���������� ����������� ��������� ����������
     */
    public static JarClassLoader getLoader() { return loader; }
    /**
     * ������������� ����������� ��������� ����������
     */
    public static void setLoader(JarClassLoader cl) { loader = cl; }
}
