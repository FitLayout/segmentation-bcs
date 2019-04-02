package fit.cbs.model;

import org.fit.layout.model.Rect;

import fit.cbs.data.CBSElType;
import fit.cbs.data.CBSPosition;

/**
 * Basic BCS rectangle interface.
 *  
 * @author xlenga01
 */
public interface CBSRect extends Rect 
{
	
	/**
	 * Obtains the elemen type: box/cluster
	 * @return the element type
	 */
	CBSElType getType();

	
	/**
	 * Recalculates the nearest neighbors of the box/cluster
	 * @param couple a cluster pair for which a new neighbor is computed
	 * @param pos the position for which the computation is performed 
	 */
	void recNgb(SemiAlg couple, CBSPosition pos);
}
