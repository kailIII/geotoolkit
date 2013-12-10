/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2013, Geomatys
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
package org.geotoolkit.display3d.scene.loader;

import org.apache.sis.storage.DataStoreException;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import javax.measure.converter.ConversionException;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;

/**
 * Generate tile elevation for terrain.
 *
 * @author Thomas Rouby (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public interface ElevationLoader {

    double getMinimumElevation();

    double getMaximumElevation();

    void setOutputCRS(CoordinateReferenceSystem outputCrs) throws FactoryException, ConversionException;

    BufferedImage getBufferedImageOf(Envelope outputEnv, Dimension outputDimension) throws TransformException, FactoryException, ConversionException;

    double getSmoothValueOf(DirectPosition position, double scale) throws FactoryException, ConversionException, TransformException;

    double getValueOf(DirectPosition position, double scale) throws DataStoreException, TransformException, IOException, ConversionException, FactoryException;

}
