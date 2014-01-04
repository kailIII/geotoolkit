/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2001-2012, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotoolkit.referencing.crs;

import java.util.Map;
import java.util.Collections;
import net.jcip.annotations.Immutable;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.datum.VerticalDatum;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.referencing.cs.DefaultVerticalCS;
import org.apache.sis.referencing.AbstractReferenceSystem;
import org.geotoolkit.referencing.datum.DefaultVerticalDatum;
import javax.xml.bind.annotation.XmlTransient;


/**
 * A 1D coordinate reference system used for recording heights or depths. Vertical CRSs make use
 * of the direction of gravity to define the concept of height or depth, but the relationship with
 * gravity may not be straightforward.
 * <p>
 * By implication, ellipsoidal heights (<var>h</var>) cannot be captured in a vertical coordinate
 * reference system. Ellipsoidal heights cannot exist independently, but only as inseparable part
 * of a 3D coordinate tuple defined in a geographic 3D coordinate reference system. However GeoAPI
 * does not enforce this rule. This class defines a {@link #ELLIPSOIDAL_HEIGHT} constant in
 * violation with ISO 19111; this is considered okay if this constant is used merely as a step
 * toward the construction of a 3D CRS (for example in a transient state during WKT parsing),
 * or for passing arguments in methods enforcing type-safety.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link VerticalCS Vertical}
 * </TD></TR></TABLE>
 *
 * @author Martin Desruisseaux (IRD, Geomatys)
 * @version 3.19
 *
 * @since 1.2
 * @module
 *
 * @deprecated Moved to Apache SIS.
 */
@Immutable
@Deprecated
@XmlTransient
public class DefaultVerticalCRS extends org.apache.sis.referencing.crs.DefaultVerticalCRS {
    /**
     * Default vertical coordinate reference system using ellipsoidal datum.
     * Ellipsoidal heights are measured along the normal to the ellipsoid
     * used in the definition of horizontal datum.
     *
     * @see DefaultVerticalDatum#ELLIPSOIDAL
     * @see DefaultVerticalCS#ELLIPSOIDAL_HEIGHT
     */
    public static final DefaultVerticalCRS ELLIPSOIDAL_HEIGHT = new DefaultVerticalCRS(
            IdentifiedObjects.getProperties(DefaultVerticalCS.ELLIPSOIDAL_HEIGHT),
            DefaultVerticalDatum.ELLIPSOIDAL, DefaultVerticalCS.ELLIPSOIDAL_HEIGHT);

    /**
     * Default vertical coordinate reference system using geoidal datum.
     *
     * @see DefaultVerticalDatum#GEOIDAL
     * @see DefaultVerticalCS#GRAVITY_RELATED_HEIGHT
     *
     * @since 2.5
     */
    public static final DefaultVerticalCRS GEOIDAL_HEIGHT = new DefaultVerticalCRS(
            IdentifiedObjects.getProperties(DefaultVerticalCS.GRAVITY_RELATED_HEIGHT),
            DefaultVerticalDatum.GEOIDAL, DefaultVerticalCS.GRAVITY_RELATED_HEIGHT);

    /**
     * Constructs a new vertical CRS with the same values than the specified one.
     * This copy constructor provides a way to convert an arbitrary implementation into a
     * Geotk one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @param crs The coordinate reference system to copy.
     *
     * @since 2.2
     */
    public DefaultVerticalCRS(final VerticalCRS crs) {
        super(crs);
    }

    /**
     * Constructs a vertical CRS with the same properties than the given datum.
     * The inherited properties include the {@linkplain #getName name} and aliases.
     *
     * @param datum The datum.
     * @param cs The coordinate system.
     *
     * @since 2.5
     */
    public DefaultVerticalCRS(final VerticalDatum datum, final VerticalCS cs) {
        this(IdentifiedObjects.getProperties(datum), datum, cs);
    }

    /**
     * Constructs a vertical CRS from a name.
     *
     * @param name The name.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultVerticalCRS(final String         name,
                              final VerticalDatum datum,
                              final VerticalCS       cs)
    {
        this(Collections.singletonMap(NAME_KEY, name), datum, cs);
    }

    /**
     * Constructs a vertical CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultVerticalCRS(final Map<String,?> properties,
                              final VerticalDatum datum,
                              final VerticalCS    cs)
    {
        super(properties, datum, cs);
    }
}
