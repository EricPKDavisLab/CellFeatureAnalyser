/*
 * Copyright 2018 mqbssep5.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package chromaticcorrection;

import abstractprocessors.AbstractParentFeatureProcessor;
import gui.CellAnalyserGUIModel;
import gui.GUI;
import gui.TextFieldGetTextOps;
import ij.ImagePlus;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import rapidsimpleguibuilder.FastJComponents;
import rapidsimpleguibuilder.RapidBuilderJPanel2Column;

/**
 * Processor for applying chromatic correction to the multi-channel data.
 *
 * It currently works with 2 channel data. The chromatic (x,y) shift is applies
 * to the second channel.
 *
 * @author mqbssep5
 */
public class ChromaticCorrection2Dprocessor extends AbstractParentFeatureProcessor implements ActionListener {

    private GUI gc;
    
    private FastJComponents fc;

    private JTextField jtfShiftX, jtfShiftY;

    private JRadioButton jrbPhyUnits, jrbPix;

    private ButtonGroup buttonGroup;

    private JButton jbtPreview;

    private double shiftX = 0.00;

    private double shiftY = 0.00;

    public static String NAME = "Chromatic";

    public ChromaticCorrection2Dprocessor(CellAnalyserGUIModel model) {
        super(model);
    }

    @Override
    public JPanel getSettingsPanel() {

        gc = new GUI();
        
        fc = new FastJComponents(GUI.TEXT_SIZE_STANDARD, this, null);

        JPanel panel = new JPanel();

        Dimension siz = GUI.FULL_WIDTH_DIM;

        jtfShiftX = gc.jTextFeild("" + shiftX, siz);
        jtfShiftY = gc.jTextFeild("" + shiftY, siz);

        // shift units. 
        jrbPix = gc.jRadioButton("Pix", null, siz);
        jrbPhyUnits = gc.jRadioButton("Distance", null, siz);
        jrbPix.setSelected(true);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(jrbPix);
        buttonGroup.add(jrbPhyUnits);

        // preview button 
        jbtPreview = gc.jButton("Preview", this, siz);

        RapidBuilderJPanel2Column builder = new RapidBuilderJPanel2Column(panel, (int)0.9*GUI.GUI_WIDTH, GUI.GUI_HEIGHT, GUI.SMALL_COMPONENT_HEIGHT, GUI.GUI_INSET_X, GUI.GUI_INSET_Y);
        // Add the components to the panel. 
        builder.addComponentDoubleColumnStandardHeight(fc.jLabelBold("Chromatic correction"));
        builder.addComponentDoubleColumnStandardHeight(fc.jLabel("Shift XY (in same units as pixels)"));
        //panel.addTwoComponentsToRow(jrbPhyUnits, jrbPix);
        builder.addTwoComponentsToRow(fc.jLabel("dx"), jtfShiftX);
        builder.addTwoComponentsToRow(fc.jLabel("dy"), jtfShiftY);
        builder.addSingleComponentRHS(jbtPreview);
        builder.packComponents();

        return panel;

    }

    @Override
    public void doProcess() {

        getSettings();

        // shift the image current image 
        ShiftImageSlices shifter = new ShiftImageSlices();
        shifter.process(model.getCurrentImageDataSet(), new double[]{shiftX}, new double[]{shiftY}, true);

    }

    private void doPreview() {

        getSettings();

        // duplicate the current image, apply the shift, and display the image. 
        ImagePlus impCurrentDup = model.getCurrentImageDataSet().duplicate();
        impCurrentDup.setTitle("Preview shifted " + impCurrentDup.getTitle());
        impCurrentDup.show();

        //System.out.println(" pix size " + impCurrentDup.getCalibration().pixelWidth);

        ShiftImageSlices shifter = new ShiftImageSlices();
        shifter.process(impCurrentDup, new double[]{shiftX}, new double[]{shiftY}, true);

    }

    private void getSettings() {
        shiftX = TextFieldGetTextOps.doubleOrDefault(jtfShiftX, shiftX);
        shiftY = TextFieldGetTextOps.doubleOrDefault(jtfShiftY, shiftY);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void summariseAndSave() {

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == jbtPreview) {
            doPreview();
        }
    }

}
