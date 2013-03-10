package com.sulauncher.supad3d;

/**
 * Translator of T9, from numbers to String[]
 * @author YuMS
 */
public class Supad3DT9Translator {
	private static String[] data = {"0", "1", "2abc", "3def", "4ghi", "5jkl", "6mno", "7pqrs", "8tuv", "9wxyz"};
	public static String tr(char str) {
		int num = str - '0';
		if (num >= 0 && num <= 9) {
			return data[num];
		} else return "";
	}
}
