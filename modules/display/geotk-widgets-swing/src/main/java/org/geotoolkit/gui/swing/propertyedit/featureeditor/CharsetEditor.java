/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Johann Sorel
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
package org.geotoolkit.gui.swing.propertyedit.featureeditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.opengis.feature.type.PropertyType;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 */
public class CharsetEditor extends PropertyValueEditor implements ActionListener{

    private final JComboBox component = new JComboBox();

    public CharsetEditor() {
        super(new BorderLayout());
        add(BorderLayout.NORTH, component);

        final List<Charset> sets = new ArrayList<Charset>(Charset.availableCharsets().values());
        component.setModel(new ListComboBoxModel(sets));
        component.addActionListener(this);
    }

    @Override
    public boolean canHandle(PropertyType candidate) {
        return Charset.class.equals(candidate.getBinding());
    }

    @Override
    public void setValue(PropertyType type, Object value) {
        if (value instanceof Charset) {
            component.setSelectedItem(value);
        }else{
            component.setSelectedItem(null);
        }
    }

    @Override
    public Object getValue() {
        return component.getSelectedItem();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        firePropertyChange(PROP_VALUE, null, getValue());
    }

}
