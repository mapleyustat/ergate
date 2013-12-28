package ergate.segment.imp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ergate.segment.AtomList;
import ergate.segment.Category;
import ergate.segment.Cell;
import ergate.segment.Node;
import ergate.segment.Recognizer;
import ergate.utils.FSM;
import ergate.utils.FSM.ACTION;
import ergate.utils.Matrix;

/**
 * 数字相关的识别器<br>
 * 数字，日期,货币
 * 
 * @author en.xu
 * 
 */

public class NumAboutRecognizer implements Recognizer {

	// 状态机的可能状态
	private static final int begin_state = 0;
	private static final int end_state = -1;
	private static final int with_prfix_num_state = 1;
	private static final int no_prfix_num_state = 2;

	// 状态机的可能输入类型
	private static final int end_type = -1;
	private static final int prfix_type = 1;
	private static final int num_type = 2;
	private static final int suffix_type = 3;
	private static final int unknow_type = 4;

	private FSM fsm;
	private int readindex;
	private int suggestNextIndex;
	private AtomList aList;
	private Matrix<Node> words;
	private Category prfixType;// 前缀类型
	private Category numType;// 数字类型
	private String prfix;// 前缀
	private StringBuilder builder = new StringBuilder();// 数字缓冲
	private int prfix_offset_l = 0;// 前缀在基本单元链条中的偏移
	private int prfix_offset_s = 0;// 前缀在原始字符串中偏移

	private int num_state;// 检查模糊状态,&0x40,0x20,1
	private int num_offset_l = 0;// 数字在基本单元链条中的偏移
	private int num_offset_s = 0;// 数字在原始字符串中偏移

	public NumAboutRecognizer() {
		fsm = new FSM(begin_state, end_state) {
			@Override
			protected int input(Object[] mes) throws IOException {
				if (readindex >= aList.size() - 1) {
					mes[0] = null;
					return end_type;
				}
				int type = typeNow(mes);
				++readindex;
				return type;
			}
		};
		loadFsm();
	}

	private static class Message {
		String image;
		Category type;
		int numstate;// 如果是数字给出数字包含模糊的状态

		public Message(String image, Category type, int numstate) {
			super();
			this.image = image;
			this.type = type;
			this.numstate = numstate;
		}

	}

	public static final Map<String, Category> NUM_PRFIX;

	public static final Map<String, Category> NUM_SUFFIX;

	// 中文数字
	public static final String[] CHINESE_NUM;
	// 中文古数字
	public static final String[] CHINESE_OLD_NUM;
	// 中文数字前缀
	public static final String[] CHINESE_NUM_FUZZY_PRE;

	public static final String[] CHINESE_NUM_FUZZY_END;

	// 中文数字前缀
	public static final String[] CHINESE_NUM_FUZZY_MIDDLE;
	// 阿拉伯数字正则表达式
	public static final String ARBIC_NUM_REGEX = "\\d+(\\.\\d+)?%?";

	static {
		CHINESE_NUM = new String[] { "零", "○", "一", "二", "两", "三", "四", "五",
				"六", "七", "八", "九", "十", "廿", "百", "千", "万", "亿", "壹", "贰",
				"叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾", "佰", "仟" };

		CHINESE_NUM_FUZZY_PRE = new String[] { "几", "数", "第", "上", "成" };
		CHINESE_NUM_FUZZY_MIDDLE = new String[] { "几", "多", "点", "/", "分", "余" };
		CHINESE_NUM_FUZZY_END = new String[] { "几", "多", "数" };

		CHINESE_OLD_NUM = new String[] { "甲", "乙", "丙", "丁", "戊", "己", "庚",
				"辛", "壬", "癸", "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申",
				"酉", "戌", "亥" };

		Arrays.sort(CHINESE_NUM);
		Arrays.sort(CHINESE_NUM_FUZZY_PRE);
		Arrays.sort(CHINESE_NUM_FUZZY_END);
		Arrays.sort(CHINESE_NUM_FUZZY_MIDDLE);

		Arrays.sort(CHINESE_OLD_NUM);

		NUM_PRFIX = new HashMap<String, Category>();
		NUM_SUFFIX = new HashMap<String, Category>();

		String[] timePrfix = "星期,上周,下周,中午,午间,上午,下午,午后,晚上,傍晚,晚间,初,周,晚"
				.split(",");
		String[] timesuffix = new String[] { "年末", "年内", "年中", "年底", "年前",
				"年间", "年初", "月末", "月内", "月中", "月底", "月前", "月间", "月初", "月份",
				"小时", "分钟", "毫秒", "年", "月", "日", "号", "时", "分", "秒", "点", "点钟" };
		for (String str : timePrfix) {
			NUM_PRFIX.put(str, Category.TIME);
		}
		for (String str : timesuffix) {
			NUM_SUFFIX.put(str, Category.TIME);
		}

		NUM_SUFFIX.put("元", Category.CN_CURRENCY);
		NUM_SUFFIX.put("CNY", Category.CN_CURRENCY);
		NUM_SUFFIX.put("￥", Category.CN_CURRENCY);

		NUM_SUFFIX.put("美元", Category.US_CURRENCY);
		NUM_SUFFIX.put("$", Category.US_CURRENCY);
		NUM_SUFFIX.put("USD", Category.US_CURRENCY);

		NUM_SUFFIX.put("镑", Category.ER_CURRENCY);
		NUM_SUFFIX.put("￡", Category.ER_CURRENCY);

		NUM_SUFFIX.put("日元", Category.JP_CURRENCY);
		NUM_SUFFIX.put("¥", Category.JP_CURRENCY);

	}

