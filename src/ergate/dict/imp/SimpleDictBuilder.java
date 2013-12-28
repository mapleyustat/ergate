package ergate.dict.imp;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ergate.segment.AtomList;
import ergate.segment.Category;
import ergate.segment.Cell;

/**
 * 学习人民日报语料，语料格式参见
 * 
 * @author En.Xu
 * 
 */
public class SimpleDictBuilder implements Closeable {
	/** 不可分割的标记映射 */
	private static final Map<String, Category> ATOM_MAP;

	/** 可在再次分割的单位标记映射 */
	private static final Map<String, Category> SPLIT_MAP;

	/** 句子结束符号 */
	private static final char[] TERMINATOR;

	private static final String SAPCE;
	private static final Set<Character> NUMSET;

	static {
		ATOM_MAP = new HashMap<String, Category>();
		SPLIT_MAP = new HashMap<String, Category>();

		ATOM_MAP.put("w", Category.PUNCTUATION);
		ATOM_MAP.put("t", Category.TIME);
		ATOM_MAP.put("m", Category.NUM);
		ATOM_MAP.put("nx", Category.SINGLE);
		ATOM_MAP.put("nr", Category.PERSON);
		ATOM_MAP.put("nc", Category.CURRENCY);

		// 可以继续拆分的结构
		SPLIT_MAP.put("nt", Category.ORG);
		SPLIT_MAP.put("ns", Category.PLACE);
		SPLIT_MAP.put("nz", Category.JARGON);
		SPLIT_MAP.put("i", Category.IDIOM);
		SPLIT_MAP.put("l", Category.TEMP);

		TERMINATOR = "!.:;?…！。，：；？…()（）﹛﹜〈〉﹝﹞「」‹›〖〗[]{}《》〔〕『』«»【】\"`'|,‘’“”、"
				.toCharArray();
		SAPCE = "[\\s\t ]+";

		NUMSET = new HashSet<Character>();
		String numChars = "零○一二两三四五六七八九十廿百千万亿壹贰叁肆伍陆柒捌玖拾佰仟"
				+ "甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未申酉戌亥" + "0123456789０１２３４５６７８９";
		for (Character c : numChars.toCharArray()) {
			NUMSET.add(c);
		}
	}

	private static class Node {
		/** 是否是结构体开始 */
		public boolean bs;
		public String image;
		/** 类型 */
		public String type;
		public String beType;// 外层结构类型

		@Override
		public String toString() {
			if (bs) {
				if (beType != null)
					return "[" + image + "/" + type + "]" + beType;
				return "[" + image + "/" + type;
			}
			if (beType != null)
				return image + "/" + type + "]" + beType;
			return image + "/" + type;
		}

		public Node(String image, String type, String beType) {
			super();
			this.image = image;
			this.type = type;
			this.beType = beType;
		}

		public Node(String image, String type) {
			super();
			this.image = image;
			this.type = type;
		}

	}

	private File dicts;
	private RamLexicon lex;

	public SimpleDictBuilder(File dicts, RamLexicon lex) {
		super();
		this.dicts = dicts;
		this.lex = lex;
	}

	public SimpleDictBuilder(File dicts) {
		super();
		this.dicts = dicts;
		this.lex = new RamLexicon();
	}

	@Override
	public void close() throws IOException {
		save();
	}

	/** 学习语料库中的一行 */
	public void learn(String line) throws IOException {
		line = line.trim();
		if (line.isEmpty()) {
			return;
		}
		learn(dealPserson(getList(line)), new AtomList(0));
	}

	/**
	 * 处理人名
	 * 
	 * @param sentences
	 * @return
	 */
	private List<Node> dealPserson(List<Node> sentences) {
		LinkedList<Node> list = new LinkedList<Node>();
		StringBuilder bu = new StringBuilder();
		int i = 0;
		Iterator<Node> it = sentences.iterator();
		Node str;
		while (it.hasNext()) {
			str = it.next();
			it.remove();
			if (str.type.equals("nr") && !str.bs && str.beType == null) {// 结构体内部不与合并
				bu.append(str.image);
				i++;
			} else {
				if (bu.length() > 0) {
					list.add(new Node(bu.toString(), "nr"));
					i = 0;
					bu.setLength(0);
				}
				list.add(str);
			}
			if (bu.length() > 0 && i > 1) {
				list.add(new Node(bu.toString(), "nr"));
				i = 0;
				bu.setLength(0);
			}
		}
		return list;
	}

