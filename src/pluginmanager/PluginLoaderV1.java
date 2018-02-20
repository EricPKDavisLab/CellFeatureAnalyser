/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pluginmanager;

import abstractprocessors.AbstractParentFeatureProcessor;
import chromaticcorrection.ChromaticCorrection2Dprocessor;
import colocalisation.Colocalisation2Dprocessor;
import featuredetector.spotdetector.SpotDetectorMultiChannelProcessor;
import featuremathandstats.distance.Feature2FeatureDistanceProcessor;
import gui.CellAnalyserGUIModel;
import imagedomanmeasurement.ParentFeatureImageDomainIntensityMeasureProcessor;
import java.util.HashMap;

/**
 * A class used for loading the analysis streams.
 *
 * This class has to be modified for loading in plugins externally.
 *
 * @author mqbssep5
 */
public class PluginLoaderV1 {

    private String[] labels = new String[]{"Spot detect.","Spot/intens stats.", "Spot coloc."};

    private HashMap<String, AbstractParentFeatureProcessor[]> analysisProcessors;

    public PluginLoaderV1() {

    }

    public AbstractParentFeatureProcessor[] getProcessingStream( CellAnalyserGUIModel model, String s ) {
        if (s.equalsIgnoreCase(labels[0])) {
            return getSpotBrighnessProcessors(model);
        } else if(s.equalsIgnoreCase(labels[1])){
            return getSpotAndBrighnessProcessors(model);
        }else if (s.equalsIgnoreCase(labels[2])) {
            return getSpotColoc(model);
        }
        return null;
    }

    private AbstractParentFeatureProcessor[] getSpotBrighnessProcessors( CellAnalyserGUIModel model ) {
        AbstractParentFeatureProcessor[] processors = new AbstractParentFeatureProcessor[1];
        processors[0] = new SpotDetectorMultiChannelProcessor( model ); //
        return processors;
    }    
    
    private AbstractParentFeatureProcessor[] getSpotAndBrighnessProcessors( CellAnalyserGUIModel model ) {
        AbstractParentFeatureProcessor[] processors = new AbstractParentFeatureProcessor[2];
        processors[0] = new ParentFeatureImageDomainIntensityMeasureProcessor(model); //
        processors[1] = new SpotDetectorMultiChannelProcessor(model); //
        return processors;
    }

    private AbstractParentFeatureProcessor[] getSpotColoc( CellAnalyserGUIModel model ) {
        AbstractParentFeatureProcessor[] processors = new AbstractParentFeatureProcessor[4];
        processors[0] = new ChromaticCorrection2Dprocessor(model); //
        processors[1] = new ParentFeatureImageDomainIntensityMeasureProcessor(model); //
        processors[2] = new SpotDetectorMultiChannelProcessor(model); //
        processors[3] = new Feature2FeatureDistanceProcessor(model);
        //processors[3] = new Colocalisation2Dprocessor(model);
        return processors;
    }

    /**
     * @return the list of names for the analysis plugin streams.
     */
    public String[] getAnalysisSreamNames() {
        return labels;
    }

}
