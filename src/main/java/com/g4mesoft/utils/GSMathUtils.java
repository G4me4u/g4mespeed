package com.g4mesoft.utils;

public final class GSMathUtils {

	public static float clamp(float v, float mn, float mx) {
		return v < mn ? mn : (v > mx ? mx : v);
	}

	public static double clamp(double v, double mn, double mx) {
		return v < mn ? mn : (v > mx ? mx : v);
	}

	public static boolean equalsApproximate(float a, float b) {
		return Math.abs(b - a) < 1.0E-5F;
	}

	public static boolean equalsApproximate(double a, double b) {
		return Math.abs(b - a) < 1.0E-5F;
	}
}
