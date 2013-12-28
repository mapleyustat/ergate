package ergate.dict.imp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

public class FileLinesIterotor implements Iterator<String>, Closeable {

	private File dir;
	private String corporaEncoding;

	LinkedList<BufferedReader> list;

	Iterator<BufferedReader> it;
	String line;
	BufferedReader nr;

	public FileLinesIterotor(File dir, String corporaEncoding)
			throws IOException {
		super();
		this.dir = dir;
		this.corporaEncoding = corporaEncoding;
		init();
	}

	private void init() throws IOException {
		File[] corporas = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile()
						&& pathname.getName().toLowerCase().endsWith(".txt"))
					return true;
				return false;
			}
		});
		list = new LinkedList<BufferedReader>();
		for (File f : corporas) {
			list.add(new BufferedReader(new InputStreamReader(
					new FileInputStream(f), corporaEncoding), 1024 * 10));
		}
		it = list.iterator();

	}

	@Override
	public boolean hasNext() {
		line = null;
		do {
			if (nr == null && it.hasNext()) {
				nr = it.next();
			}
			if (nr == null) {
				return false;
			}
			try {
				line = nr.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line != null) {
				break;
			} else
				nr = null;
		} while (true);

		return line != null;
	}

	@Override
	public String next() {
		return line;
	}

	@Override
	public void remove() {

	}

	@Override
	public void close() throws IOException {
		if (list != null) {
			for (BufferedReader reader : list)
				reader.close();
		}
	}
	
	



}