	@Override
	public void close() throws IOException {
		this.aList = null;
		this.words = null;
	}

	@Override
	public void recognized(Matrix<Node> words, AtomList aList)
			throws IOException {
		suggestNextIndex = readindex = 1;// 跳去第一个开始元素
		this.aList = aList;
		this.words = words;
		clearprfix();
		clearNum();
		fsm.run();
	}

	private void setPrfix(String p, Category type, int l, int s) {
		prfix = p;
		prfixType = type;
		prfix_offset_l = l;
		prfix_offset_s = s;
	}

	private void addNum(String num, int state) {
		builder.append(num);
		num_state = state;
	}

	private void setNum(String num, Category numtypt, int l, int s, int state) {
		builder.setLength(0);
		builder.append(num);
		numType = numtypt;
		num_offset_l = l;
		num_offset_s = s;
		num_state = state;
	}

	private void clearprfix() {
		prfix = null;
		prfixType = null;
		prfix_offset_l = prfix_offset_s = 0;
	}

	private void clearNum() {
		builder.setLength(0);
		numType = null;
		num_offset_l = num_offset_s = 0;
		num_state = 0;
	}

	private void loadFsm() {
		fsm.addAction(new ACTION(begin_state, end_type) {
			@Override
			public int act(Object mes) throws IOException {
				return end_state;
			}
		});
		fsm.addAction(new ACTION(begin_state, prfix_type) {
			@Override
			public int act(Object mes) throws IOException {
				Message ms = (Message) mes;
				setPrfix(ms.image, ms.type, readindex - 1,
						aList.get(readindex - 1).offset);
				if (suggestNextIndex > readindex)
					readindex = suggestNextIndex;
				return with_prfix_num_state;
			}
		});
		fsm.addAction(new ACTION(begin_state, num_type) {
			@Override
			public int act(Object mes) throws IOException {
				Message ms = (Message) mes;
				setNum(ms.image, ms.type, readindex - 1,
						aList.get(readindex - 1).offset, ms.numstate);
				if (suggestNextIndex > readindex)
					readindex = suggestNextIndex;
				return no_prfix_num_state;
			}
		});
		fsm.addAction(new ACTION(begin_state, suffix_type) {
			@Override
			public int act(Object mes) throws IOException {
				return begin_state;
			}
		});
		fsm.addAction(new ACTION(begin_state, unknow_type) {
			@Override
			public int act(Object mes) throws IOException {
				return begin_state;
			}
		});

		fsm.addAction(new ACTION(with_prfix_num_state, end_type) {
			@Override
			public int act(Object mes) throws IOException {

				if (builder.length() > 0) {
					words.insert(new Node(num_offset_l, readindex, new Cell(
							builder.toString(), numType, num_offset_s)), false);
					words.insert(new Node(prfix_offset_l, readindex, new Cell(
							prfix + builder.toString(), prfixType,
							prfix_offset_s)), false);
				}
				return end_state;
			}
		});
		fsm.addAction(new ACTION(with_prfix_num_state, prfix_type) {
			@Override
			public int act(Object mes) throws IOException {
				if (builder.length() > 0) {
					words.insert(
							new Node(num_offset_l, readindex - 1, new Cell(
									builder.toString(), numType, num_offset_s)),
							false);
					words.insert(new Node(prfix_offset_l, readindex - 1,
							new Cell(prfix + builder.toString(), prfixType,
									prfix_offset_s)), false);
				}
				clearNum();
				Message ms = (Message) mes;
				setPrfix(ms.image, ms.type, readindex - 1,
						aList.get(readindex - 1).offset);
				if (suggestNextIndex > readindex)
					readindex = suggestNextIndex;
				return with_prfix_num_state;
			}
		});
		fsm.addAction(new ACTION(with_prfix_num_state, num_type) {
			@Override
			public int act(Object mes) throws IOException {
				Message ms = (Message) mes;
				if (builder.length() > 0) {
					if (numType == ms.type) {
						addNum(ms.image, ms.numstate);
					} else {
						words.insert(new Node(num_offset_l, readindex - 1,
								new Cell(builder.toString(), numType,
										num_offset_s)), false);
						words.insert(new Node(prfix_offset_l, readindex - 1,
								new Cell(prfix + builder.toString(), prfixType,
										prfix_offset_s)), false);
						clearprfix();
						setNum(ms.image, ms.type, readindex - 1,
								aList.get(readindex - 1).offset, ms.numstate);
						if (suggestNextIndex > readindex)
							readindex = suggestNextIndex;
						return no_prfix_num_state;
					}
				} else {
					setNum(ms.image, ms.type, readindex - 1,
							aList.get(readindex - 1).offset, ms.numstate);
				}
				if (suggestNextIndex > readindex)
					readindex = suggestNextIndex;
				return with_prfix_num_state;
			}
		});
		fsm.addAction(new ACTION(with_prfix_num_state, suffix_type) {
			@Override
			public int act(Object mes) throws IOException {
				if (builder.length() > 0) {
					words.insert(
							new Node(num_offset_l, readindex - 1, new Cell(
									builder.toString(), numType, num_offset_s)),
							false);
					Message ms = (Message) mes;
					if (prfixType == ms.type) {
						if (suggestNextIndex > readindex)
							readindex = suggestNextIndex;
						words.insert(new Node(prfix_offset_l, readindex,
								new Cell(prfix + builder.toString() + ms.image,
										prfixType, prfix_offset_s)), false);

					} else {
						words.insert(new Node(prfix_offset_l, readindex - 1,
								new Cell(prfix + builder.toString(), prfixType,
										prfix_offset_s)), false);
						if (suggestNextIndex > readindex)
							readindex = suggestNextIndex;
						words.insert(new Node(num_offset_l, readindex,
								new Cell(builder.toString() + ms.image,
										ms.type, num_offset_s)), false);
					}
				}
				clearNum();
				clearprfix();
				return begin_state;
			}
		});
		fsm.addAction(new ACTION(with_prfix_num_state, unknow_type) {
			@Override
			public int act(Object mes) throws IOException {
				if (builder.length() > 0) {
					words.insert(
							new Node(num_offset_l, readindex - 1, new Cell(
									builder.toString(), numType, num_offset_s)),
							false);
					words.insert(new Node(prfix_offset_l, readindex - 1,
							new Cell(prfix + builder.toString(), prfixType,
									prfix_offset_s)), false);
				}
				clearNum();
				clearprfix();
				return begin_state;
			}
		});

		fsm.addAction(new ACTION(no_prfix_num_state, end_type) {
			@Override
			public int act(Object mes) throws IOException {
				words.insert(
						new Node(num_offset_l, readindex, new Cell(builder
								.toString(), numType, num_offset_s)), false);
				return end_state;
			}
		});
		fsm.addAction(new ACTION(no_prfix_num_state, prfix_type) {
			@Override
			public int act(Object mes) throws IOException {
				words.insert(new Node(num_offset_l, readindex - 1, new Cell(
						builder.toString(), numType, num_offset_s)), false);
				clearNum();
				Message ms = (Message) mes;
				setPrfix(ms.image, ms.type, readindex - 1,
						aList.get(readindex - 1).offset);
				if (suggestNextIndex > readindex)
					readindex = suggestNextIndex;
				return with_prfix_num_state;
			}
		});
		fsm.addAction(new ACTION(no_prfix_num_state, num_type) {
			@Override
			public int act(Object mes) throws IOException {
				Message ms = (Message) mes;
				if (ms.type == numType) {
					addNum(ms.image, ms.numstate);
				} else {
					words.insert(
							new Node(num_offset_l, readindex - 1, new Cell(
									builder.toString(), numType, num_offset_s)),
							false);
					clearprfix();
					setNum(ms.image, ms.type, readindex - 1,
							aList.get(readindex - 1).offset, ms.numstate);
				}
				if (suggestNextIndex > readindex)
					readindex = suggestNextIndex;
				return no_prfix_num_state;
			}
		});
		fsm.addAction(new ACTION(no_prfix_num_state, suffix_type) {
			@Override
			public int act(Object mes) throws IOException {
				Message ms = (Message) mes;
				words.insert(new Node(num_offset_l, readindex - 1, new Cell(
						builder.toString(), numType, num_offset_s)), false);

				if (suggestNextIndex > readindex)
					readindex = suggestNextIndex;
				words.insert(
						new Node(num_offset_l, readindex, new Cell(builder
								.toString() + ms.image, ms.type, num_offset_s)),
						false);
				clearprfix();
				clearNum();
				return begin_state;
			}
		});
		fsm.addAction(new ACTION(no_prfix_num_state, unknow_type) {
			@Override
			public int act(Object mes) throws IOException {
				words.insert(new Node(num_offset_l, readindex - 1, new Cell(
						builder.toString(), numType, num_offset_s)), false);
				clearprfix();
				clearNum();
				return begin_state;
			}
		});

	}

