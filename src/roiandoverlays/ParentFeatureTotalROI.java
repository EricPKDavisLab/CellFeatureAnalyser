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
package roiandoverlays;

import featureobjects.Feature;
import featureobjects.ParentFeature;
import ij.ImagePlus;
import ij.gui.Roi;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Creates an Roi object for our {@link ParentFeature}s as well as their own
 * internal {@link Feature}s.
 *
 * @author mqbssep5
 */
public class ParentFeatureTotalROI extends Roi {

    private final ArrayList<ParentFeature> features;

    private Color col = Color.yellow;

    private double trans = 0.5d;
    
    private double boxWidth = 2d;

    /**
     * @param imp
     * @param features
     */
    public ParentFeatureTotalROI(ImagePlus imp, ArrayList<ParentFeature> features) {
        super(0, 0, imp);
        this.features = features;
    }

    public void setColorAndTransparency(Color col, double trans) {
        this.col = col;
        this.trans = trans;
    }

    @Override
    public final synchronized void drawOverlay(final Graphics g) {

        final int xcorner = ic.offScreenX(0);
        final int ycorner = ic.offScreenY(0);
        final double magnification = getMagnification();

        final Graphics2D g2d = (Graphics2D) g;
        // Save graphic device original settings
        final AffineTransform originalTransform = g2d.getTransform();
        final Composite originalComposite = g2d.getComposite();
        final Stroke originalStroke = g2d.getStroke();
        final Color originalColor = g2d.getColor();

        final int currentFrame = imp.getFrame() - 1;

        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float transparency = (float) trans;

        // for colouring

        for (ParentFeature pf : features) {
            g2d.setColor(col);
            // only draw the features in this frame. 
            if ((pf.getFrame() - 1) == currentFrame) {
                drawParentOutline(g2d, pf, "", xcorner, ycorner, magnification, transparency);
                drawFeatures(g2d, pf, "", xcorner, ycorner, magnification, transparency);
            }
        }

        // reset everything to how it was before.
        g2d.setTransform(originalTransform);
        g2d.setComposite(originalComposite);
        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);

    }

    protected void drawParentOutline(final Graphics2D g2d, ParentFeature feature, String name, final int xcorner, final int ycorner, final double magnification, final float transparency) {

        Roi roi = feature.getFeatureBoundsOriginalImage();

        float[] xPoints = roi.getFloatPolygon().xpoints;
        float[] yPoints = roi.getFloatPolygon().ypoints;

        int nPoints = roi.getFloatPolygon().npoints;

        double x0p, y0p, x1p, y1p;
        for (int i = 0; i < nPoints - 1; i++) {
            // Scale to image zoom
            x0p = (xPoints[i] - xcorner) * magnification + 0.5;
            y0p = (yPoints[i] - ycorner) * magnification + 0.5;
            x1p = (xPoints[i + 1] - xcorner) * magnification + 0.5;
            y1p = (yPoints[i + 1] - ycorner) * magnification + 0.5;
            //
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            g2d.draw(new Line2D.Double(x0p, y0p, x1p, y1p));
        }
        // join the first and last points 
        x0p = (xPoints[0] - xcorner) * magnification + 0.5;
        y0p = (yPoints[0] - ycorner) * magnification + 0.5;
        x1p = (xPoints[nPoints - 1] - xcorner) * magnification + 0.5;
        y1p = (yPoints[nPoints - 1] - ycorner) * magnification + 0.5;
        //
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2d.draw(new Line2D.Double(x0p, y0p, x1p, y1p));

//        // draw on the cell number 
//        float xc = (cell.getXCent() - (float)xcorner) * (float)magnification + 0.5f;
//        float yc = (cell.getYCent() - (float)ycorner) * (float)magnification + 0.5f;
//
//        // draw on the number 
//        g2d.drawString("" + name, xc, yc);
    }

    protected void drawFeatures(final Graphics2D g2d, ParentFeature feature, String name, final int xcorner, final int ycorner, final double magnification, final float transparency) {

        HashMap< String, ArrayList<Feature> > channelFeatures;
        Set<String> keySet;
        ArrayList<Feature> channelFeatureList;
        int x0, y0;
        x0 = feature.get_x0();
        y0 = feature.get_y0();
        // for each channel. 
        for (int i = 0; i < imp.getNChannels(); i++) {
            // all features for this channel
            channelFeatures = feature.getAllFeaturesForChannel(i);
            // String keys for all of the features
            keySet = channelFeatures.keySet();
            // all features in the set. 
            for ( String key : keySet ) {
                // all the features for this set.
                channelFeatureList = channelFeatures.get(key);
                for ( Feature f : channelFeatureList ) {
                    g2d.setColor(f.getColor());
                    drawFeatureOutline( g2d, f, x0, y0, "", xcorner, ycorner, magnification, transparency );
                    //drawSpot( g2d, f.getXpix(), f.getYpix(), xcorner, ycorner, magnification, transparency );
                }

            }

        }

    }

    protected void drawFeatureOutline( final Graphics2D g2d, Feature spot, int x0, int y0, String name, final int xcorner, final int ycorner, final double magnification, final float transparency ) {

        Roi roi = spot.getOutLine();

        float[] xPoints = roi.getFloatPolygon().xpoints;
        float[] yPoints = roi.getFloatPolygon().ypoints;

        int nPoints = roi.getFloatPolygon().npoints;

        double x0p, y0p, x1p, y1p;
        for (int i = 0; i < nPoints - 1; i++) {
            // Scale to image zoom
            x0p = (xPoints[i] - xcorner  + x0) * magnification + 0.5;
            y0p = (yPoints[i] - ycorner + y0) * magnification + 0.5;
            x1p = (xPoints[i + 1] - xcorner + x0) * magnification + 0.5;
            y1p = (yPoints[i + 1] - ycorner + y0) * magnification + 0.5;
            //
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            g2d.draw(new Line2D.Double(x0p, y0p, x1p, y1p));
        }
        // join the first and last points 
        x0p = (xPoints[0] - xcorner + x0) * magnification + 0.5;
        y0p = (yPoints[0] - ycorner + y0) * magnification + 0.5;
        x1p = (xPoints[nPoints - 1] - xcorner + x0) * magnification + 0.5;
        y1p = (yPoints[nPoints - 1] - ycorner + y0) * magnification + 0.5;
        //
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2d.draw(new Line2D.Double(x0p, y0p, x1p, y1p));

//        // draw on the cell number 
//        float xc = (cell.getXCent() - (float)xcorner) * (float)magnification + 0.5f;
//        float yc = (cell.getYCent() - (float)ycorner) * (float)magnification + 0.5f;
//
//        // draw on the number 
//        g2d.drawString("" + name, xc, yc);
    }
    
    /**
     * Draws the line
     *
     * @param g2d
     * @param x
     * @param y
     * @param xcorner
     * @param ycorner
     * @param magnification
     * @param transparency
     */
    protected void drawSpot( final Graphics2D g2d, double x, double y, final int xcorner, final int ycorner, final double magnification, final float transparency ) {
        // Find x & y in physical coordinates

        // In pixel units
        // Scale to image zoom
        final double x0s = (x - xcorner) * magnification + 0.5;
        final double y0s = (y - ycorner) * magnification + 0.5;
        // Round
        final int x0 = (int) Math.round(x0s);
        final int y0 = (int) Math.round(y0s);

        double apparentRadius = boxWidth * magnification;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        //g2d.drawOval( ( int ) Math.round( x0 - apparentRadius ), ( int ) Math.round( y0 - apparentRadius ), ( int ) Math.round( 2 * apparentRadius ), ( int ) Math.round( 2 * apparentRadius ) );
        g2d.draw(new Ellipse2D.Double(x0 - 0.5, y0 - 0.5, 1, 1));
        g2d.draw(new Rectangle2D.Double(x0 - apparentRadius, y0 - apparentRadius, 2 * apparentRadius, 2 * apparentRadius));
       
    }    

}
