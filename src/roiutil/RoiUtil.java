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
package roiutil;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Arrays;

/**
 * A class with static methods which can be used to manipulate or perform
 * operations with ImageJ Roi objects.
 *
 * @author mqbssep5
 */
public class RoiUtil {

    /**
     * Computes the area of a rectangular or PolygonRoi in units of pixels.
     *
     * Some code is taken from:
     * https://imagej.nih.gov/ij/developer/source/index.html
     *
     * @param roi
     * @return
     */
    public static int computeROIareaInPixels(Roi roi) {

        if (roi instanceof PolygonRoi) {
            Polygon p = roi.getPolygon();
            // taken from https://imagej.nih.gov/ij/developer/source/index.html
            int carea = 0;
            int iminus1;
            for (int i = 0; i < p.npoints; i++) {
                iminus1 = i - 1;
                if (iminus1 < 0) {
                    iminus1 = p.npoints - 1;
                }
                carea += (p.xpoints[i] + p.xpoints[iminus1]) * (p.ypoints[i] - p.ypoints[iminus1]);
            }
            double area = (Math.abs(carea / 2.0));
            return (int) area;
        } else {
            Rectangle r = roi.getBounds();
            return (r.width * r.height);
        }
    }

    /**
     * Returns a deep duplicate of the input {@link PolygonRoi}.
     *
     * @param roi
     * @return a deep duplicate of the input
     */
    public static PolygonRoi duplicatePolygon(PolygonRoi roi) {

        PolygonRoi out;
        int[] xPoints = Arrays.copyOf(roi.getPolygon().xpoints, roi.getPolygon().xpoints.length);
        int[] yPoints = Arrays.copyOf(roi.getPolygon().ypoints, roi.getPolygon().ypoints.length);

        out = new PolygonRoi(xPoints, yPoints, roi.getPolygon().npoints, roi.getType());

        return out;
    }

    /**
     * Returns a deep duplicate of the input {@link PolygonRoi}.
     *
     * Performs the following operation on the x,y points in roi:
     *
     * xPoints[i] = xPoints[i] + dx; yPoints[i] = yPoints[i] + dy;
     *
     *
     * @param roi
     * @param dx
     * @param dy
     * @return a deep duplicate of the input
     */
    public static PolygonRoi translatedCopy( PolygonRoi roi, int dx, int dy ) {

        PolygonRoi out;
        int[] xPoints = Arrays.copyOf(roi.getPolygon().xpoints, roi.getPolygon().npoints);
        int[] yPoints = Arrays.copyOf(roi.getPolygon().ypoints, roi.getPolygon().npoints);

        for (int i = 0; i < roi.getPolygon().npoints; i++) {
            xPoints[i] = xPoints[i] + dx;
            yPoints[i] = yPoints[i] + dy;
        }

        out = new PolygonRoi(xPoints, yPoints, roi.getPolygon().npoints, roi.getType());

        return out;
    }

    /**
     *
     * @param roi the original ROI.
     * @return A copy of the PolygonRoi which has a pixel position for each
     * point on the horizonal and vertical sections of the outline (e.g. the
     * corner of each each pixel).
     */
    public static PolygonRoi pixelExtensiveDuplicate(PolygonRoi roi) {

        int[] xpoints, ypoints;

        xpoints = Arrays.copyOf(roi.getPolygon().xpoints, roi.getPolygon().npoints);
        ypoints = Arrays.copyOf(roi.getPolygon().ypoints, roi.getPolygon().npoints);

        // compute the number of pixels between each point on the polygon.
        int nPixels = 0;
        int i_plusone;
        for (int i = 0; i < xpoints.length; i++) {
            i_plusone = i + 1;
            if (i_plusone == xpoints.length) {
                i_plusone = 0;
            }
            nPixels += Math.abs(xpoints[i_plusone] - xpoints[i]) + Math.abs(ypoints[i_plusone] - ypoints[i]);
        }

        // The output arrays
        int[] xpointsFull = new int[nPixels];
        int[] ypointsFull = new int[nPixels];

        // get the coordinates for each point along the horizontal and vertical lines. 
        int d, dx, dy, sign_dx, sign_dy, x, y;
        int counter = 0;

        for (int i = 0; i < xpoints.length; i++) {
            i_plusone = i + 1;
            if (i_plusone == xpoints.length) {
                i_plusone = 0;
            }
            // The straight line distance between the two points
            dx = xpoints[i_plusone] - xpoints[i];
            dy = ypoints[i_plusone] - ypoints[i];

            // absolute number of integer pixel positions between the points
            d = Math.abs(dx) + Math.abs(dy);

            // values for these will be either -1, 0, 1
            sign_dx = (int) Math.signum(dx);
            sign_dy = (int) Math.signum(dy);
            // get the pixel locations 
            for (int j = 0; j < d; j++) {
                x = xpoints[i] + j * sign_dx;
                y = ypoints[i] + j * sign_dy;

                //System.out.println(" x " + x + " y " + y);
                xpointsFull[counter] = x;
                ypointsFull[counter] = y;

                counter++;
            }

        }

        return new PolygonRoi(xpointsFull, ypointsFull, ypointsFull.length, roi.getType());

    }

