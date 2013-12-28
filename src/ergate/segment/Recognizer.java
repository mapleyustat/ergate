package ergate.segment;

import java.io.Closeable;
import java.io.IOException;

import ergate.utils.Matrix;

/**
 * 句子识别器
 * 
 * @author en.xu
 * 
 */
public interface Recognizer extends Closeable {
	/**
	 * 从给定的标准基本句子链中识别出可能的词语并将它添加到矩阵中保存下来<br>
	 * 此部分涉及未登录词的识别以及命名体的识别等 <br>
	 * 
	 * @param words
	 * @param sentence
	 * @throws IOException
	 */
	public void recognized(Matrix<Node> words, AtomList aList)
			throws IOException;

}
