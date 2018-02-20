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
package utilimageprocessor;

import ij.plugin.filter.Convolver;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * A class of static methods for ImageJ ImageProcessors
 *
 * @author mqbssep5
 */
public class ImageProcessorUtil {

    /**
     * Performs a local maxima detection via the morphological maxima filter.
     *
     * First a morphological maxima filter is applied to the image with a
     * defined radius. The intensity of each pixel location, before and after
     * maxima filtering, is compared. Pixel locations with the same intensity
     * value before and after filtering are where local maxima are situated.
     *
     * @param ip
     * @param rad
     * @return an image the same size as the input image. Locations of any local
     * maxima are given a value of 255, all other pixels are set to value zero.
     */
    public static ImageProcessor localMaxima(ImageProcessor ip, int rad) {

        ImageProcessor ipMaxFiltered = ip.duplicate();
        // perform a local maxima filtering. 
        RankFilters rf = new RankFilters();
        rf.rank(ipMaxFiltered, rad, RankFilters.MAX);

        // now comare the pixel intensities before and after filtering
        int width, height;
        width = ip.getWidth();
        height = ip.getHeight();

        ImageProcessor ipMaximaPoints = ip.duplicate();
        ipMaximaPoints.multiply(0.0);

        double pixbefore, pixafter;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //pixbefore = (int)Math.round(ip.getPixelValue(i, j));
                pixbefore = ip.getPixelValue(i, j);
                if (pixbefore > 0) {
                    //pixafter = (int)Math.round(ipMaxFiltered.getPixelValue(i, j));
                    pixafter = ipMaxFiltered.getPixelValue(i, j);
                    if (pixbefore == pixafter) {
                        ipMaximaPoints.putPixelValue(i, j, 255d);
                    }
                }

            }
        }

        return ipMaximaPoints;
    }

    /**
     * Applies the mask to the image ip.
     *
     *
     *
     * @param ip
     * @param ipMask
     */
    public static void applyMask(ImageProcessor ip, ImageProcessor ipMask) {

        int width, height;

        width = ip.getWidth();
        height = ip.getHeight();

        double pMask;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pMask = ipMask.getPixelValue(i, j);
                if (pMask > 0.0) {
                    // do nothing. The pixel is under the mask. 
                } else {
                    // set this pixel to zero
                    ip.putPixelValue(i, j, 0.0);
                }
            }
        }

    }

    /**
     * Creates a new image whose pixel values are the sum of the two input
     * processors.
     *
     * @param ip1
     * @param ip2
     * @return
     */
    public static ImageProcessor add(ImageProcessor ip1, ImageProcessor ip2) {

        ImageProcessor out = ip1.duplicate();

        int w, h;
        w = ip1.getWidth();
        h = ip1.getHeight();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                out.putPixelValue(i, j, out.getPixelValue(i, j) + ip2.getPixelValue(i, j));
            }
        }
        return out;
    }

    /**
     * Performs an element wise subtraction of the pixels in ip1 from ip2.
     *
     *
     *
     * @param ip1
     * @param ip2
     * @return a new image containing the result.
     */
    public static ImageProcessor sub(ImageProcessor ip1, ImageProcessor ip2) {

        ImageProcessor out = ip1.duplicate();

        int w, h;
        w = ip1.getWidth();
        h = ip1.getHeight();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                out.putPixelValue(i, j, out.getPixelValue(i, j) - ip2.getPixelValue(i, j));
            }
        }
        return out;
    }

