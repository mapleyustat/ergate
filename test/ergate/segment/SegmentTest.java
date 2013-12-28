package ergate.segment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ergate.dict.imp.DataInput;
import ergate.dict.imp.RamLexicon;
import ergate.segment.imp.NumAboutRecognizer;
import ergate.segment.imp.SentenceTokenizerImp;

public class SegmentTest {

	private File base;
	private RamLexicon corpora;

	@Before
	public void load() throws IOException {
		base = new File("res/dict/base.idt");
		DataInput baseIn = new DataInput(new FileInputStream(base));
		corpora = new RamLexicon();
		corpora.load(baseIn);
		baseIn.close();
	}

	@Test
	public void test() throws IOException {
		String content = "那儿对这一条款夫妻双方在国外连续居住一年以上的留学人员指的是夫妻两人都留学";
		StringReader reader = new StringReader(content);
		SentenceTokenizer sImp = new SentenceTokenizerImp(reader);
		Segment segment = new Segment(corpora);
		segment.addRecoger(new NumAboutRecognizer());
		long s = System.currentTimeMillis();
		while (sImp.hasNext()) {
			AtomList list = segment.split(sImp.getSentence());
			show(list);
		}
		System.out.println("time=" + (System.currentTimeMillis() - s)+"ms");
		sImp.close();
		segment.close();
	}

	public Dictionary getEmptydict() {
		return new Dictionary() {

			@Override
			public void close() throws IOException {
			}

			@Override
			public int maxWordLen() {
				return 2;
			}

			@Override
			public double distance(Cell p, Cell n) throws IOException {
				return 1.0;
			}

			@Override
			public boolean isInDict(String image, ArrayList<Category> types)
					throws IOException {
				// TODO Auto-generated method stub
				return false;
			}

		};
	}

	private void show(AtomList sentence) {
		System.out.println(sentence.check());
		System.out.println(sentence);
	}

}
