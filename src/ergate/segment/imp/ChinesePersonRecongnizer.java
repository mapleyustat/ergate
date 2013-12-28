package ergate.segment.imp;

import java.io.IOException;

import ergate.segment.AtomList;
import ergate.segment.Node;
import ergate.segment.Recognizer;
import ergate.utils.Matrix;

/**
 * 中文人名识别器 <br>
 * 人名模式:
 * 
 * <pre>
 * <b>
 * A:人名的上文
 * B:人命的下文
 * C:中国人名的姓氏字
 * D:复姓其第一字
 * E:复姓第二字
 * F:人名的名字部分
 * G:人名的前缀
 * H:人名的后缀
 * M:翻译名称的首部
 * N:翻译名称的中部
 * O:翻译名称的末部
 * P:翻译名称姓氏附加字
 * X:连接词
 * Z:其他
 * </b>
 * </pre>
 * 
 * 人名匹配的模式
 * <pre>
 * (GC|GCH|CF|CFF|DEF|DEFF|CH|CHH|FF):上官婉儿,上官燕,王雅洁,周虹,王总,刘书记,小李,小李子,恩来
 * M*N?O{2+}P?:本杰明*富兰克林,小泉纯一郎,酒井美惠子
 * </pre>
 * 
 * @author en.xu
 * 
 */
public class ChinesePersonRecongnizer implements Recognizer {

	@Override
	public void close() throws IOException {

	}

	@Override
	public void recognized(Matrix<Node> words, AtomList aList)
			throws IOException {

	}

}
