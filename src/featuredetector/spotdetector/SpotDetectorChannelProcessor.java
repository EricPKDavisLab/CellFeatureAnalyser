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
package featuredetector.spotdetector;

import abstractprocessors.ChannelProcessor;
import featuredetector.AbstractFeatureDetector;
import featureobjects.Feature;
import featureobjects.FeatureOps;
import featureobjects.ParentFeatureOps;
import featureobjects.ParentFeature;
import gui.GUI;
import gui.TextFieldGetTextOps;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import numutil.PrimNumOps;
import rapidsimpleguibuilder.FastJComponents;
import rapidsimpleguibuilder.RapidBuilderJPanel2Column;

/**
 *
 * A class which is used to set up the spot detection within the {@link ParentFeature}s.
 *
 * @author mqbssep5
 */
public class SpotDetectorChannelProcessor extends ChannelProcessor implements ItemListener {

    /**
     * Constructor
     *
     * @param channel
     */
    public SpotDetectorChannelProcessor( int channel ) {
        this.channelID = channel;
    }

    private GUI gc;
    
    private FastJComponents fc;

    private JTextField jtfSpotDiam, jtfThresh, jtfMinArea, jtfMinCirc, jtfMinMeanInt;

    private JComboBox jcbColor, jcbDetectorOption;
    
    private JRadioButton jrbSegOptionCC, jrbSegOptionIWWS;

    private double spotdiameter = 5.0, thresh = 4.0;

    private final double spotdiameterDefault = 5.0, threshDefault = 4.0;

    private AbstractFeatureDetector detector;

    private ImagePlus imp;

    private ArrayList<ParentFeature> parentFeatures;

    private int nfeatures, channelID;
    
    public double minVolume = 0.0, minCirc = 0.0, minMeanIntesnity = 0.0;    
    
    public double minVolumeDefault = 0.0, minCircDefault = 0.0, minMeanIntesnityDefault = 0.0;        

