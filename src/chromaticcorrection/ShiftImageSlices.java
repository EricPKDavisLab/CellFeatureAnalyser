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
package chromaticcorrection;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 *
 * This class performs the shifting of the image channels in the X-Y direction
 * to correct for a chromatic shift between image channels.
 *
 * Currently the images are translated in the x-y direction to the closest
 * pixel.
 *
 * This avoids having to deal with artefacts caused by interpolation of sub,
 * pixel shifted data.
 *
 * @author mqbssep5
 */
public class ShiftImageSlices {

    public ShiftImageSlices() {

    }

    /**
     *
     * @param imp image to be shifted.
     * @param xShifts the amount by which each channel should be shifted with
     * respect to the first channel. The array should therefore contain
     * nchannels - 1 elements.
     * @param yShifts the amount by which each channel should be shifted with
     * respect to the first channel. The array should therefore contain
     * nchannels - 1 elements.
     * @param isPhysicalUnits if the units of the shift have been provided as a
     * physical distance then this should be set to true.
     */
    public void process( ImagePlus imp, double[] xShifts, double[] yShifts, boolean isPhysicalUnits ) {

        // convert to a pixel level shift. 
        if (isPhysicalUnits) {
            double pixelWidth = imp.getCalibration().pixelWidth;
            int n = xShifts.length;
            for (int i = 0; i < n; i++) {
                xShifts[i] = xShifts[i] / pixelWidth;
                yShifts[i] = yShifts[i] / pixelWidth;
            }
        }
        // just double check the output is correct. 
        for (int i = 0; i < xShifts.length; i++) {
            //System.out.println("Shift pix " + xShifts[i] + " y shifts " + yShifts[i]);
            xShifts[i] = Math.round(xShifts[i]);
            yShifts[i] = Math.round(yShifts[i]);
        }

        int nFrames = imp.getNFrames();
        int nChannels = imp.getNChannels();
        int nSlices = imp.getNSlices(); 
        int sliceIndex;
        ImageProcessor ip;
        // Now shift the images for each channel in all frames. 
        for (int f = 0; f < nFrames; f++) {
            //
            for (int c = 1; c < (nChannels); c++) {// remember we dont want to shift the data in the first channel. 
                //
                for( int z = 0; z < nSlices; z++ ){
                    sliceIndex = imp.getStackIndex(c+1, z+1, f+1); // indexing of dimensions begins with 1.
                    ip = imp.getStack().getProcessor(sliceIndex);
                    ip.setInterpolationMethod(ImageProcessor.NONE);
                    ip.translate(xShifts[c-1], yShifts[c-1]);
                }

            }

        }

    }

}
