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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.opengis.feature.type.PropertyType;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 */
public class URLEditor extends PropertyValueEditor implements ActionListener{

    private final JTextField component = new JTextField();
    private final JButton chooseButton = new JButton("...");

    public URLEditor() {
        super(new BorderLayout());
        add(BorderLayout.CENTER, component);
        add(BorderLayout.EAST, chooseButton);
        chooseButton.addActionListener(this);
        chooseButton.setMargin(new Insets(0, 0, 0, 0));
    }

    @Override
    public boolean canHandle(PropertyType candidate) {
        return URL.class.equals(candidate.getBinding());
    }

    @Override
    public void setValue(PropertyType type, Object value) {
        if (value instanceof URL) {
            try {
                component.setText( ((URL) value).toURI().toString() );
            } catch (URISyntaxException ex) {
                Logger.getLogger(URLEditor.class.getName()).log(Level.WARNING, null, ex);
            }
        }else{
            component.setText("");
        }
    }

    @Override
    public Object getValue() {
        final String str = component.getText();
            try {
                return new URL(str);
            } catch (MalformedURLException ex) {
                Logger.getLogger(URLEditor.class.getName()).log(Level.FINER, null, ex);
                return null;
            }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        final int response = chooser.showDialog(chooseButton, MessageBundle.getString("ok"));
        if(response == JFileChooser.APPROVE_OPTION){
            final File f = chooser.getSelectedFile();
            if(f!=null){
                component.setText(f.toURI().toString());
            }
        }
    }

}
