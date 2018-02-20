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
package colocalisation.coefficients;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


import java.awt.Point;

/**
 *
 * Some of the code has been taken from.
 * https://imagej.nih.gov/ij/plugins/download/Image_Correlator.java
 *
 * @author mqbssep5
 */
public class PearsonsCorrelaionCoeff {

    /**
     * Blurs the image to reduce noise before performing the correlation on the
     * first two slices of the image.
     *
     * @param imp
     * @param roi
     * @param gaussianBlurrSigma
     * @return
     */
    public static double correlationTwoChannelImage(ImagePlus imp, Roi roi, double gaussianBlurrSigma) {

        ImageProcessor ip1 = imp.getStack().getProcessor(1).duplicate();
        ImageProcessor ip2 = imp.getStack().getProcessor(2).duplicate();

        ip1.blurGaussian(gaussianBlurrSigma);
        ip2.blurGaussian(gaussianBlurrSigma);

        if (roi == null) {
            return correlation(ip1, ip2);
        } else {
            return correlation(ip1, ip2, roi);
        }
    }

    /**
     * Performs the correlation between the two channels in the image.
     *
     * @param imp
     * @param roi
     * @return
     */
    public static double correlationTwoChannelImage(ImagePlus imp, Roi roi) {

        ImageProcessor ip1 = imp.getStack().getProcessor(1).duplicate();
        ImageProcessor ip2 = imp.getStack().getProcessor(2).duplicate();

        if (roi == null) {
            return correlation(ip1, ip2);
        } else {
            return correlation(ip1, ip2, roi);
        }
    }
    

    /**
     * Blurs the images with a Gaussian filter before computing the correlation. 
     * @param ip1
     * @param ip2
     * @param roi
     * @param gaussianBlurrSigma
     * @return 
     */
    public static double correlation(ImageProcessor ip1, ImageProcessor ip2, Roi roi, double gaussianBlurrSigma) {
         
        ip1 = ip1.duplicate();
        ip2 = ip2.duplicate();
        
        ip1.blurGaussian(gaussianBlurrSigma);
        ip2.blurGaussian(gaussianBlurrSigma);
        
        return correlation(ip1, ip2, roi);
        
    }

    public static double correlation(ImageProcessor ip1, ImageProcessor ip2, Roi roi) {

        FloatProcessor fp1 = ip1.convertToFloatProcessor();
        FloatProcessor fp2 = ip2.convertToFloatProcessor();

        // Get the pixel locations inside of the roi. 
        Point[] points = roi.getContainedPoints();
        int n = points.length;
        // extract the pixels and perform the correlation
        float[] pixels1, pixels2;

        pixels1 = new float[points.length];
        pixels2 = new float[points.length];

        int x, y;
        for (int i = 0; i < n; i++) {
            x = points[i].x;
            y = points[i].y;
            pixels1[i] = fp1.getPixelValue(x, y);
            pixels2[i] = fp2.getPixelValue(x, y);
        }

        double corr = calculateCorrelation(pixels1, pixels2);

        return corr;

    }

    public static double correlation(ImageProcessor ip1, ImageProcessor ip2) {

        FloatProcessor fp1 = ip1.convertToFloatProcessor();
        FloatProcessor fp2 = ip2.convertToFloatProcessor();

        // extract the pixels and perform the correlation
        float[] pixels1, pixels2;
        pixels1 = (float[]) fp1.getPixelsCopy();
        pixels2 = (float[]) fp2.getPixelsCopy();

        double corr = calculateCorrelation(pixels1, pixels2);

        return corr;

    }

    /**
     * Function taken from:
     * https://imagej.nih.gov/ij/plugins/download/Image_Correlator.java
     *
     * @param x
     * @param y
     * @return
     */
    // http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
    private static double calculateCorrelation(float[] x, float[] y) {
        double sumx = 0;
        double sumy = 0;
        int n = x.length;
        for (int i = 0; i < n; i++) {
            sumx += x[i];
            sumy += y[i];
        }
        double xmean = sumx / n;
        double ymean = sumy / n;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (x[i] - xmean) * (y[i] - ymean);
        }
        double sumx2 = 0;
        for (int i = 0; i < n; i++) {
            sumx2 += sqr(x[i] - xmean);
        }
        double sumy2 = 0;
        for (int i = 0; i < n; i++) {
            sumy2 += sqr(y[i] - ymean);
        }
        return sum / (Math.sqrt(sumx2) * Math.sqrt(sumy2));
    }

    private static double sqr(double x) {
        return x * x;
    }

}

//
//
//    /**
//     * Masks out spot like features with a Laplacian of Gaussian filter and then
//     * performs the correlation between the two images.
//     *
//     * @param imp a image containing two slices, one for each channel.
//     * @param sigma1 sigma value for the LoG filter 1
//     * @param sigma2 sigma value for the LoG filter 2
//     * @param thresh1 threshold for the Laplacian of Gaussian image.
//     * @param thresh2 threshold for the Laplacian of Gaussian image.
//     * @return
//     */
//    public static double correlationSpotMask2ChannelImage(ImagePlus imp, float sigma1, float sigma2, float thresh1, float thresh2) {
//
//        ImageProcessor ip1 = imp.getStack().getProcessor(1);
//        ImageProcessor ip2 = imp.getStack().getProcessor(2);
//
//        // create a masked image for each image using a LoG filter
//        ImageProcessor ip1SpotMasked1, ip1SpotMasked2;
//        ip1SpotMasked1 = ImageProcessorUtil.SpotEnhancingFilterMask(ip1, sigma1, thresh1, true);
//        ip1SpotMasked2 = ImageProcessorUtil.SpotEnhancingFilterMask(ip2, sigma2, thresh2, true);
////        new ImagePlus("Masked1 ", ip1SpotMasked1).show();
////        new ImagePlus("Masked2 ", ip1SpotMasked2).show();
//
//        // perform the correlation 
//        // perform the correlation 
//        double corr = correlation(ip1SpotMasked1, ip1SpotMasked2);
//
//        return corr;
//
//    }
//
//    /**
//     * Masks out spot like features with a Laplacian of Gaussian filter and then
//     * performs the correlation between the two images.
//     *
//     * @param ip1
//     * @param ip2
//     * @param sigma1 sigma value for the LoG filter 1
//     * @param sigma2 sigma value for the LoG filter 2
//     * @param thresh1 threshold for the Laplacian of Gaussian image.
//     * @param thresh2 threshold for the Laplacian of Gaussian image.
//     * @param roi
//     * @return
//     */
//    public static double correlationSpotMask( ImageProcessor ip1, ImageProcessor ip2, float sigma1, float sigma2, float thresh1, float thresh2, Roi roi ) {
//
//        // create a masked image for each image using a LoG filter
//        ImageProcessor ip1SpotMasked1, ip1SpotMasked2;
//        ip1SpotMasked1 = ImageProcessorUtil.SpotEnhancingFilterMask(ip1, sigma1, thresh1, true);
//        ip1SpotMasked2 = ImageProcessorUtil.SpotEnhancingFilterMask(ip2, sigma2, thresh2, true);
////        new ImagePlus("Masked1 ", ip1SpotMasked1).show();
////        new ImagePlus("Masked2 ", ip1SpotMasked2).show();
//
//        double corr;
//        if (roi == null) {
//            // perform the correlation 
//            corr = correlation(ip1SpotMasked1, ip1SpotMasked2);
//        } else {
//            // only use the pixels inside of the ROI. 
//            corr = correlation(ip1SpotMasked1, ip1SpotMasked2, roi);
//        }
//
//        return corr;
//
//    }
