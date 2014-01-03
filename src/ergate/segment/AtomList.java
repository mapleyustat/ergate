package ergate.segment;

import java.util.ArrayList;
import java.util.List;

/**
 * 标准句子，一定是以句子开始标记作为开始，句子结束标记或者句子结束标点作为结束
 * 
 * @author en.xu
 * 
 */
public class AtomList extends ArrayList<Cell> {
	private static final long serialVersionUID = -3846443969320522845L;

	// 在文中的偏移
	public final int offset;

	/**
	 * @param cells
	 */
	public AtomList(int size, int offset) {
		super(size);
		this.offset = offset;
	}

	public AtomList(int offset) {
		super();
		this.offset = offset;
	}

	public AtomList(List<Cell> list, int start) {
		super(list);
		this.offset = start;
	}

	/**
	 * 校验是否为标准句子，即符合标准
	 * 
	 * @return
	 */
	public boolean check() {
		return (size() > 2) && Category.isA(get(0).type, Category.BEGIN)
				&& Category.isA(get(size() - 1).type, Category.END);
	}

	@Override
	public String toString() {
		return "[offset=" + offset + "]" + super.toString();
	}

	public String ToHTML() {
		StringBuilder html = new StringBuilder();
		html.append("<table border=\"1\" width=\"100%\"><tr><td width=\"100%\">");
		Cell cell;
		for (int i = 1; i < size() - 1; i++) {
			cell = get(i);
			html.append("<font color=\"blue\">").append(getIString(cell.image))
					.append("</font>");
			html.append("<font color=\"#006030\">[")
					.append(getIString(cell.type.name)).append("]</font>");
			html.append("&nbsp;");
		}
		html.append("</td></tr></table>");
		return html.toString();
	}

	private String getIString(String image) {
		if (image.startsWith("<") && image.endsWith(">")) {
			return image.substring(1, image.length() - 1);
		}
		return image;
	}
}