//    public static ImageProcessor LaplacianOfGaussianFilter(ImageProcessor ip, float sigma) {
//
//        ImageProcessor ipOut = ip.duplicate();
//
//        int rad, kwidth;
//        float[] kernel;
//
//        // create the laplcian of gaussian filter kernel
//        rad = (int) Math.ceil(4f * sigma);
//        kwidth = 2 * rad + 1;
//        kernel = new float[kwidth * kwidth];
//        for (int i = 0; i < kwidth; i++) {
//            for (int j = 0; j < kwidth; j++) {
//                kernel[i + j * kwidth] = (2f * sigma * sigma - (float) (i - rad) * (i - rad) - (float) (j - rad) * (j - rad)) * (float) Math.exp(-(double) ((i - rad) * (i - rad) + (j - rad) * (j - rad)) / (2f * sigma * sigma)) / (sigma * sigma * sigma * sigma);
//            }
//        }
//
//        ipOut.convolve(kernel, (int) Math.sqrt(kernel.length), (int) Math.sqrt(kernel.length));
//
//        return ipOut;
//    }
//
//    public static void LaplacianOfGaussianFilter2(ImageProcessor ip, float sigma) {
//
//        int rad, kwidth;
//        float[] kernel;
//
//        // create the laplcian of gaussian filter kernel
//        rad = (int) Math.ceil(4f * sigma);
//        kwidth = 2 * rad + 1;
//        kernel = new float[kwidth * kwidth];
//        for (int i = 0; i < kwidth; i++) {
//            for (int j = 0; j < kwidth; j++) {
//                kernel[i + j * kwidth] = (2f * sigma * sigma - (float) (i - rad) * (i - rad) - (float) (j - rad) * (j - rad)) * (float) Math.exp(-(double) ((i - rad) * (i - rad) + (j - rad) * (j - rad)) / (2f * sigma * sigma)) / (sigma * sigma * sigma * sigma);
//            }
//        }
//
//        ip.convolve(kernel, (int) Math.sqrt(kernel.length), (int) Math.sqrt(kernel.length));
//
//    }
    /**
     * Locates the pixel with the highest intensity
     *
     * @param ip
     * @return the pixel location of the pixel with the greatest intensity.
     */
    public static int[] findMaxPixelLocation(ImageProcessor ip) {

        int w = ip.getWidth();
        int h = ip.getHeight();

        double max = Double.NEGATIVE_INFINITY;
        int x = 0, y = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                //
                if (ip.getPixelValue(i, j) > max) {
                    x = i;
                    y = j;
                    max = ip.getPixelValue(i, j);
                }

            }
        }

        return new int[]{x, y};
    }

    /**
     * Computes the intensity weighted centre of mass of the input image.
     *
     * @param ip
     * @return the (x,y) location for the centre of mass double[]{xCoM,yCoM}
     */
    public static double[] computeCoM(ImageProcessor ip) {

        int w = ip.getWidth();
        int h = ip.getHeight();

        double sum = 0;
        double x = 0, y = 0, pval;

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                //
                pval = ip.getPixelValue(i, j);
                x += pval * (double) i;
                y += pval * (double) j;
                // compute sum
                sum += pval;
            }
        }
        // intensity weighted centre of mass
        if (sum > 0.0) {
            x = x / sum;
            y = y / sum;
        } else {
            x = 0.5 * (double) w;
            y = 0.5 * (double) h;
        }

        return new double[]{x, y};
    }

    /**
     * Applies a Gaussian filter to the image with the defined sigma and then
     * filters the image with the 3 by 3 Laplacian filter.
     *
     * @param ip
     * @param sigma
     */
    public static void LaplacianOfGaussianFilter(ImageProcessor ip, float sigma) {

        GaussianBlur gb = new GaussianBlur();
        gb.blurGaussian(ip, sigma);

        // now apply the laplacian filter. 
        float[] laplacian;
//                    laplacian = new float[]{0f, 1f, 0f,
//                1f, -4f, 1f,
//                0f, 1f, 0f};
        laplacian = new float[]{1f, 1f, 1f,
            1f, -8f, 1f,
            1f, 1f, 1f};

        //ip.convolve(laplacian, 3, 3);
        Convolver conv = new Convolver();
        conv.setNormalize(false);
        conv.convolve(ip, laplacian, 3, 3);
        ip.multiply(-1f);
    }

    /**
     * The spot enhancing filter. It is based on two steps:
     *  1) Laplacian of Gaussian filtering of the image
     *  2) Thresholding of the image
     * 
     * @param ip 2D image slice 
     * @param sigma the standard deviation of the Gaussian component of filtering step.
     * @param threshold the threshold applied to the filtered image. 
     * This is typically a positive number. 
     * @param absoluteThreshold if true then the threshold value supplied is applied 
     * directly to the image. If false, then a new threshold value is computed in
     * the following way: threshold = threshold*std(LoG(Image)). 
     */
    public static void SpotEnhancingFilter( ImageProcessor ip, float sigma, float threshold, boolean absoluteThreshold ) {
        // filter
        LaplacianOfGaussianFilter(ip, sigma);

        if (!absoluteThreshold) {
            // threshold based on mean and standard deviation 
            ImageStatistics is = ip.getStatistics();
            // threshold the LoG image 
            threshold = (float) ( (is.stdDev) * threshold);

            System.out.println(" mean " + is.mean + " std " + is.stdDev);
        }

        float[] pixelsLoG = (float[]) ip.getPixels();
        for (int i = 0; i < pixelsLoG.length; i++) {
            if (pixelsLoG[i] > threshold) {// thresholdVal

            } else {
                pixelsLoG[i] = 0f;
            }
        }
    }

    public static ImageProcessor SpotEnhancingFilterMask(ImageProcessor ip, float sigma, float cfactor, boolean absolutethresh) {

        ip = ip.convertToFloat();

        ImageProcessor ipMasked = ip.duplicate();
        // filter
        LaplacianOfGaussianFilter(ip, sigma);

        float thresholdVal;
        if (absolutethresh) {
            thresholdVal = cfactor;
        } else {
            // threshold based on mean and standard deviation 
            ImageStatistics is = ip.getStatistics();
            // threshold the LoG image 
            thresholdVal = (float) (is.mean + (is.stdDev) * cfactor);
        }

        float[] pixelsLoG = (float[]) ip.getPixels();
        float[] imagePixels = (float[]) ipMasked.getPixels();
        for (int i = 0; i < pixelsLoG.length; i++) {
            if (pixelsLoG[i] > thresholdVal) {// thresholdVal

            } else {
                imagePixels[i] = 0f;
            }
        }

        return ipMasked;
    }

    /**
     * Normalised the pixel range from 0 to value, by dividing by the maximum
     * value and scaling by the value.
     *
     * @param ip
     * @param value
     */
    public static void normaliseToValue(ImageProcessor ip, double value) {
        double max = ip.getMax();
        ip.multiply((1d / max) * value);
    }

}
