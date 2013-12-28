package ergate.segment;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 语料词典，里面记载了登陆词以及一些标签的相关信息,<br>
 * 记录的标签涵盖{@link Category}所定义的除去WORD,UNKNOW标签以外的其他标签系信息<br>
 * 
 * @author en.xu
 * 
 */
public interface  Dictionary extends Closeable {

	/**
	 * 返回最长的字典词的长度
	 * 
	 * @return
	 */
	public  int maxWordLen();

	/**
	 * 判断指定的字符串是否为字典中的登陆词或者标签，标签参见{@link Category} 同时返回这个词可能的类型
	 * 
	 * @param image
	 * @return
	 * @throws IOException
	 */
	public  boolean isInDict(String image, ArrayList<Category> types)
			throws IOException;

	/**
	 * 度量两个单元之间的语义距离,可以采用-log(P(C<sub>i</sub>|C<sub>i-1</sub>)),<br>
	 * 这里会采用数据平滑，可以利用Jelinek-Mercer插值,或者Katz方法<br>
	 * 注意对未登录词的处理
	 * @return
	 * @throws IOException
	 */
	public  double distance(Cell p, Cell n) throws IOException;

}
