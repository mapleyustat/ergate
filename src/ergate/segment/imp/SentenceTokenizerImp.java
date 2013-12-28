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

package ergate.segment.imp;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;

import ergate.segment.AtomList;
import ergate.segment.Category;
import ergate.segment.Cell;
import ergate.segment.SentenceTokenizer;
import ergate.utils.FSM;
import ergate.utils.FSM.ACTION;

/**
 * 
 * 
 * <p>
 * <h1><br>
 * Author: Xu En <br>
 * Date: 2013-6-14 上午10:27:45</h1>
 */
public class SentenceTokenizerImp extends SentenceTokenizer {

	// 中文字符集
	public static final char CHINESE_S;// 起始字符
	public static final char CHINESE_E;// 结束字符

	// 界定符类型(有全半角的以英文半角为主,并且已经排序)
	public static final char[] END_DELIMETER;
	public static final char[] SUB_DELIMETER;
	public static final char[] GAP;

	static {
		CHINESE_S = '\u4e00';
		CHINESE_E = '\u9fa5';
		END_DELIMETER = "!.:;?…。".toCharArray();
		SUB_DELIMETER = "\"`'()|,[]‘’“”、《》「」『』【】〔〕〖〗«»‹›〈〉﹛﹜{}".toCharArray();
		GAP = "\n\u0020\t\r\u000B\b\f\0\1\2".toCharArray();
		Arrays.sort(END_DELIMETER);
		Arrays.sort(SUB_DELIMETER);
		Arrays.sort(GAP);
	}

	// 句子切分所采用的有限状态机
	private FSM fsm;

	// 此次读取的句子开始位置
	private int start;
	// 句子的结束位置
	private int end;

	/** 前向提前查看的大小 */
	protected static final int LOOK_AHEAD = 2;
	// 前向查找的缓冲字符
	private int[] preLook;
	// 前向缓冲区中的可读字符
	private int limit;
	// 输入流是否结束
	private boolean inputEnd;

	private LinkedList<Cell> list;
	private StringBuilder buffer;// 原子单元缓冲区
	private int bufferoffset;

	/**
	 * @param input
	 */
	public SentenceTokenizerImp(Reader input) {
		super(input);
		this.buffer = new StringBuilder();
		this.preLook = new int[LOOK_AHEAD];
		list = new LinkedList<Cell>();
		bufferoffset = 0;
		initFSM();
	}

	/**
	 * 设置有限状态机
	 */
	private void initFSM() {
		fsm = new FSM(0, -1) {
			@Override
			protected int input(final Object[] mes) throws IOException {
				int read = readFromPre();
				mes[0] = read;
				++end;
				return charType(read);
			}
		};
		addAct();
	}

	/**
	 * 返回字符的类型值,有限状态机的输入类型
	 * 
	 * @param ch
	 * @return
	 */
	private int charType(int ch) {
		if (ch <= 0)
			return -1;// 结束状态
		char inc = (char) ch;
		if (Arrays.binarySearch(GAP, inc) >= 0) {
			return 1;// 空白
		}
		if (Arrays.binarySearch(END_DELIMETER, inc) >= 0) {
			return 2;// 句子结尾
		}
		if (Arrays.binarySearch(SUB_DELIMETER, inc) >= 0) {
			return 3;// 子句子结尾
		}
		if (inc <= CHINESE_E && inc >= CHINESE_S) {
			return 4;// 标准中文字符
		}
		return Integer.MAX_VALUE;// 其他未可识别符号
	}

