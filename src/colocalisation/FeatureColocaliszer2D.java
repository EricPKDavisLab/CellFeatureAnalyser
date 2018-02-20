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
package colocalisation;

import colocalisation.coefficients.MandersColocalisationCoefficients;
import colocalisation.coefficients.PearsonsCorrelaionCoeff;
import featuredetector.spotdetector.LoGSpotDetector2D;
import featuredetector.spotdetector.SpotDetectorChannelProcessor;
import featuredetector.spotdetector.SpotDetectorCommon;
import featureobjects.Feature;
import featureobjects.FeatureOps;
import featureobjects.ParentFeature;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TestUtils;

/**
 * Performs the number crunching for the {@link Colocalisation2Dprocessor} class.
 * @author mqbssep5
 */
public class FeatureColocaliszer2D {

    private final ParentFeature pf;

    private final int nRandomisations;
    
    private int width, height;

    private ImageProcessor ipReference, ipRandomised, ipCandidateOriginal;

    private final int referenceChannel;

    private int nchannels;

    public static final String PCC_FEATURE_ORG = "PCC_FEATURE_ORG";

    public static final String PCC_FEATURE_RAND_MEAN = "PCC_FEATURE_RAND_MEAN";

    public static final String PCC_FEATURE_RAND_STD = "PCC_FEATURE_RAND_STD";

    public static final String PCC_FEATURE_TTEST = "PCC_FEATURE_TTEST";

    public static final String MOC1_FEATURE_ORG = "MOC1_FEATURE_ORG";

    public static final String MOC1_FEATURE_RAND_MEAN = "MOC1_FEATURE_RAND_MEAN";

    public static final String MOC1_FEATURE_RAND_STD = "MOC1_FEATURE_RAND_STD";

    public static final String MOC1_FEATURE_TTEST = "MOC1_FEATURE_TTEST";

    public static final String MOC2_FEATURE_ORG = "MOC2_FEATURE_ORG";

    public static final String MOC2_FEATURE_RAND_MEAN = "MOC2_FEATURE_RAND_MEAN";

    public static final String MOC2_FEATURE_RAND_STD = "MOC2_FEATURE_RAND_STD";

    public static final String MOC2_FEATURE_TTEST = "MOC2_FEATURE_TTEST";

    public static final String[] ALL_METRICS = new String[]{PCC_FEATURE_ORG,PCC_FEATURE_RAND_MEAN,PCC_FEATURE_RAND_STD,PCC_FEATURE_TTEST,MOC1_FEATURE_ORG,MOC1_FEATURE_RAND_MEAN,MOC1_FEATURE_RAND_STD,MOC1_FEATURE_TTEST,MOC2_FEATURE_ORG,MOC2_FEATURE_RAND_MEAN,MOC2_FEATURE_RAND_STD,MOC2_FEATURE_TTEST};
    
    boolean displayRandomisedData = true;
    
    /**
     *
     * @param pf
     * @param nRandomisations
     * @param referenceChannel
     */
    public FeatureColocaliszer2D(ParentFeature pf, int nRandomisations, int referenceChannel) {
        this.pf = pf;
        this.nRandomisations = nRandomisations;
        this.referenceChannel = referenceChannel;
    }

    public void showRandimisedImages( boolean showImages ){
        displayRandomisedData = showImages;
    }
    
