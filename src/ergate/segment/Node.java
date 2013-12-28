package ergate.segment;

import ergate.utils.Element;


/**
 * 分词中的单元节点
 * @author en.xu
 *
 */
public class Node extends Element{
	
	public Cell cell;

	public Node(int row, int col, Cell cell) {
		super(row, col);
		this.cell = cell;
	}

	@Override
	public String toString() {
		return cell.image;
	}
	
	
}
