//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                       G l y p h T a s k                                        //
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
package org.audiveris.omr.script;

import org.audiveris.omr.glyph.Glyph;
import org.audiveris.omr.glyph.Glyphs;
import org.audiveris.omr.sheet.Sheet;
import org.audiveris.omr.sheet.SystemInfo;
import org.audiveris.omr.sheet.SystemManager;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Class {@code GlyphTask} handles a collection of glyphs.
 *
 * <p>
 * These glyphs may already exist (as in a plain {@link GlyphUpdateTask})
 * or remain to be created (as in {@link InsertTask})</p>
 *
 * <h4>Glyphs and sections in a script:<br/>
 * <img src="doc-files/script.png"/>
 * </h4>
 *
 * @author Hervé Bitteur
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class GlyphTask
        extends SheetTask
{
    //~ Instance fields ----------------------------------------------------------------------------

    /** The collection of glyphs which are concerned by this task */
    protected Set<Glyph> glyphs;

    /** The set of (pre) impacted systems, using status before action */
    protected SortedSet<SystemInfo> initialSystems;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new GlyphTask object.
     *
     * @param sheet  the sheet impacted
     * @param glyphs the collection of glyphs concerned by this task
     */
    protected GlyphTask (Sheet sheet,
                         Collection<Glyph> glyphs)
    {
        super(sheet);

        // Check parameters
        if ((glyphs == null) || glyphs.isEmpty()) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + " needs at least one glyph");
        }

        this.glyphs = new LinkedHashSet<Glyph>(glyphs);
    }

    /**
     * Creates a new GlyphTask object
     *
     * @param sheet the sheet impacted
     */
    protected GlyphTask (Sheet sheet)
    {
        super(sheet);
    }

    /** No-arg constructor for JAXB only. */
    protected GlyphTask ()
    {
    }

    //~ Methods ------------------------------------------------------------------------------------
    //--------//
    // epilog //
    //--------//
    @Override
    public void epilog (Sheet sheet)
    {
        ///Stepping.reprocessSheet(Step.SYMBOLS, sheet, getImpactedSystems(sheet), false);
    }

    //--------------------//
    // getImpactedSystems //
    //--------------------//
    /**
     * Report the set of systems that are impacted by the action
     *
     * @param sheet the containing sheet
     * @return the ordered set of impacted systems
     */
    public SortedSet<SystemInfo> getImpactedSystems (Sheet sheet)
    {
        SortedSet<SystemInfo> impactedSystems = new TreeSet<SystemInfo>();
        impactedSystems.addAll(initialSystems);
        impactedSystems.addAll(retrieveCurrentImpact(sheet));

        return impactedSystems;
    }

    //------------------//
    // getInitialGlyphs //
    //------------------//
    /**
     * Report the collection of initial glyphs
     *
     * @return the impactedGlyphs
     */
    public Set<Glyph> getInitialGlyphs ()
    {
        return glyphs;
    }

    //------------------//
    // impactAllSystems //
    //------------------//
    /**
     * Set the impacted systems as all systems
     */
    public void impactAllSystems ()
    {
        initialSystems.addAll(sheet.getSystems());
    }

    //--------//
    // prolog //
    //--------//
    @Override
    public void prolog (Sheet sheet)
    {
        super.prolog(sheet);
        this.sheet = sheet;

        // Make sure the concrete sections and glyphs are available
        if (glyphs == null) {
            retrieveGlyphs();
        }

        initialSystems = retrieveCurrentImpact(sheet);
    }

    //-----------//
    // internals //
    //-----------//
    @Override
    protected String internals ()
    {
        StringBuilder sb = new StringBuilder(super.internals());

        if (glyphs != null) {
            sb.append(" ").append(Glyphs.ids(glyphs));
        } else {
            sb.append(" no-glyphs");
        }

        return sb.toString();
    }

    //-----------//
    // remaining //
    //-----------//
    /**
     * Report the collection of systems that follow the provided one
     *
     * @param system the one after which we pick any system
     * @return the remaining portion of the sequence of systems in the sheet
     */
    protected Collection<SystemInfo> remaining (SystemInfo system)
    {
        List<SystemInfo> all = sheet.getSystems();

        return all.subList(all.indexOf(system) + 1, all.size());
    }

    //-----------------------//
    // retrieveCurrentImpact //
    //-----------------------//
    /**
     * Report the set of systems that are impacted by the action, as determined
     * by the *current status* of the glyphs *currently* pointed by the sections
     *
     * @param sheet the containing sheet
     * @return the ordered set of impacted systems
     */
    protected SortedSet<SystemInfo> retrieveCurrentImpact (Sheet sheet)
    {
        SortedSet<SystemInfo> impactedSystems = new TreeSet<SystemInfo>();
        SystemManager systemManager = sheet.getSystemManager();

        for (Glyph glyph : glyphs) {
            if (glyph != null) {
                for (SystemInfo system : systemManager.getSystemsOf(glyph)) {
                    impactedSystems.add(system);

                    //                    Shape shape = glyph.getShape();
                    //
                    //                    if ((shape != null) && shape.isPersistent()) {
                    // Include all following systems
                    impactedSystems.addAll(remaining(system));

                    //                    }
                }
            }
        }

        return impactedSystems;
    }

    //----------------//
    // retrieveGlyphs //
    //----------------//
    /**
     * This method is in charge of retrieving the glyphs to be handled, using
     * either their composing sections ids or their shape and locations.
     */
    protected abstract void retrieveGlyphs ();
}
