package com.g4mesoft.util;

public class GSColorUtil {

	private static final double SRGB_2_XYZ_B11 = 0.4124;
	private static final double SRGB_2_XYZ_B12 = 0.3576;
	private static final double SRGB_2_XYZ_B13 = 0.1805;

	private static final double SRGB_2_XYZ_B21 = 0.2126;
	private static final double SRGB_2_XYZ_B22 = 0.7152;
	private static final double SRGB_2_XYZ_B23 = 0.0722;

	private static final double SRGB_2_XYZ_B31 = 0.0193;
	private static final double SRGB_2_XYZ_B32 = 0.1192;
	private static final double SRGB_2_XYZ_B33 = 0.9505;
	
	private static final double D65_XN =  95.0489;
	private static final double D65_YN = 100.0000;
	private static final double D65_ZN = 108.8840;
	
	private static final double LAB_DELTA = 6.0 / 29.0;
	private static final double LAB_DELTA_SQ = LAB_DELTA * LAB_DELTA;
	private static final double LAB_DELTA_CB = LAB_DELTA_SQ * LAB_DELTA;
	
	private static final double SIMILAR_COLOR_CIE76 = 2.3;
	
	private static final String RGB_HEX_FORMAT  = "%06X";
	private static final String RGBA_HEX_FORMAT = "%08X";
	
	/* @see https://en.wikipedia.org/wiki/SRGB#The_reverse_transformation */
	public static double[] rgb2xyz(int r, int g, int b) {
		double rf = toSRGB(r / 255.0);
		double gf = toSRGB(g / 255.0);
		double bf = toSRGB(b / 255.0);
	
		return new double[] { 
			SRGB_2_XYZ_B11 * rf + SRGB_2_XYZ_B12 * gf + SRGB_2_XYZ_B13 * bf,
			SRGB_2_XYZ_B21 * rf + SRGB_2_XYZ_B22 * gf + SRGB_2_XYZ_B23 * bf,
			SRGB_2_XYZ_B31 * rf + SRGB_2_XYZ_B32 * gf + SRGB_2_XYZ_B33 * bf
		};
	}
	
	private static double toSRGB(double u) {
		return (u < 0.04045) ? (25.0 * u / 323.0) : 
			Math.pow((200.0 * u + 11.0) / 211.0, 12.0 / 5.0);
	}

	/* @see https://en.wikipedia.org/wiki/CIELAB_color_space#Forward_transformation */
	public static double[] xyz2lab(double x, double y, double z) {
		double labY = labForward(y / D65_YN);
		
		double l = 116.0 * labY - 16.0;
		double a = 500.0 * (labForward(x / D65_XN) - labY);
		double b = 200.0 * (labY - labForward(z / D65_ZN));
	
		return new double[] { l, a, b };
	}
	
	private static double labForward(double t) {
		return (t > LAB_DELTA_CB) ? Math.cbrt(t) : 
			t / (3.0 * LAB_DELTA_SQ) + 4.0 / 29.0;
	}
	
	public static double[] rgb2lab(int r, int g, int b) {
		double[] xyz = rgb2xyz(r, g, b);
		return xyz2lab(xyz[0], xyz[1], xyz[2]);
	}

	/* @see https://en.wikipedia.org/wiki/Color_difference#CIE76 */
	public static double getColorDifferenceCIE76(double l1, double a1, double b1, double l2, double a2, double b2) {
		double dl = l2 - l1;
		double da = a2 - a1;
		double db = b2 - b1;
		return Math.sqrt(dl * dl + da * da + db * db);
	}
	
	public static boolean isRGBSimilar(int rgb1, int rgb2) {
		return isRGBSimilar(unpackR(rgb1), unpackG(rgb1), unpackB(rgb1), 
		                    unpackR(rgb2), unpackG(rgb2), unpackB(rgb2));
	}

	public static boolean isRGBSimilar(int r1, int g1, int b1, int r2, int g2, int b2) {
		return getColorDifferenceCIE76(rgb2lab(r1, g1, b1), rgb2lab(r2, g2, b2)) < SIMILAR_COLOR_CIE76;
	}
	
