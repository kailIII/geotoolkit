/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2012, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotoolkit.referencing.operation.provider;

import net.jcip.annotations.Immutable;

import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.ReferenceIdentifier;

import org.geotoolkit.referencing.NamedIdentifier;
import org.geotoolkit.referencing.operation.projection.Mercator;
import org.geotoolkit.internal.referencing.DeprecatedName;
import org.geotoolkit.internal.referencing.Identifiers;
import org.geotoolkit.metadata.iso.citation.Citations;


/**
 * The provider for "<cite>Mercator (2SP)</cite>" projection (EPSG:9805).
 * The programmatic names and parameters are enumerated at
 * <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_2sp.html">Mercator 2SP on
 * RemoteSensing.org</A>. The math transform implementations instantiated by this provider may
 * be any of the following classes:
 * <p>
 * <ul>
 *   <li>{@link org.geotoolkit.referencing.operation.projection.Mercator}</li>
 * </ul>
 *
 * @author Martin Desruisseaux (IRD, Geomatys)
 * @author Rueben Schulz (UBC)
 * @version 3.20
 *
 * @since 2.2
 * @module
 */
@Immutable
public class Mercator2SP extends MapProjection {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 6356028352681135786L;

    /**
     * The operation parameter descriptor for the {@linkplain
     * org.geotoolkit.referencing.operation.projection.UnitaryProjection.Parameters#centralMeridian
     * central meridian} parameter value.
     *
     * This parameter is <a href="package-summary.html#Obligation">mandatory</a>.
     * Valid values range is [-180 &hellip; 180]&deg; and default value is 0&deg;.
     *
     * @deprecated Invoke <code>{@linkplain #PARAMETERS}.{@linkplain ParameterDescriptorGroup#descriptor(String)
     * descriptor(String)}</code> instead.
     */
    @Deprecated
    public static final ParameterDescriptor<Double> CENTRAL_MERIDIAN;

    /**
     * The operation parameter descriptor for the {@linkplain
     * org.geotoolkit.referencing.operation.projection.UnitaryProjection.Parameters#latitudeOfOrigin
     * latitude of origin} parameter value.
     *
     * This parameter is <a href="package-summary.html#Obligation">mandatory</a>.
     * Valid values range is [-90 &hellip; 90]&deg; and default value is 0&deg;.
     *
     * @deprecated Invoke <code>{@linkplain #PARAMETERS}.{@linkplain ParameterDescriptorGroup#descriptor(String)
     * descriptor(String)}</code> instead.
     */
    @Deprecated
    public static final ParameterDescriptor<Double> LATITUDE_OF_ORIGIN;

    /**
     * The operation parameter descriptor for the standard parallel parameter value.
     *
     * This parameter is <a href="package-summary.html#Obligation">mandatory</a>.
     * Valid values range is [-90 &hellip; 90]&deg; and default value is 0&deg;.
     *
     * @deprecated Invoke <code>{@linkplain #PARAMETERS}.{@linkplain ParameterDescriptorGroup#descriptor(String)
     * descriptor(String)}</code> instead.
     */
    @Deprecated
    public static final ParameterDescriptor<Double> STANDARD_PARALLEL;

    /**
     * The operation parameter descriptor for the {@linkplain
     * org.geotoolkit.referencing.operation.projection.UnitaryProjection.Parameters#falseEasting
     * false easting} parameter value.
     *
     * This parameter is <a href="package-summary.html#Obligation">mandatory</a>.
     * Valid values range is unrestricted and default value is 0 metre.
     *
     * @deprecated Invoke <code>{@linkplain #PARAMETERS}.{@linkplain ParameterDescriptorGroup#descriptor(String)
     * descriptor(String)}</code> instead.
     */
    @Deprecated
    public static final ParameterDescriptor<Double> FALSE_EASTING;

    /**
     * The operation parameter descriptor for the {@linkplain
     * org.geotoolkit.referencing.operation.projection.UnitaryProjection.Parameters#falseNorthing
     * false northing} parameter value.
     *
     * This parameter is <a href="package-summary.html#Obligation">mandatory</a>.
     * Valid values range is unrestricted and default value is 0 metre.
     *
     * @deprecated Invoke <code>{@linkplain #PARAMETERS}.{@linkplain ParameterDescriptorGroup#descriptor(String)
     * descriptor(String)}</code> instead.
     */
    @Deprecated
    public static final ParameterDescriptor<Double> FALSE_NORTHING;

    /**
     * Parameters creation, which must be done before to initialize the {@link #PARAMETERS} field.
     */
    static {
        CENTRAL_MERIDIAN = Identifiers.CENTRAL_MERIDIAN.select(null,
                "Longitude of natural origin",    // EPSG
                "central_meridian",               // OGC
                "Central_Meridian",               // ESRI
                "longitude_of_projection_origin", // NetCDF
                "NatOriginLong");                 // GeoTIFF
        LATITUDE_OF_ORIGIN = Identifiers.LATITUDE_OF_ORIGIN.select(null,
                "Latitude of natural origin",     // EPSG
                "latitude_of_origin",             // OGC
                "Latitude_Of_Origin",             // ESRI
                "NatOriginLat");                  // GeoTIFF
        STANDARD_PARALLEL = Identifiers.STANDARD_PARALLEL_1.select(true, null, null, null,
                "Latitude of 1st standard parallel", // EPSG
                "standard_parallel_1",               // OGC
                "Standard_Parallel_1",               // ESRI
                "standard_parallel[1]");             // NetCDF
        FALSE_EASTING = Identifiers.FALSE_EASTING.select(null,
                "False easting",    // EPSG
                "FalseEasting");    // GeoTIFF
        FALSE_NORTHING = Identifiers.FALSE_NORTHING.select(null,
                "False northing",   // EPSG
                "FalseNorthing");   // GeoTIFF
    }

    /**
     * The parameters group.
     */
    public static final ParameterDescriptorGroup PARAMETERS = Identifiers.createDescriptorGroup(
        new ReferenceIdentifier[] {
            new NamedIdentifier(Citations.OGC,    "Mercator_2SP"),
            new NamedIdentifier(Citations.EPSG,   "Mercator (variant B)"), // Starting from 7.6
            new DeprecatedName (Citations.EPSG,   "Mercator (2SP)"), // Prior to EPSG version 7.6
            new IdentifierCode (Citations.EPSG,    9805),
            new NamedIdentifier(Citations.ESRI,   "Mercator"),
            new NamedIdentifier(Citations.NETCDF, "Mercator"),
                     sameNameAs(Citations.GEOTIFF,    Mercator1SP.PARAMETERS),
                     sameNameAs(Citations.PROJ4,      Mercator1SP.PARAMETERS),
                     sameNameAs(Citations.GEOTOOLKIT, Mercator1SP.PARAMETERS)
        }, null, new ParameterDescriptor<?>[] {
            SEMI_MAJOR, SEMI_MINOR, ROLL_LONGITUDE,
            STANDARD_PARALLEL, LATITUDE_OF_ORIGIN, CENTRAL_MERIDIAN,
            FALSE_EASTING, FALSE_NORTHING
        });

    /**
     * Constructs a new provider.
     */
    public Mercator2SP() {
        super(PARAMETERS);
    }

    /**
     * Returns the operation type for this map projection.
     */
    @Override
    public Class<CylindricalProjection> getOperationType() {
        return CylindricalProjection.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MathTransform2D createMathTransform(ParameterValueGroup values) {
        return Mercator.create(getParameters(), values);
    }
}
