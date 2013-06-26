/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 3 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.gui.swing.chooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.gui.swing.chooser.JServerChooser.FactoryCellRenderer;
import org.geotoolkit.gui.swing.misc.JOptionDialog;
import org.geotoolkit.gui.swing.propertyedit.JFeatureOutLine;
import org.geotoolkit.gui.swing.propertyedit.featureeditor.PropertyValueEditor;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.map.MapLayer;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.util.logging.Logging;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Panel allowing to choose a data store and configure it among the
 * declared DataStoreFactories.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class JFeatureStoreChooser extends javax.swing.JPanel {

    private static final Logger LOGGER = Logging.getLogger(JFeatureStoreChooser.class);

    private static final Comparator<FeatureStoreFactory> SORTER = new Comparator<FeatureStoreFactory>() {
        @Override
        public int compare(FeatureStoreFactory o1, FeatureStoreFactory o2) {
            return o1.getDisplayName().toString().compareTo(o2.getDisplayName().toString());
        }
    };

    private final JFeatureOutLine guiEditor = new JFeatureOutLine();
    private final JLayerChooser chooser = new JLayerChooser();

    public JFeatureStoreChooser() {
        initComponents();
        guiEditPane.add(BorderLayout.CENTER,new JScrollPane(guiEditor));

        final List<FeatureStoreFactory> factories = new ArrayList<FeatureStoreFactory>(FeatureStoreFinder.getAvailableFactories(null));
        Collections.sort(factories, SORTER);

        guiList.setHighlighters(HighlighterFactory.createAlternateStriping() );
        guiList.setModel(new ListComboBoxModel(factories));
        guiList.setCellRenderer(new FactoryCellRenderer());
        guiList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                final FeatureStoreFactory factory = (FeatureStoreFactory) guiList.getSelectedValue();
                final ParameterValueGroup param = factory.getParametersDescriptor().createValue();
                guiEditor.setEdited(param);
            }
        });
        setLayerSelectionVisible(false);
    }

    public void setLayerSelectionVisible(boolean visible){
        if(visible){
            guiSplit.setRightComponent(guiLayerSplit);
            guiLayerSplit.setLeftComponent(guiConfig);
            guiLayerSplit.setRightComponent(chooser);
            guiLayerSplit.setDividerLocation(260);
        }else{
            guiSplit.setRightComponent(guiConfig);
        }
    }

    public FeatureStore getFeatureStore() throws DataStoreException{
        final FeatureStoreFactory factory = (FeatureStoreFactory) guiList.getSelectedValue();

        if(factory == null){
            return null;
        }

        final ParameterValueGroup param = guiEditor.getEditedAsParameter(factory.getParametersDescriptor());
        if(guiCreateNew.isSelected()){
            return factory.create(param);
        }else{
            return factory.open(param);
        }
    }

    public List<MapLayer> getSelectedLayers() throws DataStoreException{
        return chooser.getLayers();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiLayerSplit = new javax.swing.JSplitPane();
        guiSplit = new javax.swing.JSplitPane();
        guiConfig = new javax.swing.JPanel();
        guiEditPane = new javax.swing.JPanel();
        guiCreateNew = new javax.swing.JCheckBox();
        guiConnect = new javax.swing.JButton();
        guiInfoLabel = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        guiList = new org.jdesktop.swingx.JXList();

        guiLayerSplit.setDividerSize(5);

        guiSplit.setDividerLocation(240);
        guiSplit.setDividerSize(5);

        guiEditPane.setLayout(new java.awt.BorderLayout());

        guiCreateNew.setText(MessageBundle.getString("chooserfeaturestore.new")); // NOI18N

        guiConnect.setText(MessageBundle.getString("chooserfeaturestore.connect")); // NOI18N
        guiConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiConnectActionPerformed(evt);
            }
        });

        guiInfoLabel.setEditable(false);

        javax.swing.GroupLayout guiConfigLayout = new javax.swing.GroupLayout(guiConfig);
        guiConfig.setLayout(guiConfigLayout);
        guiConfigLayout.setHorizontalGroup(
            guiConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(guiConfigLayout.createSequentialGroup()
                .addComponent(guiCreateNew)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiConnect))
            .addComponent(guiEditPane, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
        );
        guiConfigLayout.setVerticalGroup(
            guiConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, guiConfigLayout.createSequentialGroup()
                .addComponent(guiEditPane, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(guiConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guiCreateNew)
                    .addComponent(guiConnect)
                    .addComponent(guiInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        guiSplit.setRightComponent(guiConfig);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setViewportView(guiList);

        guiSplit.setLeftComponent(jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(guiSplit, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(guiSplit)
        );
    }// </editor-fold>//GEN-END:initComponents

private void guiConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiConnectActionPerformed

        FeatureStore store = null;
        try {
            chooser.setSource(null);
            store = getFeatureStore();
            chooser.setSource(store);
            guiInfoLabel.setForeground(Color.GREEN);
            guiInfoLabel.setText(MessageBundle.getString("chooserfeaturestore.ok"));
        } catch (DataStoreException ex) {
            guiInfoLabel.setForeground(Color.RED);
            guiInfoLabel.setText(""+ex.getMessage());
            LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }

}//GEN-LAST:event_guiConnectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel guiConfig;
    private javax.swing.JButton guiConnect;
    private javax.swing.JCheckBox guiCreateNew;
    private javax.swing.JPanel guiEditPane;
    private javax.swing.JTextField guiInfoLabel;
    private javax.swing.JSplitPane guiLayerSplit;
    private org.jdesktop.swingx.JXList guiList;
    private javax.swing.JSplitPane guiSplit;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables


    /**
     * Display a modal dialog.
     *
     * @return
     * @throws DataStoreException
     */
    public static List<FeatureStore> showDialog() throws DataStoreException{
        return showDialog(Collections.EMPTY_LIST);
    }

    /**
     * Display a modal dialog.
     *
     * @param editors : additional FeatureOutline editors
     * @return
     * @throws DataStoreException
     */
    public static List<FeatureStore> showDialog(List<PropertyValueEditor> editors) throws DataStoreException{
        return showDialog(editors, false);
    }

    /**
     * Display a modal dialog choosing layers.
     *
     * @param editors : additional FeatureOutline editors
     * @return
     * @throws DataStoreException
     */
    public static List<MapLayer> showLayerDialog(List<PropertyValueEditor> editors) throws DataStoreException{
        return showDialog(editors, true);
    }

    private static List showDialog(List<PropertyValueEditor> editors, boolean layerVisible) throws DataStoreException{
        final JFeatureStoreChooser chooser = new JFeatureStoreChooser();
        if(editors != null){
            chooser.guiEditor.getEditors().addAll(editors);
        }        
        chooser.setLayerSelectionVisible(layerVisible);
        
        final int res = JOptionDialog.show(null, chooser, JOptionPane.OK_OPTION);
        
        if (JOptionPane.OK_OPTION == res) {
            if(layerVisible){
                return chooser.getSelectedLayers();
            }else{
                final FeatureStore store = chooser.getFeatureStore();
                if(store == null){
                    return Collections.EMPTY_LIST;
                }else{
                    return Collections.singletonList(store);
                }
            }
        } else {
            return Collections.EMPTY_LIST;
        }
        
    }

}