    private Color[] cols = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.PINK, Color.ORANGE};

    private String[] colorNames = new String[]{"Red", "Green", "Blue", "Cyan", "Pink", "Orange"};

    private Color drawColor;
   
    private int chosenSegmentationMethod;
    
    private int panWidth;
    
    private int panHeight;

    /**
     * The key ID assigned to the {@link Feature}s detected using this {@link ChannelProcessor}
     * and stored in the {@link ParentFeature}s Feature map. 
     */
    public static final String SPOT_FEATURE_NAME = "SPOTS";

    /**
     * The Key IDs assigned to the numerical properties stored in to each of the 
     * {@link Feature}s numerical properties map, at the point of detection,
     * e.g. Position, intensity etc. 
     */
    public static final String[] SPOT_FEATURE_NUMERIC_PROPERTY_NAMES = new String[]{SpotDetectorCommon.SPOT_ID, SpotDetectorCommon.COM_X, SpotDetectorCommon.COM_Y, SpotDetectorCommon.COM_X_PIX, SpotDetectorCommon.COM_Y_PIX, SpotDetectorCommon.SPOT_AMPLITUDE, SpotDetectorCommon.SPOT_MEAN_VALUE, SpotDetectorCommon.SPOT_AREA, SpotDetectorCommon.SPOT_BG_USED, SpotDetectorCommon.SPOT_SUM_INTENSITY, SpotDetectorCommon.SPOT_PERIMETER, SpotDetectorCommon.SPOT_CIRCULARITY };

    private static final String[] COMBO_DETECTOR_NAMES = new String[]{"LoG detector", "Threshold only"};

    private JPanel cards;

    private int selected_detector_index = 0;

    @Override
    public JPanel getSettingsPanel() {

        gc = new GUI();
        
        panWidth = PrimNumOps.scaleInt(GUI.GUI_WIDTH,0.9);
        panHeight = PrimNumOps.scaleInt(GUI.GUI_HEIGHT, 0.6);        
        
        fc = new FastJComponents(GUI.TEXT_SIZE_STANDARD, null, this);
        
        JPanel comboPane = new JPanel();

        jcbDetectorOption = fc.jComboBox(COMBO_DETECTOR_NAMES);//, hsiz
        //jcbDetectorOption.addItemListener(this);
        comboPane.add(jcbDetectorOption);

        // create the Panel for the LoG detector. 
        JPanel card1 = createLoGPanel();
        JPanel card2 = createThresholdDetectorPanel();
        // 
        cards = new JPanel(new CardLayout());
        cards.add(card1, COMBO_DETECTOR_NAMES[0]);
        cards.add(card2, COMBO_DETECTOR_NAMES[1]);

        
        JPanel commonBits = createCommonPanel();
        JPanel combined = new JPanel();

        RapidBuilderJPanel2Column builder = new RapidBuilderJPanel2Column( combined, panWidth, panHeight, GUI.SMALL_COMPONENT_HEIGHT, 0, 0 );
        builder.addComponentDoubleColumnCustomHeight(comboPane);
        builder.addComponentDoubleColumnCustomHeight(cards);
        builder.addComponentDoubleColumnCustomHeight(commonBits);
        builder.packComponents();
        
        return combined;
    }

    private JPanel createThresholdDetectorPanel() {
//        JPanelSpeedy2ColBased pan = new JPanelSpeedy2ColBased();
//        // add components to the panel.
//        pan.addTwoComponentsToRow(gc.blankSpace(hsiz), gc.blankSpace(hsiz));
//        pan.addComponentDoubleColumn(gc.jLabelBold("Threshold only detector", hsiz));
        JPanel pan = new JPanel();
        RapidBuilderJPanel2Column builder = new RapidBuilderJPanel2Column(pan , panWidth, panHeight/5, GUI.SMALL_COMPONENT_HEIGHT,GUI.GUI_INSET_X,GUI.GUI_INSET_Y);        
        // add components to the panel.
        builder.addTwoComponentsToRow(fc.blankSpace(), fc.blankSpace());
        //builder.addComponentDoubleColumnStandardHeight(fc.jLabelBold("Threshold only detector ")); //  
        //builder.packComponents();
        return pan;
    }

    /**
     * Create the panel for the LoG detector.
     *
     * @return the panel for the LoG detector.
     */
    private JPanel createLoGPanel() {

        JPanel logpanel = new JPanel();

        // text fields 
        jtfSpotDiam = fc.jTextFeild(Double.toString(spotdiameter));

        RapidBuilderJPanel2Column builder = new RapidBuilderJPanel2Column(logpanel, panWidth, panHeight/5, GUI.SMALL_COMPONENT_HEIGHT,GUI.GUI_INSET_X,GUI.GUI_INSET_Y);
        
        // add components to the panel.
        //builder.addTwoComponentsToRow(fc.jLabelBold("LoG detection"),fc.blankSpace());//
        //builder.addComponentDoubleColumnStandardHeight(fc.jLabelBold("LoG detection"));
        //builder.addTwoComponentsToRow(fc.blankSpace(), fc.blankSpace());
        builder.addTwoComponentsToRow(fc.jLabel("Spot diameter (pix)"), jtfSpotDiam); //fc.jLabel("Spot diameter (pix)")
       
        //builder.packComponents();
        return logpanel;
    }

    private JPanel createCommonPanel() {

        JPanel pan = new JPanel();
        //
        jcbColor = fc.jComboBox(colorNames);
        jcbColor.setSelectedIndex(channelID);
        jtfThresh = fc.jTextFeild("" + thresh);
        jtfMinArea = fc.jTextFeild("" + minVolume);
        jtfMinCirc = fc.jTextFeild("" + minCirc );
        jtfMinMeanInt = fc.jTextFeild("" + minMeanIntesnity );

        // segmentation option 
        jrbSegOptionIWWS = fc.jRadioButton("Int. based WS");        
        jrbSegOptionCC = fc.jRadioButton("Conn. compnts.");
        jrbSegOptionIWWS.setSelected(true);
        //   
        ButtonGroup bg = new ButtonGroup();
        bg.add(jrbSegOptionIWWS);
        bg.add(jrbSegOptionCC);
        
        // Inialise the GUI builder. 
        RapidBuilderJPanel2Column builder = new RapidBuilderJPanel2Column( pan,  panWidth, panHeight, GUI.SMALL_COMPONENT_HEIGHT,GUI.GUI_INSET_X,GUI.GUI_INSET_Y);
        
        builder.addTwoComponentsToRow(fc.jLabel("Threshold"), jtfThresh);
        builder.addHorizontalJSeparator();
//        pan.addComponentDoubleColumn(jrbSegOptionIWWS);
//        pan.addComponentDoubleColumn(jrbSegOptionCC); 
        builder.addComponentDoubleColumnStandardHeight(fc.jLabel("Segmentation method"));
        builder.addTwoComponentsToRow(jrbSegOptionIWWS, jrbSegOptionCC);
        //pan.addComponentDoubleColumn(gc.jLabel("Filter options", GUIcomponents.FULL_WIDTH_DIM));        
        builder.addTwoComponentsToRow(fc.jLabel("Min area/volume"), jtfMinArea);
        builder.addTwoComponentsToRow(fc.jLabel("Min circularity"), jtfMinCirc);
        builder.addTwoComponentsToRow(fc.jLabel("Min mean int."), jtfMinMeanInt);
        //builder.addHorizontalJSeparator();
        builder.addTwoComponentsToRow(fc.jLabel("Draw color"), jcbColor);//
        builder.packComponents();

        return pan;
    }

    /**
     * Setter for the image data and parent features. 
     * @param imp
     * @param features
     * @param channelID 
     */
    public void setData(ImagePlus imp, ArrayList<ParentFeature> features, int channelID) {
        this.imp = imp;
        this.parentFeatures = features;
        this.channelID = channelID;
    }

    @Override
    public void run() {

        // Return the values provided in the text box. 
        getValues();
        // 
        nfeatures = parentFeatures.size();

        ParentFeature currentFeature;
        ImageProcessor ip;
        double pixelscale = imp.getCalibration().pixelWidth;
        ArrayList<Feature> spots;
        int connectivity = 8;
        int rad = (int) Math.max(Math.ceil((double)spotdiameter / 2d), 1);
        for (int i = 0; i < nfeatures; i++) {
            // feature to process
            currentFeature = parentFeatures.get(i);
            // get a crop of the image data around our feature. 
            ip = ParentFeatureOps.getLocalisedFeatureImageData(currentFeature, imp).getProcessor().duplicate();

            if (selected_detector_index == 0) {
                detector = new LoGSpotDetector2D(currentFeature, ip, spotdiameter / 2d, thresh, chosenSegmentationMethod, rad, connectivity, pixelscale);
            } else {
                detector = new NoFilterDetector2D(currentFeature, ip, thresh, chosenSegmentationMethod, connectivity, pixelscale);
            }
            detector.run();

            // get the detected features. 
            spots = detector.getFeatures();

            // filter the set based on area/volume
            if ( minVolume > 0.0 ) {
                FeatureOps.removeFeaturesBasedOnNumericalPropertyValue( spots, SpotDetectorCommon.SPOT_AREA, minVolume, FeatureOps.KEEP_GREATER_THAN );
            }
            if( minCirc > 0.0 ){
                FeatureOps.removeFeaturesBasedOnNumericalPropertyValue( spots, SpotDetectorCommon.SPOT_CIRCULARITY, minCirc, FeatureOps.KEEP_GREATER_THAN );
            }
            if( minMeanIntesnity > 0.0 ){
                FeatureOps.removeFeaturesBasedOnNumericalPropertyValue( spots, SpotDetectorCommon.SPOT_MEAN_VALUE, minMeanIntesnity, FeatureOps.KEEP_GREATER_THAN );
            }            
            
            // set the draw color 
            FeatureOps.setAllFeaturesDrawColorSameColor(spots, drawColor);
            currentFeature.addFeatures(channelID, SPOT_FEATURE_NAME, spots);
        }

    }

    /**
     * Get the values from the text box.
     */
    private void getValues() {

        // get the selcted detector
        selected_detector_index = jcbDetectorOption.getSelectedIndex();

        // text box values. 
        spotdiameter = TextFieldGetTextOps.positiveDoubleOrDefault(jtfSpotDiam, spotdiameterDefault);
        thresh = TextFieldGetTextOps.positiveDoubleOrDefault(jtfThresh, threshDefault);
        minVolume = TextFieldGetTextOps.positiveDoubleOrDefault(jtfMinArea, minVolumeDefault);
        minCirc = TextFieldGetTextOps.positiveDoubleOrDefault(jtfMinCirc, minCircDefault);
        minMeanIntesnity = TextFieldGetTextOps.positiveDoubleOrDefault(jtfMinMeanInt, minMeanIntesnityDefault);
        
        //System.out.println("spotdiameter " + spotdiameter + " thresh " + thresh + " minVolume " + minVolume + " minCirc " + minCirc + " minMeanIntesnity " + minMeanIntesnity );
        
        // color 
        drawColor = cols[jcbColor.getSelectedIndex()];
        
        if(jrbSegOptionIWWS.isSelected()){
            chosenSegmentationMethod = SpotDetectorCommon.INTENSITY_BASED_WATERSHED_SEGMENTATION;
        }else{
            chosenSegmentationMethod = SpotDetectorCommon.CONNECTED_COMPONENT_SEGMENTATION;
        }

    }

    @Override
    public String name() {
        return "LoG";
    }

    @Override
    public void itemStateChanged(ItemEvent evt) {
        if (evt.getSource() == jcbDetectorOption) {
            // switch the cards. 
            CardLayout cl = (CardLayout) (cards.getLayout());
            cl.show(cards, (String) evt.getItem());
        }
    }

}