    public void doColocalisations() {

        Rectangle bounds = pf.getBounds();
        width = bounds.width;
        height = bounds.height;
        // create the Reference image 
        ipReference = new FloatProcessor(width, height);
        // features for the reference channel. 
        ArrayList<Feature> refFeatures = pf.getFeatures(referenceChannel, SpotDetectorChannelProcessor.SPOT_FEATURE_NAME);

        plotFeatures(ipReference, refFeatures);

        //new ImagePlus("Reference", ipReference).show();
        nchannels = pf.getNchannels();

        ArrayList<Feature> candFeatures;

        PolygonRoi roi = pf.getFeatureBoundsShifted();

        double pcc, moc1, moc2;
        double pccOrg, moc1Org, moc2Org;
        double[] moc;
        
        ImageStack is = new ImageStack(width,height);
        ImagePlus imp;
        
        for (int c = 0; c < nchannels; c++) {
            // dont perform self correlation
            if (c == referenceChannel) {
                continue;
            }
            candFeatures = pf.getFeatures(c, SpotDetectorChannelProcessor.SPOT_FEATURE_NAME);

            // first get our actual colocalisation values before randomisation. 
            ipCandidateOriginal = new FloatProcessor(bounds.width, bounds.height);
            plotFeatures(ipCandidateOriginal, candFeatures);
            
            if(displayRandomisedData){
                is.addSlice(ipCandidateOriginal.duplicate());
            }
            
            pccOrg = PearsonsCorrelaionCoeff.correlation( ipReference.duplicate(), ipCandidateOriginal.duplicate(), roi );
            moc = MandersColocalisationCoefficients.mandersColocalisationCoefficients( ipReference.duplicate(), ipCandidateOriginal.duplicate(), roi );
            moc1Org = moc[0];
            moc2Org = moc[1];

            
            //System.out.println(" pcc " + pccOrg + " moc1 " + moc1Org +  " moc2 " + moc2Org );
            
            double[] pccs, moc1s, moc2s;
            pccs = new double[nRandomisations];
            moc1s = new double[nRandomisations];
            moc2s = new double[nRandomisations];
            // perform the ransomisations test. 
            for (int i = 0; i < nRandomisations; i++) {
                // create the randomised set. 
                ipRandomised = createRandomisedSet(candFeatures);
                
                if (displayRandomisedData) {
                    is.addSlice(ipRandomised.duplicate());
                }

                // compute the colocalisation Coeffs.
                pcc = PearsonsCorrelaionCoeff.correlation( ipReference.duplicate(), ipRandomised.duplicate(), roi );
                // mander overlap coefficients
                moc = MandersColocalisationCoefficients.mandersColocalisationCoefficients(ipReference.duplicate(), ipRandomised.duplicate(), roi);
                moc1 = moc[0];
                moc2 = moc[1];
                // save the values 
                pccs[i] = pcc;
                moc1s[i] = moc1;
                moc2s[i] = moc2;
                //System.out.println(" pcc " + pcc + " moc1 " + moc1 +  " moc2 " + moc2 );
            }
            double meanPCC, meanMoc1s, meanMoc2s, stdPCC, stdMoc1s, stdMoc2s;

            meanPCC = StatUtils.mean(pccs);
            meanMoc1s = StatUtils.mean(moc1s);
            meanMoc2s = StatUtils.mean(moc2s);
            // 
            stdPCC = Math.sqrt(StatUtils.variance(pccs));
            stdMoc1s = Math.sqrt(StatUtils.variance(moc1s));
            stdMoc2s = Math.sqrt(StatUtils.variance(moc2s));

            // perform T-tests to see if the original values are different from the randomised ones. 
            double tPCC, tMOC1, tMOC2;
            tPCC = TestUtils.tTest(pccOrg, pccs);
            tMOC1 = TestUtils.tTest(moc1Org, moc1s);
            tMOC2 = TestUtils.tTest(moc2Org, moc2s);

//            System.out.println(" PCC org " + pccOrg + " MOC1 org " + moc1Org + " MOC2 org " + moc2Org);
//            System.out.println(" PCC mc  " + meanPCC + " MOC1 mc " + meanMoc1s + " MOC2 mc " + meanMoc2s);
//            System.out.println(" tPCC  " + tPCC + " tMOC1  " + tMOC1 + " tMOC2 " + tMOC2);

            // save the values PCC
            pf.addNumericProperty(PCC_FEATURE_ORG, pccOrg);
            pf.addNumericProperty(PCC_FEATURE_RAND_MEAN, meanPCC);
            pf.addNumericProperty(PCC_FEATURE_RAND_STD, stdPCC);
            pf.addNumericProperty(PCC_FEATURE_TTEST, tPCC);
            // MOC1
            pf.addNumericProperty(MOC1_FEATURE_ORG, moc1Org);
            pf.addNumericProperty(MOC1_FEATURE_RAND_MEAN, meanMoc1s);
            pf.addNumericProperty(MOC1_FEATURE_RAND_STD, stdMoc1s);
            pf.addNumericProperty(MOC1_FEATURE_TTEST, tMOC1);
            // MOC2 
            pf.addNumericProperty(MOC2_FEATURE_ORG, moc2Org);
            pf.addNumericProperty(MOC2_FEATURE_RAND_MEAN, meanMoc2s);
            pf.addNumericProperty(MOC2_FEATURE_RAND_STD, stdMoc2s);
            pf.addNumericProperty(MOC2_FEATURE_TTEST, tMOC2);

            if(displayRandomisedData){
               imp = new ImagePlus("Randomised", is); 
               imp.show();
            }
            
            
        }

    }

    /**
     * Creates an image with the features placed in random positions. 
     * @param features
     * @return 
     */
    private ImageProcessor createRandomisedSet(ArrayList<Feature> features) {
        ArrayList<Feature> featuresRandomised = FeatureOps.randomizeFeaturePositions(pf, features, System.currentTimeMillis());
        ImageProcessor ip = new FloatProcessor(width,height);
        // plot the randomised features. 
        plotFeatures(ip, featuresRandomised);

        return ip;
    }

    /**
     * Plots the features into the {@link ImageProcessor}
     * @param ip
     * @param features 
     */
    private void plotFeatures(ImageProcessor ip, ArrayList<Feature> features) {

        double[] values;
        PolygonRoi roi;
        Point[] points;
        Point p;
        int n;
        double v;
        for (Feature f : features) {
            // intensity values for this spot. 
            values = (double[]) f.getObject(SpotDetectorCommon.SPOT_INTENSITY_PIXELS);
            // roi
            roi = f.getOutLine();
            // position of each pixel 
            points = roi.getContainedPoints();
            n = points.length;
            for (int i = 0; i < n; i++) {
                p = points[i];
                v = ip.getPixelValue(p.x, p.y);
                ip.putPixelValue(p.x, p.y, Math.max(values[i],v));
            }
        }

    }

}
