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
 * 矩阵，矩阵的行列都从0开始计数 <br>
 * <h1>
 * author En.Xu</h1><br>
 * 2013-6-26 下午5:53:03
 */
public abstract class Matrix<E extends Element> implements XIterable<E> {

	/**
	 * 行数
	 * 
	 * @return
	 */
	public abstract int rows();

	/**
	 * 列数
	 * 
	 * @return
	 */
	public abstract int cols();

	/**
	 * 非空元素的个数
	 * 
	 * @return
	 */
	public abstract int elements();

	/**
	 * 迭代给定的行的元素
	 * 
	 * @param num
	 * @param resum 从给定的加速器后面查找,必须出自同一对象否则结果不可预料
	 * @return
	 */
	public abstract XIterator<E> row(final int num,XIterator<E> resum);

	/**
	 * 截取指定序号的元素
	 * 
	 * @param indexs
	 */
	public abstract void retain(Set<Integer> indexs);

	/**
	 * 将元素插入到最后一行的最后一个元素里，<br>
	 * 注意调用顺序，按照元素的排列顺序调用。
	 * 
	 * @param node
	 */
	public abstract void addLast(E node);

	/**
	 * 将元素插入矩阵<br>
	 * 取代为true并且这个元素已经存在的话将采用替换的方式<br>
	 * 取代为false并且这个元素已经存在那么将这个元素与以前的元素并列保存在同一矩阵位置当做副本,<br>
	 * 此时形成了一个三维的立体矩阵,即同一个位置存在多个元素
	 * 
	 * @param replace
	 * @param node
	 */
	public abstract void insert(E node, boolean replace);
	

	public String toString() {
		StringBuilder builder = new StringBuilder("Matrix [");
		builder.append("row=").append(rows()).append(",col=").append(cols());
		builder.append(",num=").append(elements()).append("]\n");
		for (int i = 0; i < cols(); i++) {
			builder.append("\t").append("-").append(i).append("-");
		}
		E cur;
		XIterator<E> it = iterator();
		int rowN = -1, colN = 0;
		while ((cur = it.next()) != null) {
			for (; rowN < cur.row; rowN++) {
				builder.append("\n").append(rowN + 1).append("|*\t");
				colN = 0;
			}
			for (int j = colN; j < cur.col; j++) {
				builder.append("\t");
			}
			builder.append(cur);
			colN = cur.col;
		}
		builder.append("\n");
		for (int i = 0; i < cols(); i++) {
			builder.append("\t").append("-*-");
		}
		builder.append("\n");
		return builder.toString();
	}
	
	public String toHTML() {
		StringBuilder html = new StringBuilder();
		html.append("<table border=\"1\" width=\"100%\">");
		html.append("<tr>");
		html.append("<td width=\"5%\"></td>");
		int width = cols();
		double size = 95.0 / width;
		for (int i = 0; i < width; i++) {
			html.append("<td width=\"" + size + "%\" bgcolor=\"#99CCFF\">" + i
					+ "</td>");
		}
		html.append("</tr>");
		E cur = null;
		E curp = null;
		XIterator<E> it = iterator();
		int rowN = -1, colN = 0;
		int row, col;
		StringBuilder builder = new StringBuilder();
		while (true) {
			if (cur == null) {
				cur = it.next();
				if (cur == null)
					break;
			}
			row = cur.row;
			col = cur.col;
			builder.append(getIString(String.valueOf(cur))).append("|");
			while ((curp = it.next()) != null) {
				if (curp.col == col && curp.row == row) {
					builder.append(getIString(String.valueOf(curp)))
							.append("|");
				} else {
					break;
				}
			}
			cur = curp;
			boolean first = true;
			while (rowN < row) {
				if (rowN > -1) {
					if (first) {
						for (int j = colN; j < width; j++) {
							html.append("<td width=\"" + size
									+ "%\">&nbsp</td>");
						}
						html.append("</tr>");
						colN = 0;
					} else {
						for (int j = 0; j < width; j++) {
							html.append("<td width=\"" + size
									+ "%\">&nbsp</td>");
						}
						html.append("</tr>");
					}
				}
				rowN++;
				first = false;
				html.append("<tr>")
						.append("<td width=\"5%\">" + rowN + "</td>");
			}
			while (colN < col) {
				html.append("<td width=\"" + size + "%\">&nbsp</td>");
				colN++;
			}
			html.append("<td width=\"" + size + "%\"><a>");
			builder.deleteCharAt(builder.length() - 1);
			html.append(builder.toString()).append("</a></td>");
			builder.setLength(0);
			colN++;
		}
		html.append("</table>");
		return html.toString();
	}

	private String getIString(String image) {
		if (image.startsWith("<") && image.endsWith(">")) {
			return image.substring(1, image.length() - 1);
		}
		return image;
	}

}
