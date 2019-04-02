
package fit.cbs.op;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fit.layout.api.Parameter;
import org.fit.layout.impl.BaseOperator;
import org.fit.layout.impl.ParameterBoolean;
import org.fit.layout.impl.ParameterFloat;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;

import fit.cbs.data.CBSElType;
import fit.cbs.data.CBSPosition;
import fit.cbs.model.CBSBox;
import fit.cbs.model.CBSCluster;
import fit.cbs.model.CBSRect;
import fit.cbs.model.SemiAlg;

/**
 * The operator providing the whole CBS.
 * 
 * @author xlenga01
 * @author burgetr
 */
public class CBSOperator extends BaseOperator
{
    private ArrayList<CBSBox> bcsboxes;
    private ArrayList<SemiAlg> preCouples;
    private ArrayList<SemiAlg> couples;
    private ArrayList<CBSCluster> clusters;
    private float CT;
    private boolean calcImgColor;

    
    public CBSOperator()
    {
        this.CT = 0.5f;
        this.calcImgColor = true;
    }

    public CBSOperator(float attr, boolean calcColor)
    {
        this.CT = attr;
        this.calcImgColor = calcColor;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Segm.BCS";
    }

    @Override
    public String getName()
    {
        return "Block Clustering Segmentation";
    }

    @Override
    public String getDescription()
    {
        return "Implements the Block Clustering Segmentation methods on leaf areas"
                + " (FlattenTreeOperator should be used before)";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>();
        ret.add(new ParameterFloat("CT"));
        ret.add(new ParameterBoolean("calcImgColor"));
        return ret;
    }
    
    @Override
    public String getCategory()
    {
        return "restructure";
    }

    public float getCT()
    {
        return this.CT;
    }

    public void setCT(float CT)
    {
        this.CT = CT;
    }

    public boolean isCalcImgColor()
    {
        return calcImgColor;
    }

    public void setCalcImgColor(boolean calcImgColor)
    {
        this.calcImgColor = calcImgColor;
    }
    
    //=========================================================================================
    
    @Override
    public void apply(AreaTree atree, Area root)
    {
        apply(atree);
    }

    @Override
    public void apply(AreaTree atree)
    {

        AreaImpl pom = (AreaImpl) atree.getRoot();
        List<Area> areas = pom.getChildren();

        this.bcsboxes = new ArrayList<CBSBox>();

        // vytvoreni bcsboxu
        for (Iterator<Area> i = areas.iterator(); i.hasNext();)
        {
            Area item = i.next();
            if (item instanceof AreaImpl)
            {
                CBSBox newBox = new CBSBox((AreaImpl) item, this.calcImgColor);
                if (newBox.getHeight() > 0 && newBox.getWidth() > 0)
                {
                    this.bcsboxes.add(newBox);
                }
            }

        }

        this.couples = new ArrayList<SemiAlg>();
        this.preCouples = new ArrayList<SemiAlg>();

        // nalezeni semiALG boxu
        for (Iterator<CBSBox> i = this.bcsboxes.iterator(); i.hasNext();)
        {

            CBSBox m = i.next();

            for (Iterator<CBSBox> j = this.bcsboxes.iterator(); j.hasNext();)
            {
                CBSBox n = j.next();
                if (m.equals(n))
                {
                    continue;
                }
                CBSPosition pos = m.getBCSPos(n);
                if (pos != CBSPosition.other && !isCouple(m, n))
                {
                    SemiAlg alg = new SemiAlg(m, n, pos);
                    this.preCouples.add(alg);
                }
            }
        }

        for (Iterator<SemiAlg> i = this.preCouples.iterator(); i.hasNext();)
        {
            SemiAlg item = i.next();
            item.recalcNeightbourse();
        }

        for (Iterator<SemiAlg> i = this.preCouples.iterator(); i.hasNext();)
        {
            SemiAlg item = i.next();
            if (item.isDirNgb())
            {
                couples.add(item);
            }
        }

        for (Iterator<SemiAlg> i = this.couples.iterator(); i.hasNext();)
        {
            SemiAlg item = i.next();
            item.calcSim();
        }

        this.clusters = new ArrayList<CBSCluster>();

        while (this.couples.size() > 0)
        {
            // while(this.bcsboxes.size() > 1){
            SemiAlg alg = getClousestCouple();

            if (alg == null)
            {
                break;
            }

            this.couples.remove(alg);

            double sim = alg.getSim();

            if (sim > this.CT)
            {
                break;
            }

            // create cluster
            CBSCluster seed = new CBSCluster(alg.getM(), alg.getN());

            if (overlaps(seed, alg))
            {
                continue;
            }

            if (alg.getM().getType() == CBSElType.cluster)
            {
                this.clusters.remove(alg.getM());
            }
            if (alg.getN().getType() == CBSElType.cluster)
            {
                this.clusters.remove(alg.getN());
            }

            this.bcsboxes.removeAll(seed.getBoxes());

            recalcNGb(seed);
            this.clusters.add(seed);

        }

        // TODO refactoring
        pom = (AreaImpl) atree.getRoot();
        areas = pom.getChildren();
        areas.clear();

        for (Iterator<CBSCluster> i = this.clusters.iterator(); i.hasNext();)
        {
            CBSCluster c = i.next();
            AreaImpl area = new AreaImpl(c.getX1(), c.getY1(), c.getX2(),
                    c.getY2());
            for (Iterator<CBSBox> j = c.getBoxes().iterator(); j.hasNext();)
            {
                area.addBox(j.next().getBox());
            }
            areas.add(area);
        }

    }
    
