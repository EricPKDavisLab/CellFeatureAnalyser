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
package featuremathandstats.distance;

import featureobjects.Feature;
import ij.gui.Plot;
import java.awt.Color;
import java.util.ArrayList;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * Functions for computing, plotting and saving histograms for the {@link Feature}
 * {@link Feature} distances.
 *
 * @author mqbssep5
 */
public class Feature2FeatureDistHistogram {

    
    /**
     * Plots a single mean histogram. 
     * @param bins
     * @param means
     * @param title
     * @return 
     */
    public static Plot showHisto( double[] bins, double[] means, String title ){
        Plot p = new Plot("Means and standard deviation " + title, "NND", "Counts");
        p.addPoints(bins, means, Plot.BOX);
        return p;
    }   
    
    /**
     * Plots a single mean histogram with its standard deviation as error bars. 
     * @param bins
     * @param means
     * @param stdevs
     * @param title
     * @return 
     */
    public static Plot showHistoMeansAndStdevs( double[] bins, double[] means, double[] stdevs, String title ){
        Plot p = new Plot("Means and standard deviation " + title, "NND", "Counts");
        p.addPoints(bins, means, Plot.BOX);
        p.addErrorBars( stdevs );
        return p;
    }
    
    /**
     * For comparing two mean histograms with errors.  
     * 
     * @param bins
     * @param meansHist1
     * @param stdHists1
     * @param meansHist2
     * @param stdHist2
     * @param title
     * @param set1Name - name for figure legend 
     * @param set2Name - name for figure legend 
     * @return 
     */
    public static Plot compareTwoMeanHistogramsWithErrors( double[] bins, double[] meansHist1, double[] stdHists1, double[] meansHist2, double[] stdHist2, String title, String set1Name, String set2Name ) {
        
        Plot p = new Plot("Comparitive Histograms. " + title, "NND", "Counts");
        p.addPoints(bins, meansHist1, Plot.BOX);
        p.addErrorBars(stdHists1);
        p.setColor(Color.RED);
        p.addPoints(bins, meansHist2, Plot.BOX);
        p.addErrorBars(stdHist2);
        p.addLegend( set1Name + "\n" + set2Name );
        return p;
    }
    
    /**
     * For plotting a single histogram with the mean and standard deviation of 
     * another set of histograms. 
     * @param bins
     * @param refHist
     * @param means
     * @param stdevs
     * @param title
     * @param set1Name - name for figure legend 
     * @param set2Name - name for figure legend 
     * @return 
     */
    public static Plot showOriginalVsRandomisedHistograms( double[] bins, double[] refHist, double[] means, double[] stdevs, String title, String set1Name, String set2Name  ) {
        
        Plot p = new Plot("Comparitive Histograms. " + title, "NND", "Counts");
        p.addPoints(bins, refHist, Plot.BOX);
        p.setColor(Color.RED);
        p.addPoints(bins, means, Plot.BOX);
        p.addErrorBars(stdevs);
        p.addLegend( set1Name + "\n" + set2Name );
        return p;
    }

    /**
     * Assuming each array is a histogram, this function normalised the sum of
     * all to one.
     *
     * @param histos
     * @return normalised histograms.
     */
    public static double[][] normaliseSumOfAll2one(double[][] histos) {

        int n = histos.length;
        double[][] normalised = new double[n][];
        // do on all. 
        for (int i = 0; i < n; i++) {
            normalised[i] = normaliseSum2one(histos[i]);
        }
        return normalised;
    }

    /**
     * Computes the mean and standard deviation of the histogram.
     *
     * @param histos
     * @return new double[][]{ means, stdevs }
     */
    public static double[][] computeMeanAndStdevHistogram( ArrayList<double[]> histos ) {
        double[][] values;
        values = histos.toArray(new double[histos.size()][]);
        // Compute the mean and standard deviation 
        return computeMeanAndStdevHistogram(values);
    }

    /**
     * Computes the mean and standard deviation of the histograms which have
     * first been normalised to 1.
     *
     * @param histos
     * @return new double[][]{ means, stdevs }
     */
    public static double[][] computeMeanAndStandardDeviationOfNormalisedHistos( ArrayList<double[]> histos ) {

        double[][] histo = histos.toArray(new double[histos.size()][]);
        double[][] histoNormalised = normaliseSumOfAll2one(histo);

        return computeMeanAndStdevHistogram(histoNormalised);
    }

    /**
     * Computes the mean and standard deviation of the histograms which have
     * first been normaised to 1.
     *
     * @param histo
     * @return new double[][]{ means, stdevs }
     */
    public static double[][] computeMeanAndStandardDeviationOfNormalisedHistos(double[][] histo) {

        double[][] histoNormalised = normaliseSumOfAll2one(histo);

        return computeMeanAndStdevHistogram(histoNormalised);
    }

    /**
     * Computes the mean and standard deviation of the histogram.
     *
     * @param histo
     * @return new double[][]{ means, stdevs }
     */
    public static double[][] computeMeanAndStdevHistogram( double[][] histo ) {

        int nBins = histo[0].length;
        double[] means, stdevs;
        means = new double[nBins];
        stdevs = new double[nBins];

        int nhistos = histo.length;
        double[] binValues = new double[nhistos];
        double m, sd;
        for (int i = 0; i < nBins; i++) {
            // extract all the values for the current bin. 
            for (int j = 0; j < nhistos; j++) {
                binValues[j] = histo[j][i];
            }
            // compute the mean and standard deviation. 
            m = StatUtils.mean(binValues);
            sd = Math.sqrt(StatUtils.variance(binValues, m));
            means[i] = m;
            stdevs[i] = sd;
            // record the bins
        }

        return new double[][]{means, stdevs};
    }

    /**
     * Normalises the sum of the values in the array to 1.
     *
     * @param values
     * @return
     */
    public static double[] normaliseSum2one(double[] values) {
        double sum;
        sum = StatUtils.sum(values);
        int n = values.length;
        double[] normaised = new double[n];
        for (int i = 0; i < n; i++) {
            normaised[i] = values[i] / sum;
        }
        return normaised;
    }

}