    /**
     * Returns a new polygon, which consists of twice as many points as the
     * input, where the additional points lie half way between each point in the
     * original polygon.
     *
     * @param roi
     * @return
     */
    public static PolygonRoi upSamplePolygonPointFactorOfTwo( PolygonRoi roi ) {

        // get the original polygon points 
        FloatPolygon fp = roi.getFloatPolygon();
        float[] xp, yp;
        xp = fp.xpoints;
        yp = fp.ypoints;
        
        int n = fp.npoints;

        float[] xpUS, ypUS;
        xpUS = new float[2*n];
        ypUS = new float[2*n];
        
        int j;
        xpUS[0] = xp[0];
        ypUS[0] = yp[0];
        for(int i = 1; i < n; i++){
            // index in the output array. 
            j = 2*i;
            // even points        
            xpUS[j] = xp[i];
            ypUS[j] = yp[i];
            // new points (odd points, half way points between the existing points)
            xpUS[j-1] = (xp[i] + xp[i-1])/2f;
            ypUS[j-1] = (yp[i] + yp[i-1])/2f; 
        }
        // last point in the roi. 
        xpUS[2*n - 1] = (xp[n-1] + xp[0])/2f;
        ypUS[2*n - 1] = (yp[n-1] + yp[0])/2f;
        
        // Return a new polygon with the new positions. 
        return new PolygonRoi(xpUS, ypUS, 2*n, roi.getType());
    }
    
    /**
     * Returns a new PolygonROI where the new polygon positions are half way between
     * each point in the input polygon. 
     * @param roi
     * @return a new polygon as described above. 
     */
    public static  PolygonRoi getHalfWayPointsPolygon( PolygonRoi roi ){
        
        // perimeter 
        FloatPolygon fp = roi.getFloatPolygon();
        float[] xp, yp;

        xp = fp.xpoints;
        yp = fp.ypoints;
        
        // Get the positions of the points which are half way between each of the
        // current points. 
        float[] xpHW, ypHW;
        xpHW = new float[fp.npoints];
        ypHW = new float[fp.npoints];
        // Compute the locations of the non-corner points of the polygon. 
        for(int i = 0; i < fp.npoints - 1; i++ ){
           xpHW[i] = (xp[i] + xp[i+1])/2f;
           ypHW[i] = (yp[i] + yp[i+1])/2f;
        }
        xpHW[fp.npoints - 1] = (xp[fp.npoints - 1] + xp[0])/2f;
        ypHW[fp.npoints - 1] = (yp[fp.npoints - 1] + yp[0])/2f;
        
        return new PolygonRoi(xpHW, ypHW, fp.npoints, roi.getType());
    }

    /**
     * Computes the perimeter of the Polygon ROI.
     *
     * @param roi
     * @return the perimeter of the roi.
     */
    public static double computePerimieter(PolygonRoi roi) {

        double dX, dY, perim;
        // perimeter 
        FloatPolygon fp = roi.getFloatPolygon();
        float[] xp, yp;

        xp = fp.xpoints;
        yp = fp.ypoints;
        perim = 0;
        int iminus1;
        for (int i = 0; i < fp.npoints; i++) {
            iminus1 = i - 1;
            if (iminus1 < 0) {
                iminus1 = fp.npoints - 1;
            }
            // perimeter
            dX = xp[iminus1] - xp[i];
            dY = yp[iminus1] - yp[i];
            // compute distance between points. 
            perim += Math.sqrt(dX * dX + dY * dY);
        }
        double perimeter = perim;

        return perimeter;
    }

