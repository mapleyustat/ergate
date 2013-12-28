package ergate.segment;

/**
 * 分词基本单元，几个小的单元可以合并为一个大的单元<br>
 * 起初的Cell是一些简单的基本字单元，以后逐渐由单元识别器识别出来<br>
 * 
 * @author en.xu
 * 
 */
public class Cell {
	/**
	 * 基本内容
	 */
	public final String image;
	/**
	 * 基本单元的类型
	 */
	public final Category type;

	/**
	 * 在句子中的偏移
	 */
	public final int offset;

	public Cell(String image, Category type, int offset) {
		super();
		this.image = image;
		this.type = type;
		this.offset = offset;
	}

	@Override
	public String toString() {
		return "[" + offset + "]" + image + type.name;
	}

}
