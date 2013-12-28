package ergate.dict.imp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专门用于处理国家语委下载的语料的工具
 * 
 * @author en.xu
 * 
 */
public final class DealNationaLangueCora {
	public static final Map<String, String> tagmap;
	static {
		tagmap = new HashMap<String, String>();
		tagmap.put("nt", "t");
		tagmap.put("nh", "nr");
		tagmap.put("wu", "un");
		tagmap.put("ws", "nx");
		tagmap.put("ni", "nt");
		tagmap.put("nl", "s");
		tagmap.put("nd", "f");
		tagmap.put("nnf", "nr");
		tagmap.put("nng", "nr");
	}

	public static void main(String[] args) throws IOException {
		File input = new File("D:\\Document\\Download\\corpus.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(input), "GBK"), 1024 * 10);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(input.getParent(), "new_"
						+ input.getName())), "GBK"), 1024 * 10);

		String line;
		StringBuilder buffer = new StringBuilder();
		int li = 1;
		while ((line = reader.readLine()) != null) {
			buffer.setLength(0);
			line = line.trim();
			if (line.isEmpty())
				continue;
			deal(buffer, line, li++);
			writer.write(buffer.toString());
			writer.newLine();
		}
		reader.close();
		writer.flush();
		writer.close();

	}

	static class Node {
		String word;
		String p;

		@Override
		public String toString() {
			return word + "/" + p;
		}

		public Node(String word, String p) {
			super();
			this.word = word;
			this.p = p;
		}

	}

	private static void deal(StringBuilder buffer, String line, int li) {
		buffer.append(li).append("\t");
		List<Node> list = redIndex(getList(line));
		for (int i = 0; i < list.size() - 1; i++) {
			buffer.append(list.get(i).toString()).append(" ");
		}
		buffer.append(list.get(list.size() - 1).toString()).append(" ");

	}

	private static List<Node> redIndex(List<Node> list) {
		List<Node> lists = new ArrayList<Node>();
		for (int i = 0; i < list.size() - 1; i++) {
			if (list.get(i).p.equals("m") && list.get(i + 1).p.equals("t")) {
				lists.add(new Node(list.get(i).word + list.get(i + 1).word, "t"));
				i++;
			} else {
				lists.add(list.get(i));
			}
		}
		lists.add(list.get(list.size() - 1));
		return lists;
	}

	private static List<Node> getList(String line) {
		List<Node> list = new ArrayList<Node>();
		String[] sts = line.split("\\s+");
		for (int i = 1; i < sts.length - 1; i++) {
			String words;
			String p;
			if (sts[i].startsWith("[") && sts[i].endsWith("]")) {
				words = sts[i].substring(1, sts[i].length() - 1);
				p = sts[i + 1].substring(1);
				i++;
			} else {
				int l = sts[i].lastIndexOf('/');
				words = sts[i].substring(0, l);
				p = sts[i].substring(l + 1);
			}
			if (tagmap.containsKey(p)) {
				p = tagmap.get(p);
			}
			list.add(new Node(words, p));
		}
		return list;
	}

}
