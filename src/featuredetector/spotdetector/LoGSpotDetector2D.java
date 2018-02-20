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

import featuredetector.AbstractFeatureDetector;
import featureobjects.Feature;
import featureobjects.FeatureOps;
import featureobjects.ParentFeature;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Wand;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling;
import inra.ijpb.watershed.MarkerControlledWatershedTransform2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import roiutil.RoiUtil;
import utilimageprocessor.ImageProcessorUtil;

/**
 * Locates spot like features using a Laplacian of Gaussian filter followed by
 * some morphological segmentation. TODO: fill out the
 *
 * @author mqbssep5
 */
public class LoGSpotDetector2D extends AbstractFeatureDetector {

    private final ImageProcessor ip;

    private ArrayList<Feature> features = new ArrayList<>();

    private final double thresh;

    private final int segmentationMethod;

    private final double logSigma;

    private int detectRad;
    
    private ImageProcessor ipSegmentedRegions;

    private ImageProcessor ipLoG;

    private ImageProcessor ipGaussianSmoothed;

    public int t;

    private final int connectivity;

    private final double pixelScale;

    /**
     * @param parentFeature
     * @param ip localised image data which bounds the parent feature.
     * @param logSigma standard deviation of the Gaussian component of the LoG
     * filter.
     * @param thresh absolute threshold which should be applied to the filtered
     * image to define the regions that contain spots.
     * @param segmentationMethod method used to segment the filtered data.
     * Either use {@link #INTENSITY_BASED_WATERSHEAD} or
     * {@link #CONNECTED_COMPONENTS}.
     * @param detectRad
     * @param connectivity
     * @param pixelScale
     */
    public LoGSpotDetector2D(ParentFeature parentFeature, ImageProcessor ip, double logSigma, double thresh, int segmentationMethod, int detectRad, int connectivity, double pixelScale) {
        super(parentFeature);
        this.ip = ip;
        this.thresh = thresh;
        this.segmentationMethod = segmentationMethod;
        this.logSigma = logSigma;
        this.connectivity = connectivity;
        this.pixelScale = pixelScale;
    }

