package ergate.segment;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 标签
 * 
 * @author en.xu
 * 
 */
public enum Category {

	/** 未知的的类别词，所有的总类别,即默认为未知 **/
	DEFAULT(0, "<UNKNOW>"),
	/** 句子的开始 */
	BEGIN(1, "<ST>"),
	/** 句子的结束 */
	END(2, "<SE>"),
	/** 标点符号 */
	PUNCTUATION(3, "<PUNC>"),
	/** 中文字符串 **/
	CNWORD(4, "<CNWD>"),
	/** 非中文符号串 */
	SINGLE(5, "<SINGLE>"),

	// 命名体类
	/** 表情符 */
	SYMBOL(6, "<SYMBOL>"),
	/** 数字 */
	NUM(7, "<NUM>"),
	/** 人名 */
	PERSON(8, "<PERSON>"),
	/** 地名 */
	PLACE(9, "<PLACE>"),
	/** 组织机构名称 */
	ORG(10, "<ORG>"),
	/** 时间 */
	TIME(12, "<TIME>"),
	/** 货币 */
	CURRENCY(13, "<CURRENCY>"),
	/** 术语,专有名词，行话 */
	JARGON(14, "<JARGON>"),
	/*
	 * 控制符的子类别
	 */
	/** 句子结束标志 */
	BORDER(15, "<MBO>", END),
	/** 子句子结束标记 */
	SUB_BORDER(16, "<SMBO>", END),

	/*
	 * 非中文字符串类别
	 */
	/** 英文单词 */
	LETTER(17, "<LETTER>", SINGLE),
	/** URL网址 */
	URL(18, "<URL>", SINGLE),
	/** 邮箱 */
	EMAIL(19, "<EMAIL>", SINGLE),
	/** 商标 */
	TRADEMARK(20, "<TRADM>", SINGLE),
	/** 电话号码 */
	TEL(21, "<TEL>", SINGLE),

	/*
	 * 数字类
	 */
	/** 中文数字 */
	CN_NUM(22, "<CNNUM>", NUM),
	/** 中文古数字 */
	CN_ONUM(23, "<CNONUM>", NUM),
	/** 阿拉伯数字 */
	ARBIC_NUM(24, "<ARBICNUM>", NUM),
	/** 罗马数字 */
	RM_NUM(25, "<RMNUM>", NUM),

	/*
	 * 货币类
	 */
	/** 大陆货币 */
	CN_CURRENCY(26, "<CN$>", CURRENCY),
	/** 美元货币 */
	US_CURRENCY(27, "<US$>", CURRENCY),
	/** 日元货币 */
	JP_CURRENCY(28, "<JP$>", CURRENCY),
	/** 俄货币 */
	RS_CURRENCY(29, "<RS$>", CURRENCY),
	/** 欧元货币 */
	ER_CURRENCY(30, "<ER$>", CURRENCY),

	/*
	 * 人名类
	 */
	/** 中文人名 */
	CN_PERSON(31, "<CNP>", PERSON),
	/** 日本人名 */
	JP_PERSON(32, "<JPP>", PERSON),
	/** 美国人名 */
	US_PERSON(33, "<USP>", PERSON),
	/** 俄国人名 */
	RS_PERSON(34, "<RSP>", PERSON),

