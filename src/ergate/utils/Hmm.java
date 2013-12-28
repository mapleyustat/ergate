package ergate.utils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * HMM模型的算法,包含Viterbi算法
 * 
 * @author en.xu
 * 
 */
public abstract class Hmm<S, O> implements Closeable {

	public abstract class HmmDict {
		/**
		 * 获取O可能的状态集合以及他相应的概率的负对数,<br>
		 * 如果不存在就返回默认值,返回结果大小不能够为空<br>
		 * 结果的长度为sqs的长度
		 * 
		 * @param o
		 * @return
		 */
		protected abstract ArrayList<SNode>[] getAbleTags(List<O> sqs);

		/**
		 * 转移概率的负对数,条件熵
		 * 
		 * @param s
		 * @param s2
		 * @return
		 */
		protected abstract double H(S s, S s2);

		/**
		 * 初始化分布的负对数,即标签的熵
		 * 
		 * @param s
		 * @return
		 */
		protected abstract double H(S s);
	}

	/**
	 * 观察态
	 * 
	 * @author en.xu
	 * 
	 */
	protected final class SNode {
		protected S s;
		protected double value;
		private int pre;

		public SNode(S s, double value) {
			super();
			this.s = s;
			this.value = value;
			this.pre = -1;
		}
	}

	private final HmmDict dict;

	public Hmm(HmmDict dict) {
		this.dict = dict;
	}

	/**
	 * Viterbi算法,对指定的序列进行标记
	 * 
	 * @param o
	 * @return
	 */
	public List<S> tag(List<O> sqs) {
		ArrayList<SNode>[] paths = dict.getAbleTags(sqs);
		SNode temp;
		for (int i = 0; i < paths[0].size(); i++) {
			temp = paths[0].get(i);
			temp.value = dict.H(temp.s) + temp.value;
		}

		ArrayList<SNode> pre;
		SNode temp_pre;
		for (int i = 1; i < sqs.size(); i++) {
			pre = paths[i - 1];
			for (int j = 0; j < paths[i].size(); j++) {
				temp = paths[i].get(j);
				double min = Double.MAX_VALUE;
				double min_temp;
				for (int k = 0; k < pre.size(); k++) {
					temp_pre = pre.get(k);
					min_temp = temp_pre.value + dict.H(temp_pre.s, temp.s);
					if (min_temp < min) {
						min = min_temp;
						temp.pre = k;
					}
				}
				temp.value = temp.value + min;
			}
		}
		pre = paths[sqs.size() - 1];
		double min = Double.MAX_VALUE;
		temp = null;
		for (int k = 0; k < pre.size(); k++) {
			temp_pre = pre.get(k);
			if (temp_pre.value < min) {
				min = temp_pre.value;
				temp = temp_pre;
			}
		}
		assert temp != null : "the code has somthing wrong!";
		LinkedList<S> tags = new LinkedList<S>();
		tags.addFirst(temp.s);
		for (int i = sqs.size() - 2; i > -1; i--) {
			temp = paths[i].get(temp.pre);
			tags.addFirst(temp.s);
		}
		return tags;
	}

}
