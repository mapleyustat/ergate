package ergate.utils;

import org.junit.Test;

public class LinkedMatrixGraphTest {

	@Test
	public void test() {
		LinkedMatrix<Edge> matrix=new LinkedMatrix<Edge>();
		matrix.addLast(new Edge(0, 1, 1));
		matrix.addLast(new Edge(0, 2, 2));
		matrix.addLast(new Edge(1, 2, 12));
		matrix.addLast(new Edge(1, 3, 13));
		matrix.addLast(new Edge(1, 4, 14));
		matrix.addLast(new Edge(2, 3, 23));
		matrix.addLast(new Edge(2, 4, 24));
		matrix.addLast(new Edge(3, 4, 34));
		System.out.println(matrix);
		XIterator<Edge> it=matrix.iterator();
		Edge e;
		while((e=it.next())!=null){
			XIterator<Edge> its=matrix.row(e.col, it);
			Edge g;
			while((g=its.next())!=null){
				System.out.println(e.weight+"<___>"+g.weight);
			}
		}
	}

}