    //=========================================================================================
    
    private boolean overlaps(CBSCluster seed, SemiAlg alg)
    {
        ArrayList<CBSCluster> clusterComp = new ArrayList<CBSCluster>();

        CBSRect m = alg.getM();
        CBSRect n = alg.getN();

        if (m.getType() == CBSElType.cluster)
        {
            clusterComp.add((CBSCluster) m);
        }
        if (n.getType() == CBSElType.cluster)
        {
            clusterComp.add((CBSCluster) n);
        }

        ArrayList<CBSBox> newBoxes = new ArrayList<CBSBox>();

        for (Iterator<CBSBox> i = this.bcsboxes.iterator(); i.hasNext();)
        {
            CBSBox box = i.next();
            if (seed.getBoxes().contains(box))
            {
                continue;
            }
            if (seed.overlaps(box))
            {
                newBoxes.add(box);
            }
        }
        for (Iterator<CBSCluster> i = this.clusters.iterator(); i.hasNext();)
        {
            CBSCluster c = i.next();
            if (clusterComp.contains(c))
            {
                continue;
            }
            if (seed.overlaps(c))
            {
                newBoxes.addAll(c.getBoxes());
                clusterComp.add(c);
            }
        }

        for (Iterator<CBSBox> i = newBoxes.iterator(); i.hasNext();)
        {
            seed.addBox(i.next());
        }

        for (Iterator<CBSBox> i = this.bcsboxes.iterator(); i.hasNext();)
        {
            CBSBox box = i.next();
            if (seed.getBoxes().contains(box))
            {
                continue;
            }
            if (seed.overlaps(box))
            { return true; }
        }

        for (Iterator<CBSCluster> i = this.clusters.iterator(); i.hasNext();)
        {
            CBSCluster item = i.next();
            if (clusterComp.contains(item))
            {
                continue;
            }
            if (seed.overlaps(item))
            { return true; }
        }

        for (Iterator<CBSCluster> i = clusterComp.iterator(); i.hasNext();)
        {
            this.clusters.remove(i.next());
        }

        return false;
        /*
         * ArrayList<CBSCluster> clusterComp = new ArrayList<CBSCluster>();
         * 
         * CBSRect m = alg.getM(); CBSRect n = alg.getN();
         * 
         * if(m.getType() == CBSElType.cluster){ clusterComp.add((CBSCluster)
         * m); } if(n.getType() == CBSElType.cluster){
         * clusterComp.add((CBSCluster) n); }
         * 
         * 
         * ArrayList<CBSBox> newBoxes = new ArrayList<CBSBox>();
         * 
         * for(Iterator<CBSBox> i = this.bcsboxes.iterator(); i.hasNext();){
         * CBSBox box = i.next(); if(seed.getBoxes().contains(box)){ continue; }
         * if(seed.overlaps(box)){ newBoxes.add(box); } }
         * 
         * 
         * for(Iterator<CBSBox> i = newBoxes.iterator(); i.hasNext();){
         * seed.addBox(i.next()); }
         * 
         * for(Iterator<CBSBox> i = this.bcsboxes.iterator(); i.hasNext();){
         * CBSBox box = i.next(); if(seed.getBoxes().contains(box)){ continue; }
         * if(seed.overlaps(box)){ return true; } }
         * 
         * 
         * for(Iterator<CBSCluster> i = this.clusters.iterator(); i.hasNext();){
         * CBSCluster item = i.next(); if(clusterComp.contains(item)){ continue;
         * } if(seed.overlaps(item)){ return true; } }
         * 
         * return false;
         */
    }

