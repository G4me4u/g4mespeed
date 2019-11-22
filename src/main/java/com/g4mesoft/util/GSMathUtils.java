package com.g4mesoft.util;

public final class GSMathUtils {

	public static int clamp(int v, int mn, int mx) {
		return v < mn ? mn : (v > mx ? mx : v);
	}
	
	public static long clamp(long v, long mn, long mx) {
		return v < mn ? mn : (v > mx ? mx : v);
	}

	public static float clamp(float v, float mn, float mx) {
		return v < mn ? mn : (v > mx ? mx : v);
	}

	public static double clamp(double v, double mn, double mx) {
		return v < mn ? mn : (v > mx ? mx : v);
	}

	public static boolean equalsApproximate(float a, float b) {
		return equalsApproximate(a, b, 1.0E-5F);
	}

	public static boolean equalsApproximate(float a, float b, float epsilon) {
		return Math.abs(b - a) < epsilon;
	}

	public static boolean equalsApproximate(double a, double b) {
		return equalsApproximate(a, b, 1.0E-5F);
	}

	public static boolean equalsApproximate(double a, double b, double epsilon) {
		return Math.abs(b - a) < epsilon;
	}
}
