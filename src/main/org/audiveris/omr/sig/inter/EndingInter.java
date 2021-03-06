//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                      E n d i n g I n t e r                                     //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//
//  Copyright © Hervé Bitteur and others 2000-2017. All rights reserved.
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the
//  GNU Affero General Public License as published by the Free Software Foundation, either version
//  3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
//  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//  See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this
//  program.  If not, see <http://www.gnu.org/licenses/>.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package org.audiveris.omr.sig.inter;

import org.audiveris.omr.glyph.Shape;
import org.audiveris.omr.sig.BasicImpacts;
import org.audiveris.omr.sig.GradeImpacts;

import java.awt.Rectangle;
import java.awt.geom.Line2D;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class {@code EndingInter} represents an ending.
 *
 * @author Hervé Bitteur
 */
@XmlRootElement(name = "ending")
public class EndingInter
        extends AbstractInter
{
    //~ Instance fields ----------------------------------------------------------------------------

    /** Mandatory left leg. */
    @XmlElement(name = "left-leg")
    private final Line2D leftLeg;

    /** Horizontal line. */
    @XmlElement
    private final Line2D line;

    /** Optional right leg, if any. */
    @XmlElement(name = "right-leg")
    private final Line2D rightLeg;

    private final SegmentInter segment;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new EndingInter object.
     *
     * @param segment  horizontal segment
     * @param line     precise line
     * @param leftLeg  mandatory left leg
     * @param rightLeg optional right leg
     * @param bounds   bounding box
     * @param impacts  assignments details
     */
    public EndingInter (SegmentInter segment,
                        Line2D line,
                        Line2D leftLeg,
                        Line2D rightLeg,
                        Rectangle bounds,
                        GradeImpacts impacts)
    {
        super(null, bounds, Shape.ENDING, impacts);
        this.segment = segment;
        this.line = line;
        this.leftLeg = leftLeg;
        this.rightLeg = rightLeg;
    }

    /**
     * No-arg constructor meant for JAXB.
     */
    private EndingInter ()
    {
        this.segment = null;
        this.line = null;
        this.leftLeg = null;
        this.rightLeg = null;
    }

    //~ Methods ------------------------------------------------------------------------------------
    //--------//
    // accept //
    //--------//
    @Override
    public void accept (InterVisitor visitor)
    {
        visitor.visit(this);
    }

    //-----------//
    // getBounds //
    //-----------//
    @Override
    public Rectangle getBounds ()
    {
        Rectangle box = super.getBounds();

        if (box != null) {
            return box;
        }

        box = line.getBounds().union(leftLeg.getBounds());

        if (rightLeg != null) {
            box = box.union(rightLeg.getBounds());
        }

        return new Rectangle(bounds = box);
    }

    /**
     * @return the leftLeg
     */
    public Line2D getLeftLeg ()
    {
        return leftLeg;
    }

    /**
     * @return the line
     */
    public Line2D getLine ()
    {
        return line;
    }

    /**
     * @return the rightLeg
     */
    public Line2D getRightLeg ()
    {
        return rightLeg;
    }

    //~ Inner Classes ------------------------------------------------------------------------------
    //---------//
    // Impacts //
    //---------//
    public static class Impacts
            extends BasicImpacts
    {
        //~ Static fields/initializers -------------------------------------------------------------

        private static final String[] NAMES = new String[]{
            "straight", "slope", "length", "leftBar",
            "rightBar"
        };

        private static final double[] WEIGHTS = new double[]{1, 1, 1, 1, 1};

        //~ Constructors ---------------------------------------------------------------------------
        public Impacts (double straight,
                        double slope,
                        double length,
                        double leftBar,
                        double rightBar)
        {
            super(NAMES, WEIGHTS);
            setImpact(0, straight);
            setImpact(1, slope);
            setImpact(2, length);
            setImpact(3, leftBar);
            setImpact(4, rightBar);
        }
    }
}
