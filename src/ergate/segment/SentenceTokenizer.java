package ergate.segment;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * 句子切分器
 * 
 * @author en.xu
 * 
 */
public abstract class SentenceTokenizer implements Closeable{
	// 输入流
	protected Reader input;

	/**
	 * @param input
	 */
	public SentenceTokenizer(Reader input) {
		this.input = input;
	}
	
	
	/**
	 * 是否可以从输入流中获取到下一个句子
	 * @throws IOException
	 */
	public abstract boolean hasNext()throws IOException;
	
	/**
	 * 如果有句子则获取当前句子
	 * @return
	 */
	public abstract AtomList getSentence();
	
	/**
	 * 重置输入流
	 * @param reader
	 */
	public void rest(Reader reader)throws IOException{
		close();
		this.input=reader;
	}
	
	public void close()throws IOException{
		if(input!=null){
			input.close();
		}
	}


}
