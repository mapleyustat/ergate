package ergate.utils;

public final class NumUtils {

	
	/**
	 * 熵函数
	 * @param prop
	 * @return
	 */
	public static double H(double prop) {
		if (prop < 0.0 || prop > 1.0) {
			throw new IllegalArgumentException(
					"prop must in [0,1],illegal prop=" + prop);
		}
		if (prop == 0.0) {
			return Double.MAX_VALUE / 10000;
		}
		return -Math.log(prop);
	}
}
