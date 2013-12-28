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

/**
 * 矩阵中的元素 <br>
 * <h1>
 * author En.Xu</h1><br>
 * 2013-6-26 下午5:47:48
 */
public class Element {
	
	/** 行 */
	public int row;

	/** 列 */
	public int col;
	
	/**
	 * @param row
	 * @param col
	 * @param val
	 */
	public Element(int row, int col) {
		super();
		this.row = row;
		this.col = col;
	}


	@Override
	public String toString() {
		return String.valueOf("*_^");
	}

}
