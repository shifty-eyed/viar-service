package org.viar.core;

import org.junit.Test;

public class Permutations {

	@Test
	public void test() {
		String s = "ABC";
		permute(s.toCharArray(), 0, s.length() - 1);
	}

	private void permute(char[] str, int start, int end) {
		if (start == end) {
			System.out.println(str);
		} else {
			for (var i = start; i <= end; i++) {
				swap(str, i, end);
				permute(str, i, end);
				swap(str, i, end);
			}
		}
	}
	
	private void swap(char[] str, int a, int b) {
		char tmp = str[a];
		str[a] = str[b];
		str[b] = tmp;
	}

}