    @Override
    public void run() {

        // copy the input image to perform the Laplacian of Gaussian filtering on. 
        ipLoG = ip.convertToFloat();

        boolean useAbsoluteThreshold = true;
        // Apply the laplacian of gaussian filter to the image     
        ImageProcessorUtil.SpotEnhancingFilter(ipLoG, (float) logSigma, (float) thresh, useAbsoluteThreshold);

        //new ImagePlus("LoG", ipLoG).show();
        // Copy the raw image and smooth with a gaussian filer. This smoothed image
        // is used to estimate things like the spot intensity and background values.  
        ipGaussianSmoothed = ip.duplicate();
        ipGaussianSmoothed.blurGaussian(1.5);

        // threshold the image to create a binary mask
        ImageProcessor ipBinMask = ipLoG.duplicate();
        float[] pixels = (float[]) ipBinMask.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] > 0) {
                pixels[i] = 1f;
            }
        }

        if (segmentationMethod == SpotDetectorCommon.CONNECTED_COMPONENT_SEGMENTATION) {
            // get the connected components in the binary mask
            FloodFillComponentsLabeling ffl = new FloodFillComponentsLabeling(connectivity);
            ipSegmentedRegions = ffl.computeLabels(ipBinMask);
        } else if (segmentationMethod == SpotDetectorCommon.INTENSITY_BASED_WATERSHED_SEGMENTATION) {
            // locate any local maxima in the image. 
            ImageProcessor ipMaxima = ImageProcessorUtil.localMaxima(ipLoG, detectRad);
            // There may be flat maxima, therefore we will use a region labelling algorithm
            // get the connected components of plautus.
            FloodFillComponentsLabeling ffl = new FloodFillComponentsLabeling(connectivity);
            ipMaxima = ffl.computeLabels(ipMaxima);

            // invert the density image to form catchment basins for watershed. 
            ImageProcessor ipIntensityBasins = ipLoG.duplicate();
            ipIntensityBasins.invert();

            // perform the watershed algorithm to the data to segment the regions. 
            MarkerControlledWatershedTransform2D wstf;
            wstf = new MarkerControlledWatershedTransform2D(ipIntensityBasins, ipMaxima, ipBinMask, connectivity);
            wstf.setVerbose(false);
            ipSegmentedRegions = wstf.applyWithPriorityQueue();
        }

        //new ImagePlus("Water shead segmentation image", ipSegmentedRegions).show();
        // extract the outlines of the cells from the segmented image
        features = getSpots(ipSegmentedRegions);

        // Now filter out the detected spots outside of the ROI of our feature.
        FeatureOps.removeFeaturesOutsideOfROI(features, parentFeature.getFeatureBoundsShifted());

    }

    /**
     * Extract the outlines of the segmented regions using the Wand ROI tool.
     *
     * @param ipRegions
     */
    private ArrayList<Feature> getSpots(ImageProcessor ipRegions) {

        int nLabels = (int) Math.round(ipRegions.getMax());

        //System.out.println(" n Labels " + nLabels);
        // get a pixel coordinate one of the pixels in each segmented region.
        int[] xP, yP, lab;

        xP = new int[nLabels];
        yP = new int[nLabels];
        lab = new int[nLabels];
        Arrays.fill(lab, -1);

        int w, h;
        w = ipRegions.getWidth();
        h = ipRegions.getHeight();

        int pval;
        int idx;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                pval = Math.round(ipRegions.getPixelValue(i, j));
                if (pval > 0) {
                    idx = pval - 1;
                    if (lab[idx] < 0) {
                        xP[idx] = i;
                        yP[idx] = j;
                        lab[idx] = pval;
                        //System.out.println("" + idx);
                    }
                }
            }
        }

        // 
        ArrayList<Feature> spots = new ArrayList<>(nLabels);

        int idCounter = 0;

        // Now use the wand tracer to find the outline of the labelled regions
        for (int i = 0; i < lab.length; i++) {
            // Get the roi which defines the edge of the detected spot.
            PolygonRoi roi = getRoiLabel(ipRegions, xP[i], yP[i]);
            // Create the spot object 
            Feature spot = createSpotObject(roi, ipGaussianSmoothed, idCounter);

            spots.add(spot);
            idCounter++;

        }

        return spots;

    }

    /**
     * Create the spot object from the region of interest.
     */ 
    private Feature createSpotObject( PolygonRoi roi, ImageProcessor ipIntensityMeasureImage, int spotID ) {

        // get the contained points inside the ROI. 
        Point[] points = roi.getContainedPoints();
        // compute the centre of mass of the points. 
        int nContainedPoints = points.length;
        double[] xPoints = new double[nContainedPoints];
        double[] yPoints = new double[nContainedPoints];
        double[] intensity = new double[nContainedPoints];

        double val;
        for (int i = 0; i < nContainedPoints; i++) {
            // get the xy and intensity points
            xPoints[i] = (double) points[i].x;
            yPoints[i] = (double) points[i].y;

            // get the intensity of the smoothed image to compute other stats... mean intensity etc. 
            intensity[i] = ipIntensityMeasureImage.getPixelValue(points[i].x, points[i].y);
        }

        // extract the local box surrounding the cell roi. 
        Rectangle bounds;
        bounds = roi.getBounds();

        // Estimate the background value by dilating the points on the outline
        // of the spot, measuring the intensity on the dilated points and taking 
        // the median value.
        int[] xOutline = Arrays.copyOf(roi.getPolygon().xpoints, roi.getPolygon().npoints);
        int[] yOutline = Arrays.copyOf(roi.getPolygon().ypoints, roi.getPolygon().npoints);

        int nPoints = roi.getPolygon().npoints;

        int x0 = bounds.x;
        int y0 = bounds.y;

        // Shift the postion of the polygon to 1.
        for (int i = 0; i < nPoints; i++) {
            xOutline[i] = xOutline[i] - x0 + 1;
            yOutline[i] = yOutline[i] - y0 + 1;
        }

        // create a new smaller image to perform the dilation 
        ImageProcessor ipDilate = new ByteProcessor(bounds.width + 2, bounds.height + 2);
        ipDilate.setColor(255);
        ipDilate.setValue(255);

        PolygonRoi proI2 = new PolygonRoi(xOutline, yOutline, nPoints, PolygonRoi.POLYGON);
        ipDilate.fill(proI2);
        ipDilate.erode(); //(dilating the high values with errosion operation)
        // get the outline of the dilated region 
        Wand w = new Wand(ipDilate);
        w.autoOutline(xOutline[0], yOutline[0]);
        // get our dilated 
        proI2 = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, PolygonRoi.POLYGON);

        // shift the new roi back to the large image coordinates
        proI2.setLocation(x0 - 1, y0 - 1);
        // expand the ROI so that we have a point at each pixel corner it meets. 
        proI2 = RoiUtil.pixelExtensiveDuplicate(proI2);
        
        int[] fulloutlineY, fulloutlineX;
        
        fulloutlineX = proI2.getXCoordinates();
        fulloutlineY = proI2.getYCoordinates();

        double[] perimValues = new double[fulloutlineX.length];
        for (int i = 0; i < fulloutlineX.length; i++) {
            perimValues[i] = ipIntensityMeasureImage.getPixelValue(fulloutlineX[i], fulloutlineY[i]);
        }

        // compute the median value and use as the estimate for the background level. 
        Median med = new Median();
        double bg = med.evaluate(perimValues);

        // remove the background value from the intensity so we can compute the mean etc. 
        double[] intensity2 = new double[intensity.length];
        for (int i = 0; i < intensity2.length; i++) {
            intensity2[i] = Math.max(intensity[i] - bg, 0);
        }
        double sum = 0d, xCoM = 0d, yCoM = 0d;
        // Compute the centreo of mass of the object now we have removed the background value
        for (int i = 0; i < nContainedPoints; i++) {
            val = intensity2[i];
            sum += val;
            xCoM += xPoints[i] * val;
            yCoM += yPoints[i] * val;
        }
        // Centre of mass
        xCoM = xCoM / sum;
        yCoM = yCoM / sum;

        // Compute the intensity stats. 
        double spotMax = StatUtils.max(intensity2);
        double spotMean = StatUtils.mean(intensity2);

        // create a new spot object 
        //roi = RoiUtil.upSamplePolygonPointFactorOfTwo(roi);
        //roi = RoiUtil.getHalfWayPointsPolygon(roi);              
        
