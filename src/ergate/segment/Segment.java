package ergate.segment;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import ergate.utils.Edge;
import ergate.utils.LinkedMatrix;
import ergate.utils.LinkedMatrixGraph;
import ergate.utils.Matrix;
import ergate.utils.XIterator;

/**
 * 分词器主入口,非线程安全
 * 
 * @author en.xu
 * 
 */
public class Segment implements Closeable {
	private boolean debugOn;
	private StringBuilder html;

	/**
	 * 所有的词语识别器
	 */
	private LinkedList<Recognizer> recognitions;

	/**
	 * 所有的标注器
	 */
	private LinkedList<Tagger> taggers;

	/**
	 * 词典
	 */
	private final Dictionary dict;

	public Segment(Dictionary dict) {
		this.recognitions = new LinkedList<Recognizer>();
		this.taggers = new LinkedList<Tagger>();
		this.dict = dict;
	}

	/**
	 * 添加词语识别器
	 * 
	 * @param recognition
	 */
	public void addRecoger(Recognizer recognition) {
		this.recognitions.addLast(recognition);
	}

	/**
	 * 添加标注器
	 * 
	 * @param tagger
	 */
	public void addTagger(Tagger tagger) {
		this.taggers.addLast(tagger);
	}

	public void setDebug(boolean on) {
		this.debugOn = on;
	}

	public String getDebugResult() {
		return html.toString();
	}

	// 添加第一次切分结果
	private void appendHead(AtomList sentence) {
		html.setLength(0);
		StringBuilder src = new StringBuilder();
		for (int i = 1; i < sentence.size() - 1; i++) {
			src.append(sentence.get(i).image);
		}
		html.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
		html.append("<title>分词结果</title></head><body bgcolor=\"#CCFF99\">原文内容：");
		html.append("<table border=\"1\" width=\"100%\"><tr><td width=\"100%\">");
		html.append(src.toString());
		html.append("</td></tr></table>");
		// 显示经过原子分词后的结果
		html.append("<p>进行原子分词后的结果：");
		html.append(sentence.ToHTML());
	}

	// 添加最终结果
	private void appendTail(AtomList sentence) {
		html.append("<p>标注结果：");
		html.append(sentence.ToHTML());
		html.append("</body></html>");
	}

	/**
	 * 切分句子,返回最终切分结果
	 * 
	 * @param sentence
	 * @return
	 * @throws IOException
	 */
	public AtomList split(AtomList sentence) throws IOException {
		if (!sentence.check()) {
			if (sentence.size() == 2) {
				return sentence;
			}
			throw new IllegalArgumentException(
					"the sentence is not suit for the standard!");
		}
		if (debugOn) {
			appendHead(sentence);
		}

		// 识别新词并进行分词
		sentence = regWord(sentence);
		// 标注
		if (debugOn) {
			appendTail(sentence);
		}
		return tag(sentence);
	}

	/**
	 * 进行标注
	 * 
	 * @param newlist
	 * @return
	 * @throws IOException
	 */
	private AtomList tag(AtomList newlist) throws IOException {
		for (Tagger tagger : taggers) {
			newlist = tagger.tag(newlist);
		}
		return newlist;
	}

	private AtomList regWord(AtomList sentence) throws IOException {
		Matrix<Node> matrix = new LinkedMatrix<Node>();
		// 加入基本单元
		addBasicWord(sentence, matrix);
		// 添加可能的词
		addPossibleWord(sentence, matrix);
		// System.out.println(matrix);
		// 选取最优的分词方式
		getBestPath(matrix);
		return toAtomList(matrix, sentence.offset);
	}

	private AtomList toAtomList(Matrix<Node> matrix, int offset) {
		AtomList result = new AtomList(matrix.elements(), offset);
		XIterator<Node> it = matrix.iterator();
		Node node;
		while ((node = it.next()) != null) {
			result.add(node.cell);
		}
		return result;
	}

	/**
	 * 添加基于词典的基本词
	 * 
	 * @param sentence
	 * @param matrix
	 * @throws IOException
	 */
	private void addBasicWord(AtomList sentence, Matrix<Node> matrix)
			throws IOException {
		int maxlen = dict.maxWordLen();
		int len = sentence.size();
		matrix.addLast(new Node(0, 1, sentence.get(0)));
		StringBuilder buffer = new StringBuilder();
		int j;
		String temp;
		ArrayList<Category> typeBuffer = new ArrayList<Category>(5);
		for (int i = 1; i < len - 2; i++) {
			matrix.addLast(new Node(i, i + 1, sentence.get(i)));
			buffer.append(sentence.get(i).image);
			j = i + 1;
			while ((buffer.length() < maxlen) && (j < len - 1)) {
				buffer.append(sentence.get(j).image);
				temp = buffer.toString();
				if (dict.isInDict(temp, typeBuffer)) {
					for (Category type : typeBuffer) {
						matrix.addLast(new Node(i, j + 1, new Cell(temp, type,
								sentence.get(i).offset)));
					}
					typeBuffer.clear();
				}
				j++;
			}
			buffer.setLength(0);
		}
		matrix.addLast(new Node(len - 2, len - 1, sentence.get(len - 2)));
		matrix.addLast(new Node(len - 1, len, sentence.get(len - 1)));
	}

	/**
	 * 
	 * 进行命名体识别
	 * 
	 * @param sentence
	 * @throws IOException
	 */
	private void addPossibleWord(AtomList sentence, Matrix<Node> matrix)
			throws IOException {
		Iterator<Recognizer> it = recognitions.iterator();
		while (it.hasNext()) {
			it.next().recognized(matrix, sentence);
		}
	}

	// 显示初次生成的分词图表
	private void appendFisrtMartix(Matrix<Node> matrix) {
		html.append("<p>初次生成的分词图表：");
		html.append(matrix.toHTML());
	}

	/**
	 * 选取最优分词
	 * 
	 * @param matrix
	 * @return
	 * @throws IOException
	 */
	private void getBestPath(Matrix<Node> matrix) throws IOException {
		if (debugOn) {
			appendFisrtMartix(matrix);
		}
		LinkedMatrixGraph<Edge> graph = new LinkedMatrixGraph<Edge>();
		XIterator<Node> p_it, c_it;
		Node Pnode, Nnode;
		p_it = matrix.iterator();
		double distance;
		while ((Pnode = p_it.next()) != null) {
			c_it = matrix.row(Pnode.col, p_it);
			while ((Nnode = c_it.next()) != null) {
				distance = dict.distance(Pnode.cell, Nnode.cell);
				// System.err.println(Pnode.cell + "====" +
				// Nnode.cell+"["+distance+"]");
				graph.addLast(new Edge(p_it.index(), c_it.index(), distance));
			}
		}
		List<Integer> list = graph.getShortPath(0, matrix.elements() - 1);
		matrix.retain(new TreeSet<Integer>(list));
		list.clear();
	}

	@Override
	public void close() throws IOException {
		try {
			for (Recognizer r : recognitions) {
				r.close();
			}
			recognitions.clear();
		} finally {
			try {
				for (Tagger tagger : taggers) {
					tagger.close();
				}
				taggers.clear();
			} finally {
				if (dict != null)
					dict.close();
			}

		}
	}

}
