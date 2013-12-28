/***
 *   Copyright (c) 2012 Xu En
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  Substantial portions of this code were developed by the Cyc project
 *  and by Cycorp Inc, whose contribution is gratefully acknowledged.
 */

package ergate.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * 采用链表形式储存的图形 <br>
 * <h1>
 * author En.Xu</h1><br>
 * 2013-6-26 下午9:25:42
 */
public class LinkedMatrixGraph<E extends Edge> extends LinkedMatrix<E>
		implements Graph<E> {

	public List<Integer> getShortPath(int start, int end) {
		int path_num = Math.max(cols(), rows());
		// 经过的点
		int[] curs = new int[path_num];
		Arrays.fill(curs, -1);
		// 当前权值
		double[] values = new double[path_num];
		Arrays.fill(values, Double.MAX_VALUE);
		// 是否被处理过
		boolean[] processed = new boolean[path_num];
		Arrays.fill(processed, false);

		values[start] = 0;
		// 最短路径算法
		int nums = 0;// 被处理的点的数量
		double min = 0;// 最小值
		int pos = start;
		XIterator<E> it = null;
		E node;
		do {
			// 取出矩阵第i行(即点i可以到达的点),变更values
			it = row(pos, null);
			while ((node = it.next()) != null) {
				if (values[node.col] > (min + node.weight)) {
					values[node.col] = min + node.weight;
					curs[node.col] = pos;
				}
			}
			processed[pos] = true;
			nums++;// 处理的一个
			min = Double.MAX_VALUE;
			pos = -1;
			for (int i = 0; i < path_num; i++) {
				if ((!processed[i]) && values[i] < min) {
					min = values[i];
					pos = i;
				}
			}
			if (pos == end || pos == -1 || min == Double.MAX_VALUE) {
				break;
			}
		} while (nums < path_num);
		// 根据curs[]返回结果
		LinkedList<Integer> result = new LinkedList<Integer>();
		for (pos = end; pos != -1; pos = curs[pos]) {
			result.addFirst(pos);
		}
		return result;
	}

}
