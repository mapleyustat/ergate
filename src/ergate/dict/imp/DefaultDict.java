package ergate.dict.imp;

import java.io.BufferedInputStream;
import java.io.IOException;

public class DefaultDict extends RamLexicon {
	private static String DICT = "base.idt";
	public DefaultDict() {
		Class<?> cls = getClass();
		try {
			DataInput in = new DataInput(new BufferedInputStream(
					cls.getResourceAsStream(DICT), 10240));
			try{
				this.load(in);
			}finally{
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
