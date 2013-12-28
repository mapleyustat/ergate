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
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 有限状态机
 * <p>
 * <h1><br>
 * Author: Xu En <br>
 * Date: 2013-6-14 下午12:28:59</h1>
 */
public abstract class FSM {

	/** 有限状态机执行的动作 */
	public static abstract class ACTION {
		/** 行动前所处状态 */
		public final int state;
		/** 当前输入的类型 */
		public final int inputType;

		/**
		 * @param state
		 * @param inputType
		 */
		public ACTION(int state, int inputType) {
			super();
			this.state = state;
			this.inputType = inputType;
		}

		/**
		 * 执行更新动作
		 * 
		 * @param curState
		 *            当前状态
		 * @param type
		 *            输入类型
		 * @param mes
		 *            附带消息
		 * @return 更新后的状态
		 */
		public abstract int act(Object mes) throws IOException;
	}

	private Map<Long, ACTION> actors;

	private final int begin;

	private final int end;

	/**
	 * @param actors
	 * @param begin
	 * @param end
	 */
	public FSM(int begin, int end) {
		super();
		this.actors = new HashMap<Long, FSM.ACTION>();
		this.begin = begin;
		this.end = end;
	}

	/**
	 * 启动状态机
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		int state = begin;
		int type;
		final Object[] mes = new Object[1];
		while (state != end) {
			type = input(mes);
			//如果此处抛出空指针异常，说明程序存在返回不存在的类型以及状态，需要修改添加的动作程序
			state = actors.get(ID(state, type)).act(mes[0]);
		}
	}

	/**
	 * @param state
	 * @param type
	 * @return
	 */
	private long ID(int state, int type) {
		return (((long) state) << 32) | (((long) type) & 0xFFFFFFFFl);
	}

	public void addAction(ACTION action) {
		this.actors.put(ID(action.state, action.inputType), action);
	}

	/**
	 * 获取输入
	 * 
	 * @param mes
	 *            附加消息，这是一个大小为一的数组,由调用者设置需要传递的参数
	 * 
	 * @return
	 */
	protected abstract int input(final Object[] mes) throws IOException;

}