	/**
	 * 转化句子
	 */
	private List<Node> getList(String line) throws IOException {
		LinkedList<Node> sentence = new LinkedList<Node>();
		String[] strs = line.split(SAPCE);
		for (int i = 1; i < strs.length; i++) {
			int s = strs[i].lastIndexOf('/');
			if (s < 0) {
				throw new IOException("word=" + strs[i] + " in stences ["
						+ line + "] has not the '/' character");
			}
			int ss = strs[i].lastIndexOf(']');
			String p = strs[i].substring(0, s);
			String sn;
			String ptype = null;
			if (ss > -1 && s + 1 < ss) {
				sn = strs[i].substring(s + 1, ss);
				ptype = strs[i].substring(ss + 1);
			} else {
				sn = strs[i].substring(s + 1);
			}

			if ("t".equals(sn)) {
				if (!hasNum(p))
					sn = "nTime";// 转换名词性时间
			} else if ("m".equals(sn)) {
				if (!hasNum(p))
					sn = "nNum";// 转换名词性数字
			}
			Node node = new Node(p, sn, ptype);
			// 检查结构开始
			if (p.startsWith("[")) {
				node.image = node.image.substring(1);
				node.bs = true;
			}
			// 检查句子结束
			if (node.type.indexOf('w') > -1) {
				for (char ch : TERMINATOR) {
					if (p.indexOf(ch) > -1) {
						node.type = "se";
					}
				}
			}
			sentence.add(node);
		}
		// 保证最后一个元素是结束
		if (!sentence.isEmpty() && !sentence.getLast().type.equals("se")) {
			sentence.add(new Node(Category.END.name, "se"));
		}
		return sentence;
	}

	private boolean hasNum(String p) {
		for (int i = 0; i < p.length(); i++) {
			if (NUMSET.contains(p.charAt(i)))
				return true;
		}
		return false;
	}

	public void learn(Iterator<String> lines) throws IOException {
		AtomList mlist = new AtomList(0);
		String line;
		while (lines.hasNext()) {
			line = lines.next();
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			learn(dealPserson(getList(line)), mlist);
		}
	}

	private void learn(List<Node> src, AtomList mlist) throws IOException {
		mlist.clear();
		int structsNum = 0;// 结构类型的数量
		mlist.add(new Cell(Category.BEGIN.name, Category.BEGIN, -1));
		int offset = 0;
		// 细粒度学习
		for (Node node : src) {
			if (node.bs)
				structsNum++;
			if ("se".equals(node.type)) {
				if (mlist.size() > 1) {
					mlist.add(new Cell(node.image, Category.END, offset));
					lex.learn(mlist);
				}
				mlist.clear();
				mlist.add(new Cell(Category.BEGIN.name, Category.BEGIN, offset));
			} else {
				mlist.add(new Cell(node.image, gettype(node), offset));
			}
			offset += node.image.length();
		}
		if (structsNum > 0)
			learnMax(src);
		src.clear();
	}

