package com.servlet;

public class Main {
	public static void main(String[] args) {
		for(int i = 10;i < 100;i++) {
			String str = String.valueOf(i);
			System.out.println(Integer.valueOf(str + str.charAt(0) + str.charAt(1)));
		}
	}
}
