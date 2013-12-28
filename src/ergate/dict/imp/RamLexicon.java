package ergate.dict.imp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import ergate.segment.AtomList;
import ergate.segment.Category;
import ergate.segment.Cell;
import ergate.segment.Dictionary;
import ergate.utils.NumUtils;

/**
 * 内存词典
 * 
 * @author En.Xu
 * 
 */
public class RamLexicon implements Dictionary {
	protected static final int K_LIMIT = 3;// KN系数

	protected static class Word {
		public final int id;
		public Map<Integer, Long> relateFreqs;// 关联频率
		public Map<Integer, Long> tagFreqs;// 某个标签对应的频率
		public long FreqR;// 词频,这里不针对任何标签进行统计计算
		public long LNum;// 左边结合的不同的组合数量
		public double weight;// 权重系数
		public long freq;// 这个词的词频

		public Word(int id) {
			this.id = id;
			relateFreqs = new HashMap<Integer, Long>();
			tagFreqs = new HashMap<Integer, Long>();
			FreqR = 0l;
			LNum = 0l;
			weight = 0.0;
			freq = 0l;
		}

	}

	// 二元词表不同的词组的数量
	protected long wordsPairNum;
	/** kn中的d */
	protected double[] D;
	// 总共的词数
	protected long totalWords;
	// 字符串对应的ID值(*)
	protected Map<String, Word> wordMap;
	// 最大的词组长度
	protected int maxWordlen;
	// 使用的特殊分类标记
	protected Set<Category> specal;

