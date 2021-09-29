package com.g4mesoft.panel;

public final class GSLayoutProperties {

	public static final GSILayoutProperty<Integer> MINIMUM_WIDTH      = new GSIntLayoutProperty("minimumWidth" , GSPanel::getDefaultMinimumWidth   , 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer> MINIMUM_HEIGHT     = new GSIntLayoutProperty("minimumHeight", GSPanel::getDefaultMinimumHeight  , 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<GSDimension> MINIMUM_SIZE   = new GSBiLayoutProperty<>(MINIMUM_WIDTH, MINIMUM_HEIGHT, GSDimension::new, GSDimension::getWidth, GSDimension::getHeight);

	public static final GSILayoutProperty<Integer> PREFERRED_WIDTH    = new GSIntLayoutProperty("preferredWidth" , GSPanel::getDefaultPreferredWidth , 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer> PREFERRED_HEIGHT   = new GSIntLayoutProperty("preferredHeight", GSPanel::getDefaultPreferredHeight, 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<GSDimension> PREFERRED_SIZE = new GSBiLayoutProperty<>(PREFERRED_WIDTH, PREFERRED_HEIGHT, GSDimension::new, GSDimension::getWidth, GSDimension::getHeight);
	
	public static final GSILayoutProperty<Integer> GRID_X        = new GSIntLayoutProperty("gridX"     , 0, 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer> GRID_Y        = new GSIntLayoutProperty("gridY"     , 0, 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer> GRID_WIDTH    = new GSIntLayoutProperty("gridWidth" , 1, 1, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer> GRID_HEIGHT   = new GSIntLayoutProperty("gridHeight", 1, 1, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Float>   WEIGHT_X      = new GSFloatLayoutProperty("weightX" , 0.0f, 0.0f, Float.POSITIVE_INFINITY);
	public static final GSILayoutProperty<Float>   WEIGHT_Y      = new GSFloatLayoutProperty("weightY" , 0.0f, 0.0f, Float.POSITIVE_INFINITY);

	public static final GSILayoutProperty<GSEAnchor> ANCHOR      = new GSBasicLayoutProperty<>("anchor", GSEAnchor.CENTER);
	public static final GSILayoutProperty<GSEFill>   FILL        = new GSBasicLayoutProperty<>("fill"  , GSEFill.NONE);
	
	public static final GSILayoutProperty<Integer>   TOP_MARGIN    = new GSIntLayoutProperty("topMargin"   , 0, 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer>   LEFT_MARGIN   = new GSIntLayoutProperty("leftMargin"  , 0, 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer>   BOTTOM_MARGIN = new GSIntLayoutProperty("bottomMargin", 0, 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<Integer>   RIGHT_MARGIN  = new GSIntLayoutProperty("rightMargin" , 0, 0, Integer.MAX_VALUE);
	public static final GSILayoutProperty<GSSpacing> MARGIN        = new GSSpacingLayoutProperty(TOP_MARGIN, LEFT_MARGIN, BOTTOM_MARGIN, RIGHT_MARGIN);
	
	private GSLayoutProperties() {
	}
}
