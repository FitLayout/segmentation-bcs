
package fit.cbs.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.fit.layout.model.Box;
import org.fit.layout.model.ContentImage;
import org.fit.layout.model.ContentObject;
import org.fit.layout.model.Rectangular;
import org.fit.segm.grouping.AreaImpl;

import fit.cbs.data.CBSElType;
import fit.cbs.data.CBSPosition;

/**
 * A basic block implementation
 * 
 * @author xlenga01
 *
 */
public class CBSBox implements CBSRect
{

    private int x1;
    private int x2;
    private int y1;
    private int y2;

    private double maxDist;

    private List<SemiAlg> dirNgb;

    private Box box;
    private Color color;

    public CBSBox(AreaImpl ref, boolean calcCol)
    {
        this.box = ref.getBoxes().firstElement();
        Rectangular box = this.box.getBounds();

        this.x1 = box.getX1();
        this.x2 = box.getX2();
        this.y1 = box.getY1();
        this.y2 = box.getY2();

        if (this.box.getColor() != null)
        {
            this.color = this.box.getColor();
        }
        else
        {
            this.color = Color.BLACK;
        }
        ContentObject obj = this.box.getContentObject();
        if (obj != null && calcCol)
        {
            BufferedImage image = null;
            if (obj instanceof ContentImage)
            {
                final ContentImage img = (ContentImage) obj;
                try
                {
                    image = ImageIO.read(img.getUrl());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                if (image != null)
                {
                    this.color = averageColor(image, 0, 0, image.getWidth(),
                            image.getHeight());
                }
            }
        }

        this.dirNgb = new ArrayList<SemiAlg>();

    }

    public int getX1()
    {
        return this.x1;
    }

    public int getY1()
    {
        return this.y1;
    }

    public int getX2()
    {
        return this.x2;
    }

    public int getY2()
    {
        return this.y2;
    }

    public int getWidth()
    {
        return this.x2 - this.x1;
    }

    public int getHeight()
    {
        return this.y2 - this.y1;
    }

    public void move(int xofs, int yofs)
    {
        // not supported
    }

    /**
     * Gets the relative position to the input box.
     * 
     * @param box the input box
     * @return the position
     */
    public CBSPosition getBCSPos(CBSBox box)
    {
        if (this.x2 >= box.getX1() && this.x1 <= box.getX2())
        {
            if (this.y2 <= box.getY1())
            {
                return CBSPosition.above;
            }
            else if (this.y1 >= box.getY2())
            { return CBSPosition.below; }
        }
        else if (this.y2 >= box.getY1() && this.y1 <= box.getY2())
        {
            if (this.x2 <= box.getX1())
            {
                return CBSPosition.left;
            }
            else if (this.x1 >= box.getX2())
            { return CBSPosition.right; }
        }

        return CBSPosition.other;

    }

    public CBSElType getType()
    {
        return CBSElType.box;
    }

    public Color getColor()
    {
        return this.color;
    }

    public void recNgb(SemiAlg couple, CBSPosition pos)
    {
        double pomPos = couple.getAbsPos();
        if (pomPos > this.maxDist)
        {
            this.maxDist = pomPos;
        }

        SemiAlg pomAlg = null;

        for (Iterator<SemiAlg> i = this.dirNgb.iterator(); i.hasNext();)
        {
            SemiAlg sa = i.next();
            if (sa.getPos(this) == pos)
            {
                pomAlg = sa;
                break;
            }
        }

        if (pomAlg == null)
        {
            couple.setIsDirNgb(true);
            this.dirNgb.add(couple);
        }
        else
        {
            if (pomPos < pomAlg.getAbsPos())
            {
                ArrayList<SemiAlg> delList;
                delList = new ArrayList<SemiAlg>();
                for (Iterator<SemiAlg> i = this.dirNgb.iterator(); i.hasNext();)
                {
                    SemiAlg sa = i.next();
                    if (sa.getPos(this) == pos)
                    {
                        sa.setIsDirNgb(false);
                        delList.add(sa);
                    }
                }
                this.dirNgb.removeAll(delList);
                couple.setIsDirNgb(true);
                this.dirNgb.add(couple);

            }
            else if (pomPos == pomAlg.getAbsPos())
            {
                couple.setIsDirNgb(true);
                this.dirNgb.add(couple);
            }
        }

    }

    /**
     * 
     * @return maximal absolute distance
     */
    public double maxNgbDist()
    {
        return this.maxDist;
    }

    /**
     * Checks whether the element is the nearest neighbor
     * 
     * @param rect the input element (box/cluster)
     * @return true when the element is the nearest neighbor
     */
    public CBSPosition isNgb(CBSRect rect)
    {

        for (Iterator<SemiAlg> i = this.dirNgb.iterator(); i.hasNext();)
        {
            SemiAlg sa = i.next();
            if (rect.equals(sa.getOtherBox(this)))
                return CBSPosition.getReverse(sa.getPos(this));
        }
        return CBSPosition.other;

    }

    public List<SemiAlg> getDirNgb()
    {
        return this.dirNgb;
    }

    public String getAreaDesc()
    {
        return this.box.getText();
    }

    public Box getBox()
    {
        return this.box;
    }

    private Color averageColor(BufferedImage bi, int x0, int y0, int w, int h)
    {
        final int x1 = x0 + w;
        final int y1 = y0 + h;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = x0; x < x1; x++)
        {
            for (int y = y0; y < y1; y++)
            {
                Color pixel = new Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        int num = w * h;
        float red = (float) (sumr / num) / 255;
        float green = (float) (sumg / num) / 255;
        float blue = (float) (sumb / num) / 255;
        return new Color(red, green, blue);
    }

}
