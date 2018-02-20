/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagedomanmeasurement;

import abstractprocessors.AbstractParentFeatureProcessor;
import colocalisation.FeatureColocaliszer2D;
import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import gui.GUI;
import gui.JPanelSpeedy2ColBasedDefinedSize;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import io.FeaturesAndParentFeaturesToResultsTable;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import roiutil.RoiUtil;

/**
 * A class which just takes measurements of the intensity etc. inside the ROI 
 * of the outline of the parent feature.
 * 
 * @author mqbssep5
 */
public class ParentFeatureImageDomainIntensityMeasureProcessor extends AbstractParentFeatureProcessor{

    public JPanel panel; 
    
    public static final String NAME = "Int. Mes";
    
    public static String CELL_MEAN = "_CELL_MEAN";
    
    public static String CELL_MED = "_CELL_MED";
    
    public static String CELL_STD = "_CELL_STD";
    
    public static String CELL_MIN = "_CELL_MIN";
    
    public static String CELL_MAX = "_CELL_MAX";  
    
    public static String CELL_AREA = "_CELL_AREA";
    
    public static String CH_INDX = "CH_";
    
    public static String CELL_PRIM = "CELL_PRIM";
    
    public static String CELL_CIRC = "CELL_CIRC";    
    
    public static String[] STAT_NAMES = new String[]{CELL_MEAN,CELL_STD,CELL_MED,CELL_MIN,CELL_MAX};
    
    
    public ParentFeatureImageDomainIntensityMeasureProcessor(CellAnalyserGUIModel model) {
        super(model);
    }
    
    @Override
    public JPanel getSettingsPanel() {
       
       JPanelSpeedy2ColBasedDefinedSize pan = new JPanelSpeedy2ColBasedDefinedSize(300, 500);
         
       GUI gc = new GUI();
       pan.addComponentDoubleColumn(gc.jLabelBold("Intensity stats computer.", GUI.FULL_WIDTH_DIM));
       pan.addTwoComponentsToRow(gc.blankSpace(GUI.HALF_WIDTH_DIM), gc.blankSpace(GUI.HALF_WIDTH_DIM));
       pan.addComponentDoubleColumn(gc.jLabel("No input required", GUI.FULL_WIDTH_DIM));
       pan.addComponentDoubleColumn(gc.jLabel("Computes the following stats:", GUI.FULL_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI mean", GUI.HALF_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI std", GUI.HALF_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI median", GUI.HALF_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI min", GUI.HALF_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI max", GUI.HALF_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI circularity", GUI.HALF_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI perimeter", GUI.HALF_WIDTH_DIM));
       pan.addSingleComponentLHS(gc.jLabel("ROI area", GUI.HALF_WIDTH_DIM));
       
       return pan;
    }

    @Override
    public void doProcess() {
        
        ImagePlus imp = model.getCurrentImageDataSet();
        
        ArrayList<ParentFeature> features;
        features = model.getCurrentImageSetParentFeatures();
        int nFeatures = features.size(); 
        
        ParentFeature pf;
        for( int i = 0; i < nFeatures; i++ ){
            //
            pf = features.get(i);
            // compute the stats. 
            getImageStats( pf, imp );
        }
        
    }

    /**
     * Extract the data and do the stats on the image data. 
     * @param pf
     * @param imp 
     */
    private void getImageStats( ParentFeature pf, ImagePlus imp ){
        
        PolygonRoi proi = pf.getFeatureBoundsOriginalImage();
        
        int nchannels = model.getNchannels();
        double[] pixels;
        ImageProcessor ip;
        
        double mean, stdev, median, min, max,pixelScale;
        String chanPrefix; 
        pixelScale = model.getCurrentImageDataSet().getCalibration().pixelHeight;
        // get the pixels for each of the channels in the image and compute the stats
        for( int ch = 0; ch < nchannels; ch++ ){
            //
            ip = imp.getStack().getProcessor(imp.getStackIndex(ch+1, 1, pf.getFrame()));
            // extract the pixels. 
            pixels = roiutil.RoiUtil.getPixelsAsDoubles(proi, ip);
            // compute the stats
            mean = StatUtils.mean(pixels);
            stdev = Math.sqrt(StatUtils.variance(pixels));
            Median m = new Median();
            median = m.evaluate(pixels);
            min = StatUtils.min(pixels);
            max = StatUtils.max(pixels);

            
            // save in the parent feature
            chanPrefix = CH_INDX + (ch+1);
            pf.addNumericProperty(chanPrefix+CELL_MEAN, mean);
            pf.addNumericProperty(chanPrefix+CELL_STD, stdev);            
            pf.addNumericProperty(chanPrefix+CELL_MED, median);
            pf.addNumericProperty(chanPrefix+CELL_MIN, min);
            pf.addNumericProperty(chanPrefix+CELL_MAX, max);
        }
        // non-channel specific components 
        double  area, perim, circ;
        area = RoiUtil.computeROIarea(proi)*(pixelScale*pixelScale);    
        perim = RoiUtil.computePerimieter(proi)*(pixelScale);  
        circ = RoiUtil.computeCircularity(proi); 
        pf.addNumericProperty(CELL_AREA, area);
        pf.addNumericProperty(CELL_PRIM, perim);
        pf.addNumericProperty(CELL_CIRC, circ);

    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void summariseAndSave() {
        
        // compute the names of the stats computed. 
        ArrayList<String> tempList = new ArrayList<>();
        int nchannels = model.getNchannels();
        String chanPrefix;
        for( int ch = 0; ch < nchannels; ch++ ){
            chanPrefix = CH_INDX + (ch+1);
            for(int i = 0; i < STAT_NAMES.length; i++ ){
                tempList.add(chanPrefix+STAT_NAMES[i]);
            }
        }
        // add the non-channel specific components
        tempList.add(CELL_AREA);
        tempList.add(CELL_PRIM);
        tempList.add(CELL_CIRC);
        
        String[] names;
        names = tempList.toArray(new String[tempList.size()]);
     
        // Now extract the values for a results table. 
        ResultsTable rt = FeaturesAndParentFeaturesToResultsTable.parentFeatureSpecifiedNumericalProperties2ResultsTable( model ,  names );
        rt.show("Cell_stats");    
        
        try{
            rt.save(model.getSaveFileDirectory()+ "\\" + "Cell_Intensity_stats.csv");
        }catch( Exception e ){
            System.out.println(e.getMessage());
        }          
        
    }
    
}
