//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                     G l y p h s M o d e l                                      //
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
package org.audiveris.omr.glyph;

import org.audiveris.omr.classifier.Evaluation;
import org.audiveris.omr.classifier.SampleRepository;
import org.audiveris.omr.classifier.SampleSheet;
import org.audiveris.omr.sheet.Book;
import org.audiveris.omr.sheet.Sheet;
import org.audiveris.omr.step.Step;
import org.audiveris.omr.ui.selection.EntityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Class {@code GlyphsModel} is a common model for synchronous glyph handling.
 * <p>
 * Nota: User gesture should trigger actions in GlyphsController which will asynchronously delegate
 * to this model.
 *
 * @author Hervé Bitteur
 */
public class GlyphsModel
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(
            GlyphsModel.class);

    //~ Instance fields ----------------------------------------------------------------------------
    /** Underlying glyph service. */
    protected final EntityService<? extends Glyph> glyphService;

    /** Related Sheet, if any. */
    protected final Sheet sheet;

    /** Related Step, if any. */
    protected final Step step;

    /** Latest shape assigned, if any. */
    protected Shape latestShape;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Create an instance of GlyphsModel, with its underlying glyph glyphService.
     *
     * @param sheet        the related sheet (can be null)
     * @param glyphService the related sheetService (cannot be null)
     * @param step         the step after which update should be perform (can be null)
     */
    public GlyphsModel (Sheet sheet,
                        EntityService<? extends Glyph> glyphService,
                        Step step)
    {
        Objects.requireNonNull(glyphService, "Attempt to create a GlyphsModel with null service");

        this.sheet = sheet; // Null sheet is allowed (for SampleVerifier use)
        this.glyphService = glyphService;
        this.step = step;
    }

    //~ Methods ------------------------------------------------------------------------------------
    //--------------//
    // assignGlyphs //
    //--------------//
    /**
     * Assign a shape to the selected collection of glyphs.
     *
     * @param glyphs   the collection of glyphs to be assigned
     * @param shape    the shape to be assigned
     * @param compound flag to build one compound, rather than assign each
     *                 individual glyph
     * @param grade    the grade we have wrt the assigned shape
     */
    public void assignGlyphs (Collection<Glyph> glyphs,
                              Shape shape,
                              boolean compound,
                              double grade)
    {
        if (compound) {
            //            // Build & insert one compound
            //            Glyph glyph = nest.buildGlyph(glyphs, true);
            //
            //            assignGlyph(glyph, shape, grade);
        } else {
            // Assign each glyph individually
            for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
                assignGlyph(glyph, sheet.getScale().getInterline(), shape, grade);
            }
        }
    }

    //----------------//
    // deassignGlyphs //
    //----------------//
    /**
     * De-Assign a collection of glyphs.
     *
     * @param glyphs the collection of glyphs to be de-assigned
     */
    public void deassignGlyphs (Collection<Glyph> glyphs)
    {
        for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
            deassignGlyph(glyph);
        }
    }

    //--------------//
    // deleteGlyphs //
    //--------------//
    public void deleteGlyphs (Collection<Glyph> glyphs)
    {
        for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
            deleteGlyph(glyph);
        }
    }

    //-----------------//
    // getGlyphService //
    //-----------------//
    /**
     * Report the underlying glyph glyphService.
     *
     * @return the related glyph glyphService
     */
    public EntityService<? extends Glyph> getGlyphService ()
    {
        return glyphService;
    }

    //----------------//
    // getLatestShape //
    //----------------//
    /**
     * Report the latest non null shape that was assigned, or null if
     * none.
     *
     * @return latest shape assigned, or null if none
     */
    public Shape getLatestShape ()
    {
        return latestShape;
    }

    //----------------//
    // getRelatedStep //
    //----------------//
    /**
     * Report the step this GlyphsModel is used for, so that we know
     * from which step updates must be propagated.
     * (we have to update the steps that follow this one)
     *
     * @return the step related to this glyphs model
     */
    public Step getRelatedStep ()
    {
        return step;
    }

    //----------//
    // getSheet //
    //----------//
    /**
     * Report the model underlying sheet.
     *
     * @return the underlying sheet instance
     */
    public Sheet getSheet ()
    {
        return sheet;
    }

    //----------------//
    // setLatestShape //
    //----------------//
    /**
     * Assign the latest useful shape.
     *
     * @param shape the current / latest shape
     */
    public void setLatestShape (Shape shape)
    {
        if (shape != Shape.GLYPH_PART) {
            latestShape = shape;
        }
    }

    //-------------//
    // assignGlyph //
    //-------------//
    /**
     * Assign a Shape to a glyph, inserting the glyph to its containing
     * system and nest if it is still transient.
     *
     * @param glyph     the glyph to be assigned
     * @param interline global sheet interline
     * @param shape     the assigned shape, which may be null
     * @param grade     the grade about shape
     * @return the assigned glyph (perhaps an original glyph)
     */
    protected Glyph assignGlyph (Glyph glyph,
                                 int interline,
                                 Shape shape,
                                 double grade)
    {
        final Book book = sheet.getStub().getBook();
        final SampleRepository repository = book.getSampleRepository();
        final SampleSheet sampleSheet = repository.findSampleSheet(sheet);

        // TODO: we need staff information (-> interline and pitch)
        repository.addSample(shape, glyph, interline, sampleSheet, null);

        //        if (glyph == null) {
        //            return null;
        //        }
        //
        //        if (shape != null) {
        //            List<SystemInfo> systems = sheet.getSystemManager().getSystemsOf(glyph);
        //
        //            //            if (system != null) {
        //            //                glyph = system.register(glyph); // System then nest
        //            //            } else {
        //            //                // Insert in nest directly, which assigns an id to the glyph
        //            glyph = nest.register(glyph);
        //
        //            //            }
        //            boolean isTransient = glyph.isTransient();
        //            logger.debug(
        //                    "Assign {}{} to {}",
        //                    isTransient ? "compound " : "",
        //                    glyph.idString(),
        //                    shape);
        //
        //            // Remember the latest shape assigned
        //            setLatestShape(shape);
        //        }
        //
        //        // Do the assignment of the shape to the glyph
        //        glyph.setShape(shape);
        //
        //        // Should we persist the assigned glyph?
        //        if ((shape != null)
        //            && (grade == Evaluation.MANUAL)
        //            && (OMR.gui != null)
        //            && ScoreActions.getInstance().isManualPersisted()) {
        //            // Record the glyph description to disk
        //            SampleRepository.getInstance().recordOneGlyph(glyph, sheet);
        //        }
        //
        return glyph;
    }

    //---------------//
    // deassignGlyph //
    //---------------//
    /**
     * Deassign the shape of a glyph.
     *
     * @param glyph the glyph to deassign
     */
    protected void deassignGlyph (Glyph glyph)
    {
        // Assign the null shape to the glyph
        assignGlyph(glyph, sheet.getInterline(), null, Evaluation.ALGORITHM);
    }

    //-------------//
    // deleteGlyph //
    //-------------//
    protected void deleteGlyph (Glyph glyph)
    {
        logger.error("HB. Not yet implemented");

        //        if (glyph == null) {
        //            return;
        //        }
        //
        //        if (!glyph.isVirtual()) {
        //            logger.warn("Attempt to delete non-virtual {}", glyph.idString());
        //
        //            return;
        //        }
        //
        //        SystemInfo system = sheet.getSystemOf(glyph);
        //
        //        // Special case for ledger glyph
        //        if (glyph.getShape() == Shape.LEDGER) {
        //            StaffInfo staff = system.getStaffAt(glyph.getCenter());
        //
        //            ///staff.removeLedger(glyph);
        //            //TODO: handle a LedgerInter instead!
        //        }
        //
        //        if (system != null) {
        //            system.removeGlyph(glyph);
        //        }
        //
        //        nest.removeGlyph(glyph);
    }
}
