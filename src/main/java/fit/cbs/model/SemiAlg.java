
package fit.cbs.model;

import java.awt.Color;
import java.lang.Math;

import fit.cbs.data.CBSElType;
import fit.cbs.data.CBSPosition;

/**
 * A pair of two joining candidates.
 * 
 * @author xlenga01
 */
public class SemiAlg
{
    private CBSRect m;
    private CBSRect n;

    private double sim;
    private CBSPosition pos;
    private double absPos;
    private boolean isDirNgb;

    public SemiAlg(CBSRect m, CBSRect n, CBSPosition pos)
    {
        this.m = m;
        this.n = n;
        this.pos = pos;
        if (m.getType() == CBSElType.box && n.getType() == CBSElType.box)
        {
            this.absPos = calcAbsPos();
        }
        this.isDirNgb = false;
    }

    private void calcBaseSim()
    {
        double pos = calcSimPos();

        if (pos <= 0)
        {
            this.sim = 0;
        }
        else if (pos >= 1)
        {
            this.sim = 1;
        }
        else
        {
            this.sim = (pos + Math.abs(calcSimColor()) + calcSimShape()) / 3;
        }

        if (this.sim > 1)
        {
            this.sim = 1;
        }

    }

    /**
     * Similarity computation, sets the sim parameter.
     */
    public void calcSim()
    {
        if (m.getType() == CBSElType.box && n.getType() == CBSElType.box)
        {
            calcBaseSim();
        }
        else if (m.getType() == CBSElType.cluster
                && n.getType() == CBSElType.box)
        {
            CBSCluster mC = (CBSCluster) m;
            CBSBox nB = (CBSBox) n;
            this.sim = mC.calcSim(nB);
        }

        else if (m.getType() == CBSElType.cluster
                && n.getType() == CBSElType.cluster)
        {
            CBSCluster mC = (CBSCluster) m;
            CBSCluster nC = (CBSCluster) n;
            this.sim = mC.calcSim(nC);
        }
    }

    private double calcSimPos()
    {
        double relM = ((double) this.absPos) / maxDist(this.m);
        double relN = ((double) this.absPos) / maxDist(this.n);

        return (relM + relN) / 2;
    }

    private double calcSimShape()
    {
        return (Math.abs(calcRation()) + Math.abs(calcSize())) / 2;
        // return Math.abs(calcSize());
    }

    private double calcSimColor()
    {

        Color mCol = ((CBSBox) this.m).getColor();
        Color nCol = ((CBSBox) this.n).getColor();

        double redRoz = ((double) (nCol.getRed() - mCol.getRed())) / 255;
        double greenRoz = ((double) (nCol.getGreen() - mCol.getGreen())) / 255;
        double blueRoz = ((double) (nCol.getBlue() - mCol.getBlue())) / 255;

        double ret = Math.sqrt(Math.pow(redRoz, 2.0) + Math.pow(greenRoz, 2.0)
                + Math.pow(blueRoz, 2.0)) / Math.sqrt(3);

        return ret;
        // return 0.5;
    }

    private double calcRation()
    {
        double rM = ((double) m.getWidth()) / m.getHeight();
        double rN = ((double) n.getWidth()) / n.getHeight();

        double pom = Math.pow(Math.max(rM, rN), 2) - 1;
        if (pom == 1)
        {
            pom = 1.01;
        }

        return (Math.max(rM, rN) - Math.min(rM, rN))
                / ((pom) / (Math.max(rM, rN)));
    }

    private double calcSize()
    {
        double sM = ((double) m.getWidth()) * m.getHeight();
        double sN = ((double) n.getWidth()) * n.getHeight();

        return 1 - (Math.min(sM, sN) / Math.max(sM, sN));
    }

    private double calcAbsPos()
    {

        CBSBox m = (CBSBox) this.m;
        CBSBox n = (CBSBox) this.n;

        CBSPosition pos = m.getBCSPos(n);

        if (pos == CBSPosition.above)
        {
            return n.getY1() - m.getY2();
        }
        else if (pos == CBSPosition.below)
        {
            return m.getY1() - n.getY2();
        }
        else if (pos == CBSPosition.left)
        {
            return n.getX1() - m.getX2();
        }
        else if (pos == CBSPosition.right)
        { return m.getX1() - n.getX2(); }

        return Double.POSITIVE_INFINITY;
    }

    private double maxDist(CBSRect box)
    {
        CBSBox bcs = (CBSBox) box;
        return bcs.maxNgbDist();
    }

    public boolean consistsOf(CBSRect m, CBSRect n)
    {
        if (this.m.equals(m) && this.n.equals(n))
        {
            return true;
        }
        else if (this.n.equals(m) && this.m.equals(n))
        { return true; }
        return false;
    }

    /**
     * Neighbor recomputatuib for n and m
     */
    public void recalcNeightbourse()
    {
        this.m.recNgb(this, this.pos);
        this.n.recNgb(this, CBSPosition.getReverse(this.pos));
    }

    public double getAbsPos()
    {
        return this.absPos;
    }

    public double getSim()
    {
        return this.sim;
    }

    public CBSRect getN()
    {
        return this.n;
    }

    public CBSRect getM()
    {
        return this.m;
    }

    /**
     * Returns the mutual position of both elements
     * 
     * @param rect
     *            vstupni element
     * @return pozice
     */
    public CBSPosition getPos(CBSRect rect)
    {
        if (this.m.equals(rect))
        {
            return this.pos;
        }
        else if (this.n.equals(rect))
        { return CBSPosition.getReverse(this.pos); }
        return null;
    }

    /**
     * Returns the complementary element
     * 
     * @param rec
     * @return
     */
    public CBSRect getOtherBox(CBSRect rec)
    {
        if (this.m.equals(rec))
            return this.n;
        else if (this.n.equals(rec))
            return this.m;
        return null;
    }

    public boolean isDirNgb()
    {
        return this.isDirNgb;
    }

    public void setIsDirNgb(boolean b)
    {
        this.isDirNgb = b;

    }
}