	/*
	 * 中文独有的词性
	 */
	/** 形容词性语素 */
	ADJ_GEN(100, "<Ag>", CNWORD),
	/** 形容词 */
	ADJ(101, "<Adj>", CNWORD),
	/** 副形词 直接作状语的形容词 */
	ADJ_AD(102, "<Ad>", CNWORD),
	/** 名形词 具有名词功能的形容词 */
	ADJ_NOUN(103, "<an>", CNWORD),
	/** 区别词 */
	BIE(104, "<b>", CNWORD),
	/** 连词 */
	CONJ(105, "<c>", CNWORD),
	/** 副语素 副词性语素 */
	ADV_GEN(106, "<dg>", CNWORD),
	/** 副词 */
	ADV(107, "<d>", CNWORD),
	/** 叹词 */
	EXC(108, "<e>", CNWORD),
	/** 方位词 */
	FANG(109, "<f>", CNWORD),
	/** 语素 */
	GEN(110, "<g>", CNWORD),
	/** 前接成分 */
	HEAD(111, "<h>", CNWORD),
	/** 成语 */
	IDIOM(112, "<i>", CNWORD),
	/** 简称略语 */
	JIAN(113, "<j>", CNWORD),
	/** 后接成分 */
	SUFFIX(114, "<k>", CNWORD),
	/** 习用语 习用语尚未成为成语 */
	TEMP(115, "<l>", CNWORD),
	/** 名语素 名词性语素 */
	NOUN_GEN(116, "<Ng>", CNWORD),
	/** 名词 */
	NOUN(117, "<n>", CNWORD),
	
	/** 拟声词 */
	ONOM(118, "<o>", CNWORD),
	/** 介词 */
	PREP(119, "<p>", CNWORD),
	/** 量词 */
	QUAN(120, "<q>", CNWORD),
	/** 代词 */
	PRONOUN(121, "<r>", CNWORD),
	/** 处所词 */
	SPACE(122, "<s>", CNWORD),
	/** 时语素 时间词性语素 */
	TIME_GEN(123, "<Tg>", CNWORD),
	/** 助词 */
	AUXI(124, "<u>", CNWORD),
	/** 动语素 动词性语素 */
	VERB_GEN(125, "<Vg>", CNWORD),
	/** 动词 取英语动词 verb 的第一个字母 */
	VERB(126, "<v>", CNWORD),
	/** 副动词 直接作状语的动词 */
	VERB_AD(127, "<vd>", CNWORD),
	/** 名动词 指具有名词功能的动词 */
	VERB_NOUN(128, "<vn>", CNWORD),
	/** 非语素字 非语素字只是一个符号 */
	NO_GEN(129, "<x>", CNWORD),
	/** 语气词 */
	YUNQI(130, "<y>", CNWORD),
	/** 状态词 */
	STATUS(131, "<z>", CNWORD),
	
	/**名词性数字,例如：少许,不多。这些词不是数字类型会被记录与词典中*/
	NOUN_NUM(132, "<nNum>", CNWORD),
	/**名词性时间,例如:初冬,将来*/
	NOUN_TIME(133, "<nTime>", CNWORD);

	public final int code;
	public final String name;
	private final BitSet father;

	private Category(int code, String name, Category... father) {
		this.code = code;
		this.name = name;
		this.father = new BitSet();
		this.father.set(code);// 自继承
		this.father.set(0);// 默认继承未知
		if (father != null) {
			for (Category fa : father) {
				this.father.or(fa.father);// 保持继承的传递性
			}
		}
	}

	/**
	 * 判断给定的两个标签是否为继承关系
	 * 
	 * @param sun
	 * @param fa
	 * @return
	 */
	public static final boolean isA(Category sun, Category fa) {
		return sun.father.get(fa.code);
	}

	/**
	 * 通过code查找
	 * 
	 * @param code
	 * @return
	 */
	public static final Category findBycode(int code) {
		return codes.get(code);
	}

	/**
	 * 通过名称查找
	 * 
	 * @param name
	 * @return
	 */
	public static final Category findByName(String name) {
		return names.get(name);
	}

	private static final Map<String, Category> names;
	private static final Map<Integer, Category> codes;

	static {
		names = new TreeMap<String, Category>();
		codes = new HashMap<Integer, Category>();
		Category[] values = Category.values();
		for (Category ca : values) {
			assert !names.containsKey(ca.name) : "There is a conflict in enum "
					+ Category.class.getName() + " the name of " + ca + " is "
					+ ca.name + " was used by " + names.get(ca.name);
			names.put(ca.name, ca);
			assert !codes.containsKey(ca.code) : "There is a conflict in enum "
					+ Category.class.getName() + " the code of " + ca + " is "
					+ ca.code + " was used by " + codes.get(ca.code);
			codes.put(ca.code, ca);
		}

	}

}