	private int typeNow(Object[] mes) {
		Cell cell = aList.get(readindex);
		suggestNextIndex = readindex + 1;
		if (Category.isA(cell.type, Category.CNWORD)) {

			if (Arrays.binarySearch(CHINESE_NUM, cell.image) > -1) {
				mes[0] = new Message(cell.image, Category.CN_NUM, 0);
				return num_type;
			}

			if (Arrays.binarySearch(CHINESE_OLD_NUM, cell.image) > -1) {
				mes[0] = new Message(cell.image, Category.CN_ONUM, 0);
				return num_type;
			}

			if (checkIndex(readindex + 1)) {
				if (builder.length() == 0) {// 数字还未开始的情况下
					if (Arrays.binarySearch(CHINESE_NUM_FUZZY_PRE, cell.image) > -1) {
						String nexts = aList.get(readindex + 1).image;
						if (Arrays.binarySearch(CHINESE_NUM, nexts) > -1) {
							mes[0] = new Message(cell.image + nexts,
									Category.CN_NUM, 4);
							suggestNextIndex = readindex + 2;
							return num_type;
						}
					}
				} else if ((num_state & 0x07) == 0) {// 没有前后中模糊后缀的情况下
					String nexts = aList.get(readindex + 1).image;
					if (Arrays.binarySearch(CHINESE_NUM_FUZZY_MIDDLE,
							cell.image) > -1) {
						if (checkIndex(readindex + 2)) {
							String nextss = aList.get(readindex + 2).image;
							if ("之".equals(nexts)
									&& Arrays.binarySearch(CHINESE_NUM, nextss) > -1) {
								mes[0] = new Message(cell.image + nexts
										+ nextss, Category.CN_NUM, 2);
								suggestNextIndex = readindex + 3;
								return num_type;
							}
						}

						if (Arrays.binarySearch(CHINESE_NUM, nexts) > -1) {
							mes[0] = new Message(cell.image + nexts,
									Category.CN_NUM, 2);
							suggestNextIndex = readindex + 2;
							return num_type;
						}
					}
					if (Arrays.binarySearch(CHINESE_NUM_FUZZY_END, cell.image) > -1) {
						if (Arrays.binarySearch(CHINESE_NUM, nexts) < 0) {// 后一个不是数字
							mes[0] = new Message(cell.image, Category.CN_NUM, 1);
							return num_type;
						}
					}
				}

				String FF = cell.image + aList.get(readindex + 1).image;
				Category type = NUM_PRFIX.get(FF);
				if (type != null) {
					mes[0] = new Message(FF, type, 0);
					suggestNextIndex = readindex + 2;
					return prfix_type;
				}
				type = NUM_SUFFIX.get(FF);
				if (type != null) {
					mes[0] = new Message(FF, type, 0);
					suggestNextIndex = readindex + 2;
					return suffix_type;
				}
			}
			Category type = NUM_PRFIX.get(cell.image);
			if (type != null) {
				mes[0] = new Message(cell.image, type, 0);
				return prfix_type;
			}

			type = NUM_SUFFIX.get(cell.image);
			if (type != null) {
				mes[0] = new Message(cell.image, type, 0);
				return suffix_type;
			}

			return unknow_type;
		}
		if (Category.isA(cell.type, Category.SINGLE)) {
			if (cell.image.matches(ARBIC_NUM_REGEX)) {
				mes[0] = new Message(cell.image, Category.ARBIC_NUM, 0);
				return num_type;
			}
			return unknow_type;
		}
		return unknow_type;
	}

	private boolean checkIndex(int index) {
		return index < aList.size() - 1;
	}

}
