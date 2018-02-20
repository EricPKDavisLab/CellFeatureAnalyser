/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colocalisation.coefficients;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Point;
import utilimageprocessor.ImageProcessorUtil;

/**
 *
 * @author mqbssep5
 */
public class MandersColocalisationCoefficients {

     
    /**
     * Compute the Manders' Colocalization Coefficients for the two channels in 
     * the input image. It assumes that the images have been background corrected
     * and that the background regions take the value of zero. 
     *
     * Background structures are masked out using a Laplacian of Gaussian filter. 
     * 
     * @param imp
     * @param roi
     * @param sigma1
     * @param sigma2
     * @param thresh1
     * @param thresh2
     * @return
     */
    public static double[] mandersColocalisationCoefficientsTwoChannelImageLoGmasked( ImagePlus imp, Roi roi, float sigma1, float sigma2, float thresh1, float thresh2 ) {

        ImageProcessor ip1 = imp.getStack().getProcessor(1).duplicate();
        ImageProcessor ip2 = imp.getStack().getProcessor(2).duplicate();

        ImageProcessor ip1SpotMasked, ip2SpotMasked;
        ip1SpotMasked = ImageProcessorUtil.SpotEnhancingFilterMask(ip1, sigma1, thresh1, true);
        ip2SpotMasked = ImageProcessorUtil.SpotEnhancingFilterMask(ip2, sigma2, thresh2, true);        
        
        if (roi == null) {
            return mandersColocalisationCoefficients( ip1SpotMasked, ip2SpotMasked );
        } else {
            return mandersColocalisationCoefficients( ip1SpotMasked, ip2SpotMasked, roi );
        }
    }    
    
    /**
     * Compute the Manders' Colocalization Coefficients for the two channels in 
     * the input image. It assumes that the images have been background corrected
     * and that the background regions take the value of zero. 
     *
     * @param imp
     * @param roi
     * @return
     */
    public static double[] mandersColocalisationCoefficientsTwoChannelImage( ImagePlus imp, Roi roi ) {

        ImageProcessor ip1 = imp.getStack().getProcessor(1).duplicate();
        ImageProcessor ip2 = imp.getStack().getProcessor(2).duplicate();

        if (roi == null) {
            return mandersColocalisationCoefficients(ip1, ip2);
        } else {
            return mandersColocalisationCoefficients(ip1, ip2, roi);
        }
    }
        
    
    /**
     * Compute the Manders' Colocalization Coefficients for the two images. 
     * It assumes that the images have been background corrected and that the 
     * background regions take the value of zero. 
     * 
     * @param ip1
     * @param ip2
     * @param roi
     * @return 
     */
    public static double[] mandersColocalisationCoefficients( ImageProcessor ip1, ImageProcessor ip2, Roi roi ) {

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

        double[] corr = calculateMandersColocalisation(pixels1, pixels2);

        return corr;

    }

    
    /**
     * Compute the Manders' Colocalization Coefficients for the two images. 
     * It assumes that the images have been background corrected and that the 
     * background regions take the value of zero. 
     * 
     * @param ip1
     * @param ip2
     * @param roi
     * @return 
     */    
    public static double[] mandersColocalisationCoefficients(ImageProcessor ip1, ImageProcessor ip2) {

        FloatProcessor fp1 = ip1.convertToFloatProcessor();
        FloatProcessor fp2 = ip2.convertToFloatProcessor();

        // extract the pixels and perform the correlation
        float[] pixels1, pixels2;
        pixels1 = (float[]) fp1.getPixelsCopy();
        pixels2 = (float[]) fp2.getPixelsCopy();

        double[] corr = calculateMandersColocalisation(pixels1, pixels2);

        return corr;

    }

    /**
     * Compute the Manders' Colocalization Coefficients. It assumes that the
     * back ground pixels have a value of zero.
     *
     * @param r
     * @param g
     * @return a double[]{m1,m2}
     */
    private static double[] calculateMandersColocalisation(float[] r, float[] g) {

        double r_coloc, r_sum, g_coloc, g_sum;

        r_coloc = 0;
        r_sum = 0;
        g_coloc = 0;
        g_sum = 0;

        int n = r.length;

        for (int i = 0; i < n; i++) {
            // sums for normalisation 
            r_sum += r[i];
            g_sum += g[i];
            //
            if (g[i] > 0) {
                r_coloc += r[i];
            }
            if (r[i] > 0) {
                g_coloc += g[i];
            }
        }
        // compute the Manders' Colocalization Coefficients
        double m1, m2;
        m1 = r_coloc / r_sum;
        m2 = g_coloc / g_sum;
        
        return new double[]{m1, m2};
    }

}
