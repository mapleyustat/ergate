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

import java.util.Set;

/**
 * 
 * 链表矩阵，使用行优先存储，<br>
 * 每个元素除却行列坐标外还带有下标，下标按照行从左到右，从上到下标号
 * <p>
 * <h1><br>
 * Author: Xu En <br>
 * Date: 2013-5-22 上午10:29:30</h1>
 */
public class LinkedMatrix<E extends Element> extends Matrix<E> {

	/**前N行元素的迭代器*/
	private class innerXiterator implements XIterator<E> {

		Link cur;
		int index;
		int rowNum;

		/**
		 * 
		 * @param cur 开始元素
		 * @param index cur下标减一
		 * @param rowNum 最大行数
		 */
		public innerXiterator(Link cur, int index, int rowNum) {
			super();
			this.cur = cur;
			this.index = index;
			this.rowNum = rowNum;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			if (cur != null && cur.val.row <= rowNum) {
				try {
					return (E) cur.val;
				} finally {
					cur = cur.next;
					index++;
				}
			}
			return null;
		}

		@Override
		public int index() {
			return index;
		}

	}

	// 内置链表
	private class Link {
		Element val;
		Link next;

		public Link(Element val, Link next) {
			this.val = val;
			this.next = next;
		}
	}

	/**
	 * 头指针,保存有当前矩阵的行数以及列数
	 */
	private final Link head;

	/**
	 * 尾指针
	 */
	private Link tail;

	private int size;

	public LinkedMatrix() {
		tail = head = new Link(new Element(0, 0), null);
	}

	public int rows() {
		return head.val.row;
	}

	public int cols() {
		return head.val.col;
	}

	public int elements() {
		return size;
	}

	@Override
	public XIterator<E> iterator() {
		return new innerXiterator(head.next, -1, Integer.MAX_VALUE);
	}

	/**
	 * 更新矩阵大小
	 * 
	 * @param node
	 */
	private void updateSize(Element node) {
		if (head.val.row < node.row + 1) {
			head.val.row = node.row + 1;
		}
		if (head.val.col < node.col + 1) {
			head.val.col = node.col + 1;
		}
		++size;
	}

	/**
	 * 将此元素插入到链表的最后一个元素中<br>
	 * WARN!此方法可能扰乱链表的内部储存结构，如果 被调用的顺序不是按照元素的行排列顺序调用的话。
	 * 
	 * @param node
	 */
	public void addLast(E node) {
		tail.next = new Link(node, null);
		tail = tail.next;
		updateSize(node);
	}

	/**
	 * 将节点插入到此矩阵中
	 * 
	 * @param node
	 */
	public void insert(E node, boolean replace) {
		Link cur = head.next;
		Link pre = head;
		boolean rep = false;
		while (cur != null) {
			if (node.row <= cur.val.row) {
				if (node.row == cur.val.row) {
					if (node.col == cur.val.col) {
						rep = true;
						break;
					}
					if (node.col < cur.val.col) {
						break;
					}
				} else {
					break;
				}
			}
			pre = cur;
			cur = cur.next;
		}
		replace = replace && rep;
		insert(pre, cur, new Link(node, null), replace);
		if (!replace)
			updateSize(node);
	}

	/**
	 * 在pre~cur之间插入元素，并且决定是否替换当前cur
	 * 
	 * @param pre
	 * @param cur
	 * @param node
	 * @param replace
	 */
	private void insert(Link pre, Link cur, Link node, boolean replace) {
		if (cur == null) {
			pre.next = node;
			tail = node;
			return;
		}
		if (replace) {
			node.next = cur.next;
			cur.next = null;
			pre.next = node;
		} else {
			node.next = cur;
			pre.next = node;
		}
	}

	/**
	 * 获取矩阵的第i行
	 * 
	 * @param node
	 * @param i
	 * @return
	 */
	public XIterator<E> row(int rowNum, XIterator<E> resum) {
		Link cur = head.next;
		int index = -1;
		if (resum != null && (resum instanceof LinkedMatrix.innerXiterator)) {
			innerXiterator xi = (innerXiterator) resum;
			cur = xi.cur;
			index = xi.index;
		}
		while ((cur != null) && (cur.val.row != rowNum)) {
			++index;
			cur = cur.next;
		}
		return new innerXiterator(cur, index, rowNum);
	}

	/**
	 * 截取指定下标的部分元素
	 * 
	 * @param indexs
	 * @return
	 */
	public void retain(Set<Integer> indexs) {
		Link cur = head.next;
		Link pre = head;
		int index = 0;
		size = head.val.row = head.val.col = 0;
		while (cur != null) {
			if (!indexs.contains(index++)) {
				// 移除当前元素
				pre.next = cur.next;
				cur.next = null;
				cur = pre.next;
			} else {
				updateSize(cur.val);
				pre = cur;
				cur = cur.next;
			}
		}
	}
}