    /**
     * Computes the perimeter of small Polygon ROIs covering only a small number
     * pixels. For small areas containing a few pixels, which have a very,
     * 'blockey' or pixelated appearance, the perimeter estimate computed by the
     * conventional method (computing the sum of the Euclidean distance between
     * the points in the polygon which are usually corner points), is generally
     * an over estimate.
     *
     * Here the perimeter is computed by calculating the sum of the distance
     * between the (x,y) positions which lie half way between each point of the
     * polygon corner positions.
     *
     * @param roi
     * @return the perimeter of the roi.
     */
    public static double computePerimieterSmallRegion( PolygonRoi roi ) {

        double dX, dY, perim;
        // perimeter 
        FloatPolygon fp = getHalfWayPointsPolygon(roi).getFloatPolygon();
        float[] xp, yp;

        xp = fp.xpoints;
        yp = fp.ypoints;
              
        perim = 0;
        int iminus1;
        for (int i = 0; i < fp.npoints; i++) {
            iminus1 = i - 1;
            if (iminus1 < 0) {
                iminus1 = fp.npoints - 1;
            }
            // perimeter
            dX = xp[iminus1] - xp[i];
            dY = yp[iminus1] - yp[i];
            // compute distance between points. 
            perim += Math.sqrt(dX * dX + dY * dY);
        }
        double perimeter = perim;

        return perimeter;
    }

    /**
     * Computes the area of a polygon roi.
     *
     * @param roi
     * @return
     */
    public static double computeROIarea( PolygonRoi roi ) {

        Polygon p = roi.getPolygon();
        // taken from https://imagej.nih.gov/ij/developer/source/index.html
        int carea = 0;
        int iminus1;
        for (int i = 0; i < p.npoints; i++) {
            iminus1 = i - 1;
            if (iminus1 < 0) {
                iminus1 = p.npoints - 1;
            }
            carea += (p.xpoints[i] + p.xpoints[iminus1]) * (p.ypoints[i] - p.ypoints[iminus1]);
        }
        double area = (Math.abs(carea / 2.0));

        return area;
    }

    /**
     * Computes the circularity of the {@link PolygonRoi} using the following
     * equation:
     *
     * 4*pi*area/(perimeter^2)
     *
     * @param roi
     * @return the circularity of the {@link PolygonRoi}
     */
    public static double computeCircularitySmallParticle(PolygonRoi roi) {

        PolygonRoi roiS = getHalfWayPointsPolygon( roi );
        
        double area, perimeter, circularity;

        area = computeROIarea(roiS);
        perimeter = computePerimieter(roiS);

        circularity = 4d * Math.PI * area / (perimeter * perimeter);

        return circularity;
    }    
    
    /**
     * Computes the circularity of the {@link PolygonRoi} using the following
     * equation:
     *
     * 4*pi*area/(perimeter^2)
     *
     * @param roi
     * @return the circularity of the {@link PolygonRoi}
     */
    public static double computeCircularity(PolygonRoi roi) {

        double area, perimeter, circularity;

        area = computeROIarea(roi);
        perimeter = computePerimieter(roi);

        circularity = 4d * Math.PI * area / (perimeter * perimeter);

        return circularity;
    }

    /**
     * Returns the values of the pixels contained in the roi as an array of
     * doubles.
     *
     * @param proi
     * @param ip
     * @return
     */
    public static double[] getPixelsAsDoubles(PolygonRoi proi, ImageProcessor ip) {

        Point[] points = proi.getContainedPoints();
        int nPoints = points.length;

        double[] pixels = new double[nPoints];
        for (int i = 0; i < nPoints; i++) {
            // pixel value at every location inside of the roi. 
            pixels[i] = ip.getPixelValue(points[i].x, points[i].y);
        }
        return pixels;
    }

}