	/**
	 * 添加状态机的执行动作,[state:{0,1,-1}]0-buffer空状态,1-buffer处于满状态,-1-结束<br>
	 */
	private void addAct() {
		fsm.addAction(new ACTION(0, -1) {
			@Override
			public int act(Object mes) throws IOException {
				if (!list.isEmpty()) {
					list.addLast(new Cell(Category.END.name, Category.END, end
							- start - 1));
				}
				return -1;
			}
		});
		fsm.addAction(new ACTION(0, 1) {
			@Override
			public int act(Object mes) throws IOException {
				if (list.isEmpty()) {
					++start;
					return 0;
				}
				list.addLast(new Cell(Category.END.name, Category.END, end
						- start - 1));
				return -1;
			}
		});
		fsm.addAction(new ACTION(0, 2) {
			@Override
			public int act(Object mes) throws IOException {
				list.addLast(new Cell(String.valueOf(Message(mes)),
						Category.BORDER, end - start - 1));
				return -1;
			}
		});
		fsm.addAction(new ACTION(0, 3) {
			@Override
			public int act(Object mes) throws IOException {
				list.addLast(new Cell(String.valueOf(Message(mes)),
						Category.SUB_BORDER, end - start - 1));
				return -1;
			}
		});
		fsm.addAction(new ACTION(0, 4) {
			@Override
			public int act(Object mes) throws IOException {
				list.addLast(new Cell(String.valueOf(Message(mes)),
						Category.CNWORD, end - start - 1));
				return 0;
			}
		});
		fsm.addAction(new ACTION(0, Integer.MAX_VALUE) {
			@Override
			public int act(Object mes) throws IOException {
				buffer.append(Message(mes));
				bufferoffset = end;
				return 1;
			}
		});
		fsm.addAction(new ACTION(1, -1) {
			@Override
			public int act(Object mes) throws IOException {
				list.addLast(new Cell(buffer.toString(), Category.SINGLE, end
						- start - 1));
				buffer.setLength(0);
				list.addLast(new Cell(Category.END.name, Category.END, end
						- start - 1));
				return -1;
			}
		});
		fsm.addAction(new ACTION(1, 1) {
			@Override
			public int act(Object mes) throws IOException {
				list.addLast(new Cell(buffer.toString(), Category.SINGLE, end
						- start - 1));
				buffer.setLength(0);
				list.addLast(new Cell(Category.END.name, Category.END, end
						- start - 1));
				return -1;
			}
		});
		fsm.addAction(new ACTION(1, 2) {
			@Override
			public int act(Object mes) throws IOException {
				if ('.' == Message(mes) && limit > 0
						&& charType(preLook[0]) == Integer.MAX_VALUE) {
					char c = (char) preLook[0];
					if (c < CHINESE_S || c > CHINESE_E) {
						buffer.append('.');
						return 1;
					}
				}
				list.addLast(new Cell(buffer.toString(), Category.SINGLE,
						bufferoffset - start - 1));
				bufferoffset = 0;
				buffer.setLength(0);
				list.addLast(new Cell(String.valueOf(Message(mes)),
						Category.BORDER, end - start - 1));
				return -1;
			}
		});
		fsm.addAction(new ACTION(1, 3) {
			@Override
			public int act(Object mes) throws IOException {
				list.addLast(new Cell(buffer.toString(), Category.SINGLE,
						bufferoffset - start - 1));
				bufferoffset = 0;
				buffer.setLength(0);
				list.addLast(new Cell(String.valueOf(Message(mes)),
						Category.SUB_BORDER, end - start - 1));
				return -1;
			}
		});
		fsm.addAction(new ACTION(1, 4) {
			@Override
			public int act(Object mes) throws IOException {
				list.addLast(new Cell(buffer.toString(), Category.SINGLE,
						bufferoffset - start - 1));
				bufferoffset = 0;
				buffer.setLength(0);
				list.addLast(new Cell(String.valueOf(Message(mes)),
						Category.CNWORD, end - start - 1));
				return 0;
			}
		});
		fsm.addAction(new ACTION(1, Integer.MAX_VALUE) {
			@Override
			public int act(Object mes) throws IOException {
				buffer.append(Message(mes));
				return 1;
			}
		});
	}

	private char Message(Object mes) {
		return (char) (int) (Integer) mes;
	}

	@Override
	public void rest(Reader reader) throws IOException {
		super.rest(reader);
		clear();
		inputEnd = false;
	}

	/**
	 * 清空输入
	 */
	private void clear() {
		list.clear();
		buffer.setLength(0);
		start = end = 0;
		limit = 0;
		bufferoffset = 0;
	}

	/**
	 * 清除句子属性
	 */
	private void clearAtts() {
		list.clear();
		buffer.setLength(0);
		bufferoffset = 0;
		start = end;
	}

	@Override
	public boolean hasNext() throws IOException {
		clearAtts();// 清除属性
		fsm.run();// 执行更新
		return !list.isEmpty();
	}

	/**
	 * 将输入流中的数据读取出来，并将prelook塞满
	 * 
	 * @return
	 * @throws IOException
	 */
	private int readFromPre() throws IOException {
		// 从pre中取出一个字符
		int pre;
		if (limit > 0) {
			pre = this.preLook[0];
			for (int i = 0; i < limit - 1; i++) {
				preLook[i] = preLook[i + 1];
			}
			limit--;
		} else {
			if (!inputEnd) {
				pre = ToDBC(this.input.read());
				if (pre < 0)
					inputEnd = true;
			} else
				pre = -1;
		}
		// 填充缓冲区
		if (!inputEnd) {
			int read;
			while (limit < LOOK_AHEAD) {
				read = ToDBC(this.input.read());
				if (read > 0) {
					preLook[limit++] = read;
				} else {
					inputEnd = true;
					break;
				}
			}
		}

		return pre;
	}

	@Override
	public AtomList getSentence() {
		list.addFirst(new Cell(Category.BEGIN.name, Category.BEGIN, -1));
		return new AtomList(list, start);
	}

	/**
	 * 全角转半角 只针对英文字符
	 * 
	 * @param input
	 * @return 半角字符串
	 */

	private static int ToDBC(int input) {
		if (input > '\uFF00' && input < '\uFF5F') {
			return input - 65248;
		}
		if (input == '\u3000') {
			return ' ';
		}
		return input;
	}

}
