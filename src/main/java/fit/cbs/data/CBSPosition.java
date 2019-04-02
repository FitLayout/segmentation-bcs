package fit.cbs.data;


/**
 * Defines different relationships between boxes
 * @author Redwings
 *
 */
public enum CBSPosition {
	above, below, left, right, other;

	/**
	 * Obtains an opposite relationship 
	 * @param pos Input relationship
	 * @return pozice The opposite relationship
	 */
	public static CBSPosition getReverse(CBSPosition pos) 
	{
		switch (pos)
		{
			case above:
				return below;
			case below:
				return above;
			case left:
				return right;
			case right:
				return left;
			default:
				return other;
		}
	}
}
