/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009-2012, Open Source Geospatial Foundation (OSGeo)
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
package com.sun.xml.internal.bind;

import java.util.concurrent.Callable;
import javax.xml.bind.ValidationEventHandler;
import org.xml.sax.SAXException;


/**
 * A placeholder for an internal class from Sun JDK 6. Sun's internal package are not visible at
 * compile time, while they are visible at runtime. This placeholder is used only in order to
 * allows some classes to extend the Sun's class at compile-time. It will not be used at run-time;
 * the "real" Sun's class will be used instead since it come first in the classpath.
 *
 * @author Guilhem Legal (Geomatys)
 * @version 3.00
 *
 * @since 3.00
 */
public abstract class IDResolver {
    public IDResolver() {
    }

    public void startDocument(ValidationEventHandler e)  throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public abstract void bind(String s, Object o) throws SAXException;

    public abstract Callable resolve(String s, Class c) throws SAXException;
}