	public static double getColorDifferenceCIE76(double[] lab1, double[] lab2) {
		return getColorDifferenceCIE76(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
	}
	
	/* @see https://en.wikipedia.org/wiki/HSL_and_HSV#To_RGB */
	public static int hsb2rgb(double hue, double saturation, double brightness) {
		// Find H' and fraction of H'.
		double h = 6.0 * (hue - Math.floor(hue));
		double f = h - Math.floor(h);
		
		// Find chroma and minimum.
		double c = brightness * saturation;
		double m = brightness - c;

		// Find x + m for even and odd H', respectively.
		double p = m + c * f;
		double q = m + c * (1.0 - f);
		
		switch ((int)h) {
		case 0:
			return denormalizeRGB(brightness, p, m);
		case 1:
			return denormalizeRGB(q, brightness, m);
		case 2:
			return denormalizeRGB(m, brightness, p);
		case 3:
			return denormalizeRGB(m, q, brightness);
		case 4:
			return denormalizeRGB(p, m, brightness);
		case 5:
			return denormalizeRGB(brightness, m, q);
		default:
			return 0xFF000000;
		}
	}

	public static double[] rgb2hsb(int rgb) {
		return rgb2hsb(unpackR(rgb), unpackG(rgb), unpackB(rgb));
	}

	/* @see https://en.wikipedia.org/wiki/HSL_and_HSV#From_RGB */
	public static double[] rgb2hsb(int r, int g, int b) {
		int xmax = Math.max(r, Math.max(g, b));
		int xmin = Math.min(r, Math.min(g, b));
		int c = xmax - xmin;
		
		double hue;
		if (c == 0) {
			hue = 0.0;
		} else {
			if (xmax == r) {
				hue = (double)(g - b) / c;
			} else if (xmax == g) {
				hue = 2.0 + (double)(b - r) / c;
			} else {
				hue = 4.0 + (double)(r - g) / c;
			}
			
			hue /= 6.0;
			if (hue < 0.0)
				hue += 1.0;
		}
		
		double brightness = (double)xmax / 255.0;
		double saturation = (xmax != 0) ? (double)c / xmax : 0.0;
		
		return new double[] { hue, saturation, brightness };
	}
	
	public static int denormalizeRGB(double r, double g, double b) {
		return packRGB((int)(r * 255.0 + 0.5),
		               (int)(g * 255.0 + 0.5),
		               (int)(b * 255.0 + 0.5));
	}

	public static int denormalizeRGBA(double r, double g, double b, double a) {
		return packRGBA((int)(r * 255.0 + 0.5),
		                (int)(g * 255.0 + 0.5),
		                (int)(b * 255.0 + 0.5),
		                (int)(a * 255.0 + 0.5));
	}
	
	public static int withBlue(int rga, double b) {
		return withBlue(rga, (int)(b * 255.0 + 0.5));
	}
	
	public static int withBlue(int rga, int blue) {
		return blue | (rga & 0xFFFFFF00);
	}

	public static int withGreen(int rba, double g) {
		return withGreen(rba, (int)(g * 255.0 + 0.5));
	}
	
	public static int withGreen(int rba, int green) {
		return (green << 8) | (rba & 0xFFFF00FF);
	}

	public static int withRed(int gba, double r) {
		return withRed(gba, (int)(r * 255.0 + 0.5));
	}
	
	public static int withRed(int gba, int red) {
		return (red << 16) | (gba & 0xFF00FFFF);
	}

	public static int withAlpha(int rgb, double a) {
		return withAlpha(rgb, (int)(a * 255.0 + 0.5));
	}
	
	public static int withAlpha(int rgb, int alpha) {
		return (alpha << 24) | (rgb & 0x00FFFFFF);
	}
	
	public static int invertRGB(int rgb) {
		return packRGBA(0xFF - unpackR(rgb),
		                0xFF - unpackG(rgb),
		                0xFF - unpackB(rgb),
		                unpackA(rgb));
	}
	
	public static int packRGB(int r, int g, int b) {
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	public static int packRGBA(int r, int g, int b, int a) {
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public static int unpackB(int rgba) {
		return rgba & 0xFF;
	}
	
	public static int unpackG(int rgba) {
		return (rgba >>> 8) & 0xFF;
	}

	public static int unpackR(int rgba) {
		return (rgba >>> 16) & 0xFF;
	}

	public static int unpackA(int rgba) {
		return rgba >>> 24;
	}
	
	public static String rgb2str(int rgb) {
		return String.format(RGB_HEX_FORMAT, rgb & 0xFFFFFF);
	}

	public static String rgba2str(int rgba) {
		return String.format(RGBA_HEX_FORMAT, rgba);
	}
	
	public static int str2rgba(String str) {
		int rgba = 0x00000000;
		try {
			rgba = Integer.parseInt(str, 16);
		} catch (NumberFormatException ignore) {
		}
		
		return (str.length() <= 6) ? (rgba | 0xFF000000) : rgba;
	}
}
