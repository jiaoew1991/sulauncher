package com.sulauncher.search;

import java.util.ArrayList;

import android.util.Log;

import com.sulauncher.search.HanziToPinyin.Token;

/**
 * @author Internet
 * Generator for HanziToPinyin class
 */
public class KeyGenerator {
	private static final String TAG = "keygenerator";
	public static String generate(String str) {
		ArrayList<Token> tokens = HanziToPinyin.getInstance().get(str);  
	    StringBuilder sb = new StringBuilder();  
	    if (tokens != null && tokens.size() > 0) {  
	        for (Token token : tokens) {  
	            if (Token.PINYIN == token.type) {  
	                sb.append(superTrim(token.target));  
	            } else {  
	                sb.append(superTrim(token.source));  
	            }  
	            sb.append(' ');
	        }  
	    }
	    return sb.toString().toLowerCase();
	}
	public static String generateImportant(String str, ArrayList<Integer> pos) {
		StringBuilder sb = new StringBuilder();
		pos.clear();
		str = superTrim(str);
		if (str.length() > 0) {
			pos.add(0);
			sb.append(str.charAt(0));
			for (int i = 1; i < str.length(); i++) {
				if (str.charAt(i - 1) == ' ') {
					sb.append(str.charAt(i));
					pos.add(i);
				}
			}
		}
		return sb.toString();
	}
	/**
	 * For some softwares using invisible chars to promote ranking
	 * @param str
	 * @return
	 */
	private static String superTrim(String str) {
		str = str.trim();
		while (str.length() > 0 && str.codePointAt(0) > 122) {
			Log.d(TAG, "generating important, code point at 0 is" + str.codePointAt(0));
			Log.d(TAG, "generating important, str is" + str);
			str = str.substring(1);
		}
		return str;
	}
}