//        // Compute the area of the roi and check that it meets our min area threshold 
//        double area = RoiUtil.computeROIarea(roi)* pixelScale * pixelScale;
//        double perimeter = RoiUtil.computePerimieter(roi) * pixelScale;
//        double circularity = RoiUtil.computeCircularity(roi);

        // Compute the area of the roi and check that it meets our min area threshold 
        double area = RoiUtil.computeROIarea(roi)* pixelScale * pixelScale;
        double perimeter = RoiUtil.computePerimieterSmallRegion(roi) * pixelScale;
        double circularity = RoiUtil.computeCircularitySmallParticle(roi);
          
        Feature spot = new Feature(spotID, roi, xCoM, yCoM);

        // save the stats 
        spot.addNumericFeature(SpotDetectorCommon.SPOT_AMPLITUDE, spotMax);
        spot.addNumericFeature(SpotDetectorCommon.SPOT_MEAN_VALUE, spotMean);
        spot.addNumericFeature(SpotDetectorCommon.SPOT_BG_USED, bg);
        spot.addNumericFeature(SpotDetectorCommon.SPOT_SUM_INTENSITY, sum );
        // centre of mass position in physical units
        spot.addNumericFeature(SpotDetectorCommon.COM_X, xCoM * pixelScale);
        spot.addNumericFeature(SpotDetectorCommon.COM_Y, yCoM * pixelScale);
        spot.addNumericFeature(SpotDetectorCommon.COM_X_PIX, xCoM);
        spot.addNumericFeature(SpotDetectorCommon.COM_Y_PIX, yCoM);
        spot.addNumericFeature(SpotDetectorCommon.SPOT_AREA, area);   
        spot.addNumericFeature(SpotDetectorCommon.SPOT_PERIMETER, perimeter);
        spot.addNumericFeature(SpotDetectorCommon.SPOT_CIRCULARITY, circularity );
        spot.addNumericFeature(SpotDetectorCommon.SPOT_ID, new Double(spotID));        
        
        spot.addObject(SpotDetectorCommon.SPOT_INTENSITY_PIXELS, intensity2);

        return spot;

    }

    /**
     * Returns the roi bounding the region covered by the pixel value at the
     * position of the image at the specified (x,y) location.
     *
     * @param ip
     * @param labelID
     * @return
     */
    private PolygonRoi getRoiLabel( ImageProcessor ip, int xLoc, int yLoc ) {

        // wand tool for drawing around the 
        Wand wand = new Wand(ip);
        wand.autoOutline(xLoc, yLoc);

        // extract the points from this wand
        int[] xPoints = wand.xpoints;
        int[] yPoints = wand.ypoints;
        int nPoints = wand.npoints;

        PolygonRoi proi = new PolygonRoi(xPoints, yPoints, nPoints, PolygonRoi.POLYGON);
        // create an rou which has a point at the start of each pixel. 
        proi = RoiUtil.pixelExtensiveDuplicate(proi);

        return proi;
    }

    /**
     * @return the labelled regions corresponding to the areas covered by each
     * cell.
     */
    public ImageProcessor getLabelledRegions() {
        return ipSegmentedRegions;
    }

    /**
     * @return the filtered image. e.g. the input image filtered by the
     * Laplacian of Gaussian filter.
     */
    public ImageProcessor getFilteredImage() {
        return ipLoG;
    }

    @Override
    public ArrayList<Feature> getFeatures() {
        return features;
    }

}


