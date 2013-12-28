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

import java.io.IOException;

/**
 *
 * 扩展的迭代器
 * <p><h1><br> 
 * Author: Xu En
 * <br>
 * Date: 2013-5-22 上午10:19:01</h1>
 */
public interface XIterator<T> {
	
	/**
	 * 获取当前的元素如果没有则返回null,此时迭代停止
	 * 
	 * @return
	 * @throws IOException
	 */
	T next();

	/**
	 * 必须在调用了next()之后使用，返回的是元素在列表中的顺序
	 * 
	 * @return
	 * @throws IOException
	 */
	int index();

	
	
}