	/**
	 * 最大粒度学习
	 * 
	 * @param src
	 * @param mlist
	 * @throws IOException
	 */
	private void learnMax(List<Node> src) throws IOException {
		ArrayList<Node> source = new ArrayList<Node>(src);
		LinkedList<Cell[]> triad = new LinkedList<Cell[]>();
		Node p;
		StringBuilder buffer = new StringBuilder();
		for (int index = 0; index < source.size() - 1; index++) {
			p = source.get(index);
			if (p.bs) {
				Cell[] cells = new Cell[3];
				cells[0] = getFirst(source, index, buffer);
				cells[1] = getMidle(source, index, buffer);
				cells[2] = getLast(source, cells[1] == null ? -1
						: cells[1].offset, buffer);
				for (int i = 0; i < cells.length; i++) {
					if (cells[i] == null)
						throw new IOException("the some wrong tag near node "
								+ p + " in list " + src);
				}
				triad.add(cells);
			}
		}
		for (Cell[] cells : triad) {
			lex.addRelate(cells[0], cells[1]);
			lex.addRelate(cells[1], cells[0]);
			if (Category.isA(cells[1].type, Category.BEGIN)
					&& Category.isA(cells[1].type, Category.CNWORD)
					&& Category.isA(cells[1].type, Category.END)) {
				lex.addWord(cells[1]);
			}
		}
		triad.clear();
		source.clear();
	}

	/**
	 * 获取结构体开始的下一个元素
	 * 
	 * @throws IOException
	 */
	private Cell getLast(ArrayList<Node> source, int index, StringBuilder buffer)
			throws IOException {
		if (index >= source.size() - 1 || index <= 0) {
			return null;
		}
		index = index + 1;
		Node p = source.get(index);
		if (!p.bs) {
			if ("se".equals(p.type)) {
				return new Cell(p.image, Category.END, 1);
			}
			return new Cell(p.image, gettype(p), 1);
		}
		buffer.setLength(0);
		int deep = 0;
		for (int i = index; i < source.size(); i++) {
			p = source.get(i);
			if (p.bs) {
				deep++;
			}
			if (p.beType != null)
				deep--;
			buffer.append(p.image);
			if (deep == 0) {
				Category type = SPLIT_MAP.get(p.beType);
				if (type == null)
					throw new IOException("could not find tag " + p.beType
							+ " in split_map");
				return new Cell(buffer.toString(), type, i);
			}
		}
		return null;
	}

	/**
	 * 获取结构体本身
	 * 
	 * @throws IOException
	 */
	private Cell getMidle(ArrayList<Node> source, int index,
			StringBuilder buffer) throws IOException {
		buffer.setLength(0);
		int deep = 0;
		Node p;
		for (int i = index; i < source.size(); i++) {
			p = source.get(i);
			if (p.bs) {
				deep++;
			}
			if (p.beType != null)
				deep--;
			buffer.append(p.image);
			if (deep == 0) {
				Category type = SPLIT_MAP.get(p.beType);
				if (type == null)
					throw new IOException("could not find tag " + p.beType
							+ " in split_map");
				return new Cell(buffer.toString(), type, i);
			}
		}
		return null;
	}

	/**
	 * 获取结构体上一个元素
	 * 
	 * @throws IOException
	 */
	private Cell getFirst(ArrayList<Node> source, int index,
			StringBuilder buffer) throws IOException {
		if (index == 0) {
			return new Cell(Category.BEGIN.name, Category.BEGIN, -1);
		}
		Node p = source.get(index - 1);
		String sType = p.beType;
		if (sType == null) {
			if ("se".equals(p.type)) {
				return new Cell(Category.BEGIN.name, Category.BEGIN, -1);
			}
			return new Cell(p.image, gettype(p), 1);
		}
		int deep = 0;
		buffer.setLength(0);
		for (int i = index - 1; i > -1; i--) {
			p = source.get(i);
			if (p.beType != null)
				deep++;
			if (p.bs) {
				deep--;
			}
			buffer.append(p.image);
			if (deep == 0) {
				Category type = SPLIT_MAP.get(sType);
				if (type == null)
					throw new IOException("could not find tag " + sType
							+ " in split_map");
				return new Cell(buffer.toString(), type, 0);
			}
		}
		return null;
	}

	private Category gettype(Node node) {
		Category h = ATOM_MAP.get(node.type);
		if (h != null)
			return h;
		return Category.CNWORD;
	}

	private void save() throws IOException {
		if (lex != null) {
			DataOutput out = new DataOutput(new BufferedOutputStream(
					new FileOutputStream(dicts), 1024 * 10));
			try {
				lex.statics();
				lex.writeThis(out);
			} finally {
				out.close();
			}
		}
	}
}