//    private int[][] expandToAllPoints(int[] xpoints, int[] ypoints) {
//
//        // compute the number of pixels between each point on the polygone.
//        int nPixels = 0;
//        int i_plusone;
//        for (int i = 0; i < xpoints.length; i++) {
//            i_plusone = i + 1;
//            if (i_plusone == xpoints.length) {
//                i_plusone = 0;
//            }
//            nPixels += Math.abs(xpoints[i_plusone] - xpoints[i]) + Math.abs(ypoints[i_plusone] - ypoints[i]);
//        }
//
//        //System.out.println(" nPoints " + xpoints.length + " nPixels " + nPixels);
//        // The output arrays
//        int[] xpointsFull = new int[nPixels];
//        int[] ypointsFull = new int[nPixels];
//
//        // get the coordinates for each point along the horizontal and vertical lines. 
//        int d, dx, dy, sign_dx, sign_dy, x, y;
//        int counter = 0;
//
//        for (int i = 0; i < xpoints.length; i++) {
//            i_plusone = i + 1;
//            if (i_plusone == xpoints.length) {
//                i_plusone = 0;
//            }
//            // The straight line distance between the two points
//            dx = xpoints[i_plusone] - xpoints[i];
//            dy = ypoints[i_plusone] - ypoints[i];
//
//            // absolute number of integer pixel positions between the points
//            d = Math.abs(dx) + Math.abs(dy);
//
//            // values for these will be either -1, 0, 1
//            sign_dx = (int) Math.signum(dx);
//            sign_dy = (int) Math.signum(dy);
//            // get the pixel locations 
//            for (int j = 0; j < d; j++) {
//                x = xpoints[i] + j * sign_dx;
//                y = ypoints[i] + j * sign_dy;
//
//                //System.out.println(" x " + x + " y " + y);
//                xpointsFull[counter] = x;
//                ypointsFull[counter] = y;
//
//                counter++;
//            }
//
//        }
//
//        return new int[][]{xpointsFull, ypointsFull};
//
//    }