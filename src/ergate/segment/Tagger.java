package ergate.segment;

import java.io.Closeable;
import java.io.IOException;


/**
 * 标注器,对标准句子进行合并与标注
 * @author en.xu
 *
 */
public interface Tagger extends Closeable{
	
	
	/**
	 * 对给定的标准句子链条进行调整以及标注等
	 * @param src
	 * @return
	 * @throws IOException
	 */
	AtomList tag(AtomList src)throws IOException;

}
