package scaatis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
	public static <T> T getRandom(List<T> list) {
		if(list.size() == 0) {
			return null;
		}
		return list.get(new Random().nextInt() % list.size());
	}

	public static <T> T getRandom(List<T> list, List<T> except) {
		ArrayList<T> cross = new ArrayList<>(list);
		for(T item : except) {
			cross.remove(item);
		}
		return getRandom(cross);
	}
	
	public static int mod(int a, int b) {
		int d = Math.abs(b);
		int c = a % d;
		while (c < 0) {
			c += d;
		}
		return c;
	}
}
