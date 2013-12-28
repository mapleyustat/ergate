package ergate.segment.imp;

import java.io.IOException;
import java.io.StringReader;

import ergate.dict.imp.DefaultDict;
import ergate.segment.AtomList;
import ergate.segment.Cell;
import ergate.segment.Segment;

public class SegmentMain {

	private Segment segment;

	public SegmentMain() {
		segment = new Segment(new DefaultDict());
		segment.addRecoger(new NumAboutRecognizer());
	}

	
	public String token(String str) throws IOException {
		if (str == null) {
			return null;
		}
		str = str.trim();
		if (str.isEmpty()) {
			return "";
		}
		StringReader reader = new StringReader(str);
		SentenceTokenizerImp imp = new SentenceTokenizerImp(reader);
		StringBuilder builder = new StringBuilder();
		AtomList list;
		Cell cell;
		while (imp.hasNext()) {
			list = imp.getSentence();
			list = segment.split(list);
			for (int i = 0; i < list.size() - 1; i++) {
				cell = list.get(i);
				builder.append(cell.image).append("[").append(cell.offset)
						.append("]").append(cell.type.name).append(" ");
			}
		}
		imp.close();
		return builder.toString();
	}

	public void close() throws IOException {
		if (segment != null)
			segment.close();
	}

}
