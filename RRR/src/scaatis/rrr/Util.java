package scaatis.rrr;

public class Util {
	public static int mod(int a, int b) {
		int d = Math.abs(b);
		int c = a % d;
		while (c < 0) {
			c += d;
		}
		return c;
	}
}