	public RamLexicon() {
		wordsPairNum = 0l;
		D = new double[K_LIMIT];
		Arrays.fill(D, 1.0);
		totalWords = 0l;
		wordMap = new HashMap<String, RamLexicon.Word>();
		maxWordlen = 0;
		specal = new TreeSet<Category>();
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public int maxWordLen() {
		return maxWordlen;
	}

	@Override
	public boolean isInDict(String image, ArrayList<Category> types)
			throws IOException {
		if (wordMap.containsKey(image)) {
			types.add(Category.CNWORD);
			return true;
		}
		return false;
	}

	@Override
	public double distance(Cell p, Cell n) throws IOException {
		String pre = getSymble(p);
		String next = getSymble(n);
		return NumUtils.H(prob(pre, next));
	}

	protected double D(long freq) {
		if (freq <= 0)
			return 0.0;
		if (freq > K_LIMIT)
			return D[K_LIMIT - 1];
		return D[(int) freq - 1];
	}

	/** 计算条件概率 */
	protected double prob(String pre, String next) {
		Word wordP = wordMap.get(pre);
		Word wordN = wordMap.get(next);

		long ocFreq = 0l;
		if (wordP != null && wordN != null) {
			Long temp = wordP.relateFreqs.get(wordN.id);
			ocFreq = temp != null ? temp : 0l;
		}

		double LWeight = 0.0;
		long preRfreq = 0l;
		if (wordP != null) {
			LWeight = wordP.weight;
			preRfreq = wordP.FreqR;
		}

		long lnum = 0l;
		if (wordN != null) {
			lnum = wordN.LNum;
		}

		double val = 0.0;
		if (wordsPairNum > 0 && preRfreq > 0) {
			val = (ocFreq - D(ocFreq) + LWeight * lnum / wordsPairNum)
					/ preRfreq;
		}

		return val;
	}

	private String getSymble(Cell p) {
		if (Category.isA(p.type, Category.CNWORD)) {
			return p.image;
		}
		if (specal.contains(p.type)) {
			return p.type.name;
		}
		for (Category type : specal) {
			if (Category.isA(p.type, type)) {
				return type.name;
			}
		}
		return Category.DEFAULT.name;
	}

	/** 进行学习总结 */
	public void statics() throws IOException {
		// 清除以前的数据
		D = new double[K_LIMIT];
		wordsPairNum = 0l;
		Arrays.fill(D, 1.0);
		Map<Integer, Object[]> idMap = new HashMap<Integer, Object[]>();
		Object[] obj;
		long[] rnum;
		for (Word word : wordMap.values()) {
			word.FreqR = 0l;
			word.LNum = 0l;
			word.weight = 0.0;
			rnum = new long[K_LIMIT];
			obj = new Object[] { word, rnum };
			Arrays.fill(rnum, 0l);
			idMap.put(word.id, obj);
		}
		long[] nk = new long[K_LIMIT + 1];
		Arrays.fill(nk, 0l);
		// 计算D的取值
		Word w;
		for (Object[] oba : idMap.values()) {
			w = (Word) oba[0];
			rnum = (long[]) oba[1];
			for (Entry<Integer, Long> entry : w.relateFreqs.entrySet()) {
				((Word) ((Object[]) idMap.get(entry.getKey()))[0]).LNum++;
				long val = entry.getValue();
				wordsPairNum++;
				w.FreqR += val;
				if (val < K_LIMIT + 2)
					nk[(int) val - 1]++;

				if (val < K_LIMIT)
					rnum[(int) val - 1]++;
				else
					rnum[K_LIMIT - 1]++;
			}
		}

		long sum = nk[0]+2*nk[1];
		double Y = 0;
		if (sum > 0)
			Y = nk[0] * 1.0 / sum;
		for (int i = 0; i < K_LIMIT; i++) {
			if (nk[i] > 0)
				D[i] = (i+1) - (i + 2) * Y * nk[i + 1] / nk[i];
		}

		for (Object[] oba : idMap.values()) {
			w = (Word) oba[0];
			rnum = (long[]) oba[1];
			for (int i = 0; i < K_LIMIT; i++) {
				w.weight += (rnum[i] * D[i]);
			}
		}
		idMap.clear();
	}

	/** 学习一个标准句子的切分方式 */
	public void learn(AtomList list) throws IOException {
		if (!list.check()) {
			throw new IOException("the list is not the standard list!>" + list);
		}
		Cell p, n;
		Word wp, wn;
		for (int i = 0; i < list.size() - 1; i++) {
			p = list.get(i);
			n = list.get(i + 1);
			wp = getWord(p);
			wn = getWord(n);
			addWord(p, wp);
			addRelate(p, wp, n, wn);
		}
		p = list.get(list.size() - 1);
		wp = getWord(p);
		addWord(p, wp);
	}

	public void addRelate(Cell first, Cell next) throws IOException {
		Word p = getWord(first);
		Word n = getWord(next);
		addRelate(first, p, next, n);
	}

	public void addWord(Cell cell) throws IOException {
		Word w = getWord(cell);
		addWord(cell, w);
	}

	private void addRelate(Cell p, Word wp, Cell n, Word wn) {
		Long val = wp.relateFreqs.get(wn.id);
		if (val == null)
			val = 0l;
		val++;
		wp.relateFreqs.put(wn.id, val);
	}

	private void addWord(Cell p, Word wp) {
		wp.freq++;
		totalWords++;
		if (maxWordlen < p.image.length())
			maxWordlen = p.image.length();
	}

	private Word getWord(Cell word) {
		String sym = word.image;
		if (!Category.isA(word.type, Category.CNWORD)) {
			sym = word.type.name;
			specal.add(word.type);
		}
		Word w = wordMap.get(sym);
		if (w == null) {
			w = new Word(wordMap.size());
			wordMap.put(sym, w);
		}
		return w;
	}

	public void writeThis(DataOutput out) throws IOException {
		out.writeVInt(K_LIMIT);
		for (int i = 0; i < K_LIMIT; i++) {
			out.writeDouble(D[i]);
		}
		out.writeVInt(maxWordlen);
		out.writeVLong(totalWords);
		out.writeVLong(wordsPairNum);
		out.writeInt(specal.size());
		for (Category cays : specal) {
			out.writeVInt(cays.code);
		}
		out.writeInt(wordMap.size());
		for (Entry<String, Word> et : wordMap.entrySet()) {
			out.writeString(et.getKey());
			writeWord(et.getValue(), out);
		}

	}

	public void load(DataInput in) throws IOException {
		int size = in.readVInt();
		if (size != K_LIMIT)
			throw new IOException("this dict is not suit for this version!");
		for (int i = 0; i < K_LIMIT; i++) {
			D[i] = in.readDouble();
		}
		maxWordlen = in.readVInt();
		totalWords = in.readVLong();
		wordsPairNum = in.readVLong();
		size = in.readInt();
		for (int i = 0; i < size; i++) {
			specal.add(Category.findBycode(in.readVInt()));
		}
		size = in.readInt();
		String key;
		for (int i = 0; i < size; i++) {
			key = in.readString();
			wordMap.put(key, readWord(in));
		}
	}

	private static void writeWord(Word word, DataOutput out) throws IOException {
		out.writeVInt(word.id);
		out.writeVLong(word.FreqR);
		out.writeVLong(word.freq);
		out.writeVLong(word.LNum);
		out.writeDouble(word.weight);
		out.writeInt(word.relateFreqs.size());
		for (Entry<Integer, Long> et : word.relateFreqs.entrySet()) {
			out.writeVInt(et.getKey());
			out.writeVLong(et.getValue());
		}
		out.writeInt(word.tagFreqs.size());
		for (Entry<Integer, Long> et : word.tagFreqs.entrySet()) {
			out.writeVInt(et.getKey());
			out.writeVLong(et.getValue());
		}
	}

	private static Word readWord(DataInput in) throws IOException {
		Word word = new Word(in.readVInt());
		word.FreqR = in.readVLong();
		word.freq = in.readVLong();
		word.LNum = in.readVLong();
		word.weight = in.readDouble();
		int size = in.readInt();
		int temp;
		for (int i = 0; i < size; i++) {
			temp = in.readVInt();
			word.relateFreqs.put(temp, in.readVLong());
		}
		size = in.readInt();
		for (int i = 0; i < size; i++) {
			temp = in.readVInt();
			word.tagFreqs.put(temp, in.readVLong());
		}
		return word;
	}

	@Override
	public String toString() {
		return "RamLexicon [D=" + Arrays.toString(D) + "]";
	}

	
}