    private SemiAlg getClousestCouple()
    {
        double pomSim = Double.MAX_VALUE;
        SemiAlg ret = null;

        for (Iterator<SemiAlg> i = this.couples.iterator(); i.hasNext();)
        {
            SemiAlg item = i.next();
            if (item.getSim() < pomSim)
            {
                pomSim = item.getSim();
                ret = item;
            }
        }
        return ret;
    }

    private boolean isCouple(CBSBox m, CBSBox n)
    {
        for (Iterator<SemiAlg> i = this.preCouples.iterator(); i.hasNext();)
        {
            SemiAlg item = i.next();
            if (item.consistsOf(m, n))
            { return true; }
        }
        return false;
    }

    private void recalcNGb(CBSCluster c)
    {

        ArrayList<SemiAlg> newCouples = new ArrayList<SemiAlg>();

        for (Iterator<CBSBox> i = this.bcsboxes.iterator(); i.hasNext();)
        {
            CBSBox item = i.next();

            newCouples.addAll(c.recNgbBox(item));
        }

        for (Iterator<CBSCluster> i = this.clusters.iterator(); i.hasNext();)
        {
            CBSCluster item = i.next();
            newCouples.addAll(c.recNgbCluster(item));
        }

        for (Iterator<SemiAlg> i = this.couples.iterator(); i.hasNext();)
        {
            SemiAlg item = i.next();
            CBSRect m = item.getM();
            CBSRect n = item.getN();

            if (m.getType() == CBSElType.box && n.getType() == CBSElType.box)
            {
                if (c.getBoxes().contains(m) || c.getBoxes().contains(n))
                {
                    continue;
                }
                else
                {
                    newCouples.add(item);
                }
            }

            else if (m.getType() == CBSElType.cluster
                    && n.getType() == CBSElType.cluster)
            {
                CBSCluster mC = (CBSCluster) m;
                CBSCluster nC = (CBSCluster) n;
                if (c.getBoxes().containsAll(mC.getBoxes())
                        || c.getBoxes().containsAll(nC.getBoxes()))
                {
                    continue;
                }
                else
                {
                    newCouples.add(item);
                }
            }
            else if (m.getType() == CBSElType.cluster)
            {
                CBSCluster mC = (CBSCluster) m;

                if (c.getBoxes().containsAll(mC.getBoxes())
                        || c.getBoxes().contains(n))
                {
                    continue;
                }
                else
                {
                    newCouples.add(item);
                }
            }
            else if (n.getType() == CBSElType.cluster)
            {
                CBSCluster nC = (CBSCluster) n;

                if (c.getBoxes().contains(m)
                        || c.getBoxes().containsAll(nC.getBoxes()))
                {
                    continue;
                }
                else
                {
                    newCouples.add(item);
                }
            }
            else
            {
                continue;
            }
        }
        this.couples = newCouples;

    }

}
