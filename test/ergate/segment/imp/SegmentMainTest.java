package ergate.segment.imp;

import java.io.IOException;

import org.junit.Test;

public class SegmentMainTest {

	@Test
	public void testToken() throws IOException {
		SegmentMain main=new SegmentMain();
		String str="中国推进城镇化速度和规模都是史无前例的，加快推进大规模城市化进程，面临诸多挑战";
		System.out.println(main.token(str));
		main.close();
	}

}
