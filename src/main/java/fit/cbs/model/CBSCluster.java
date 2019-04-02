
package fit.cbs.model;

import java.util.ArrayList;
import java.util.Iterator;

import fit.cbs.data.CBSElType;
import fit.cbs.data.CBSPosition;

/**
 * A cluster implementation
 * 
 * @author xlenga01
 *
 */
public class CBSCluster implements CBSRect
{

    private int x1;
    private int x2;
    private int y1;
    private int y2;

    private ArrayList<CBSBox> boxes;
    private ArrayList<SemiAlg> dirNgb;

    public CBSCluster(CBSRect b1, CBSRect b2)
    {

        this.boxes = new ArrayList<CBSBox>();
        this.dirNgb = new ArrayList<SemiAlg>();

        if (b1.getType() == CBSElType.box)
        {
            this.boxes.add((CBSBox) b1);
        }
        else
        {
            CBSCluster c = (CBSCluster) b1;
            this.boxes.addAll(c.getBoxes());
        }

        if (b2.getType() == CBSElType.box)
        {
            this.boxes.add((CBSBox) b2);
        }
        else
        {
            CBSCluster c = (CBSCluster) b2;
            this.boxes.addAll(c.getBoxes());
        }

        this.x1 = Integer.MAX_VALUE;
        this.x2 = Integer.MIN_VALUE;
        this.y1 = Integer.MAX_VALUE;
        this.y2 = Integer.MIN_VALUE;

        for (Iterator<CBSBox> i = this.boxes.iterator(); i.hasNext();)
        {
            CBSBox it = i.next();
            recalcBoundries(it);
        }
    }

    public int getX1()
    {
        return x1;
    }

    public int getY1()
    {
        return y1;
    }

    public int getX2()
    {
        return x2;
    }

    public int getY2()
    {
        return y2;
    }

    public int getWidth()
    {
        return x2 - x1;
    }

    public int getHeight()
    {
        return y2 - y1;
    }

    public void move(int xofs, int yofs)
    {
        // unsupported
    }

    public CBSElType getType()
    {
        return CBSElType.cluster;
    }

    public void recNgb(SemiAlg couple, CBSPosition pos)
    {
        // unsupporte
    }

    public ArrayList<CBSBox> getBoxes()
    {
        return this.boxes;
    }

    /**
     * Inserts a box to a cluster and recomputes the cluster bounds
     * 
     * @param box the box to be inserted
     */
    public void addBox(CBSBox box)
    {
        this.boxes.add(box);
        recalcBoundries(box);
    }

    private void recalcBoundries(CBSBox box)
    {
        if (box.getX1() < this.x1)
        {
            this.x1 = box.getX1();
        }
        if (box.getX2() > this.x2)
        {
            this.x2 = box.getX2();
        }
        if (box.getY1() < this.y1)
        {
            this.y1 = box.getY1();
        }
        if (box.getY2() > this.y2)
        {
            this.y2 = box.getY2();
        }
    }

    /**
     * Kontroluje, zda se cluster prekryva s elementem
     * 
     * @param c
     *            elemet (box/cluster), ktery se kontroluje
     * @return true pokud se elementy prekrivaji
     */
    public boolean overlaps(CBSRect c)
    {

        if (this.x1 >= c.getX2() || this.x2 <= c.getX1())
        {
            return false;
        }
        else if (this.y1 >= c.getY2() || this.y2 <= c.getY1())
        { return false; }
        return true;

    }

    /**
     * Prepocita sousedy clusteru
     * 
     * @param box
     *            box ktery se kontroluje na sousednost
     * @return mnozinu novych dvojic patrici do nejblizsich sousedu
     */
    public ArrayList<SemiAlg> recNgbBox(CBSBox box)
    {

        ArrayList<SemiAlg> ret = new ArrayList<SemiAlg>();

        if (this.boxes.contains(box))
        { return ret; }

        for (Iterator<CBSBox> i = this.boxes.iterator(); i.hasNext();)
        {
            CBSBox item = i.next();
            CBSPosition pos = box.isNgb(item);
            if (pos != CBSPosition.other)
            {
                SemiAlg pomAlg = new SemiAlg(this, box, pos);
                this.dirNgb.add(pomAlg);
                pomAlg.calcSim();
                ret.add(pomAlg);
                break;
                // box.setNgb(pomAlg, pos);
            }
        }

        return ret;

    }

