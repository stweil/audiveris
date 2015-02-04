//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                    F u l l B e a m I n t e r                                   //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Herve Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.sig.inter;

import omr.glyph.Shape;
import omr.glyph.facets.Glyph;

import omr.sig.GradeImpacts;

import java.awt.geom.Line2D;

/**
 * Class {@code FullAbstractBeamInter} represents a full beam interpretation, as opposed
 * to a beam hook interpretation.
 *
 * @see BeamHookInter
 *
 * @author Hervé Bitteur
 */
public class FullBeamInter
        extends AbstractBeamInter
{
    //~ Constructors -------------------------------------------------------------------------------

    /**
     * Creates a new FullBeamInter object.
     *
     * @param glyph   the underlying glyph
     * @param impacts the grade details
     * @param median  median beam line
     * @param height  beam height
     */
    public FullBeamInter (Glyph glyph,
                          GradeImpacts impacts,
                          Line2D median,
                          double height)
    {
        super(glyph, Shape.BEAM, impacts, median, height);
    }
}