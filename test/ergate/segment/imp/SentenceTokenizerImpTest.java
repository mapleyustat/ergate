package ergate.segment.imp;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import ergate.segment.AtomList;
import ergate.segment.SentenceTokenizer;

public class SentenceTokenizerImpTest {

	@Test
	public void testGetSentence() throws IOException {
		String content=",";
		StringReader reader=new StringReader(content);
		SentenceTokenizer sImp=new SentenceTokenizerImp(reader);
		while(sImp.hasNext()){
			show(sImp.getSentence());
		}
		sImp.close();
	}

	private void show(AtomList sentence) {
		System.out.println(sentence.check());
		System.out.println(sentence);
		System.out.println("*********************************************************");
	}

}