    /**
     * Prepocita sousedy clusteru
     * 
     * @param c
     *            cluster ktery se kontroluje na sousednost
     * @return mnozinu novych dvojic patrici do nejblizsich sousedu
     */
    public ArrayList<SemiAlg> recNgbCluster(CBSCluster c)
    {
        ArrayList<SemiAlg> ret = new ArrayList<SemiAlg>();

        if (this.getBoxes().containsAll(c.getBoxes()))
        { return ret; }
        for (Iterator<CBSBox> i = this.boxes.iterator(); i.hasNext();)
        {

            CBSBox m = i.next();
            for (Iterator<CBSBox> j = c.getBoxes().iterator(); j.hasNext();)
            {
                CBSBox n = j.next();
                CBSPosition pos = m.isNgb(n);
                if (pos != CBSPosition.other)
                {
                    SemiAlg pomAlg = new SemiAlg(this, c, pos);// obracene
                                                               // poradi
                                                               // argumentu
                    this.dirNgb.add(pomAlg);
                    pomAlg.calcSim();
                    ret.add(pomAlg);
                    return ret;
                }
                else
                {
                    pos = n.isNgb(m);
                    if (pos != CBSPosition.other)
                    {
                        // this.dirNgb.add(c);
                        SemiAlg pomAlg = new SemiAlg(c, this, pos);// obracene
                                                                   // poradi
                                                                   // argumentu
                        this.dirNgb.add(pomAlg);
                        pomAlg.calcSim();
                        ret.add(pomAlg);
                        return ret;
                    }
                }
            }
        }

        return ret;

    }

    /**
     * @return vsechny sousedici boxi
     */
    public ArrayList<CBSBox> getNgbBoxes()
    {
        ArrayList<CBSBox> ret = new ArrayList<CBSBox>();

        for (Iterator<SemiAlg> i = this.dirNgb.iterator(); i.hasNext();)
        {
            SemiAlg item = i.next();
            if (item.getOtherBox(this).getType() == CBSElType.box)
            {
                CBSBox pom = (CBSBox) item.getOtherBox(this);
                ret.add(pom);
            }
        }

        return ret;
    }

    /**
     * Vypocitani podobnosti mezi clusterem a boxem nebo dvema clustery
     * 
     * @param rect
     *            druhy box/cluster
     * @return vraci podobnost
     */
    public double calcSim(CBSRect rect)
    {
        double ret = 0;

        if (rect.getType() == CBSElType.box)
        {
            double card = 0;
            double cumul = 0;
            CBSBox box = (CBSBox) rect;

            for (Iterator<CBSBox> i = this.boxes.iterator(); i.hasNext();)
            {
                CBSBox pomBox = i.next();

                for (Iterator<SemiAlg> j = box.getDirNgb().iterator(); j
                        .hasNext();)
                {
                    SemiAlg sa = j.next();
                    CBSRect r = sa.getOtherBox(box);
                    if (r.equals(pomBox))
                    {
                        card += 1;
                        cumul += sa.getSim();
                        break;
                    }
                }
            }
            if (card == 0)
            { return 0; }
            ret = cumul / card;
        }
        else if (rect.getType() == CBSElType.cluster)
        {
            CBSCluster cluster = (CBSCluster) rect;
            double card = 0;
            double cumul = 0;
            for (Iterator<CBSBox> i = this.boxes.iterator(); i.hasNext();)
            {
                CBSBox box = i.next();
                double sim = cluster.calcSim(box);
                if (sim > 0)
                {
                    card++;
                    cumul += sim;
                }
            }
            if (card == 0)
            { return 0; }
            return cumul / card;
        }

        return ret;
    }

}
