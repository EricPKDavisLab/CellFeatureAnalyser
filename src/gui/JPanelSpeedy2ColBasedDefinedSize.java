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
package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * An extension of the JPanel which already has a set {@link GridBagConstraints}
 * layout manager.
 *
 * It has a few methods used to easily add components to the JPanel either by
 * adding to either the left or the right hand side of the panel, both
 * simultaneously, or across the two columns.
 *
 * There is an internal counter for the number of rows being used in the panel
 * therefore many many of the methods just have to be called in sequence that
 * each row could be added.
 *
 * @author mqbssep5.
 */
public final class JPanelSpeedy2ColBasedDefinedSize extends JPanel {

    private final int dx = 10;
    
    private final int dy = 5;
    
    private final int compHeight = 20;
    
    private final Insets insets = new Insets(dy, dx, dy, dx);

    private GridBagConstraints c;

    private int row = 0;

    private final int width;

    private final int height;

    private Dimension dimComponentsHalfWidth;
    
    private Dimension dimComponentsFullWidth;

    /**
     * Constructor.
     *
     * @param width width of the JPanel
     * @param height height of the JPanel.
     */
    public JPanelSpeedy2ColBasedDefinedSize(int width, int height) {
        
        this.setLayout(new GridBagLayout());
        this.width = width;
        this.height = height;

        computeDims();

    }

    /**
     * Initialise the GridBagConstraints layout manager as well as the dimensions
     * used to set the size of the components when they are added. 
     */
    private void computeDims() {
        
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        
        // compute the dimensions of the preferred width for components spanning 
        // half way across the panel.
        int halfwidth = (int)Math.round( (double)(width - 4*dx)/2d );
        dimComponentsHalfWidth = new Dimension(halfwidth, compHeight);
        //
        int fullwidth = (width - 2*dx);
        dimComponentsFullWidth = new Dimension(fullwidth, compHeight);      
        // set the size of this component. 
        this.setPreferredSize(new Dimension(width, height));
    }

    /**
     * Resets the anchor for this panel. The default is
     * GridBagConstraints.NORTHWEST.
     *
     * @param anchor
     */
    public void setAnchor(int anchor) {
        c.anchor = anchor;
    }

    /**
     * Adds a components to the specified JPanel with defined grid constraints.
     *
     * @param comp component to be added.
     * @param row row this will be added to.
     * @param col column this will be added to.
     */
    private void addComponentSimple(Component comp, int row, int col) {
        c.gridx = col;
        c.gridy = row;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weightx = 1d;
        this.add(comp, c);
    }

    /**
     * Adds a single component to the right hand side of this double column
     * panel.
     *
     * @param comp
     * @param row specified row that these components will be added to.
     */
    public void addSingleComponentRHS(Component comp, int row) {
        comp.setPreferredSize(dimComponentsHalfWidth);
        addComponentSimple(comp, row, 1);
    }

    /**
     * Adds a single component to the right hand side of this double column
     * panel.
     *
     * @param comp
     */
    public void addSingleComponentRHS(Component comp) {
        comp.setPreferredSize(dimComponentsHalfWidth);
        addComponentSimple(comp, row, 1);
        row++;
    }

    /**
     * Adds a single component to the right hand side of this double column
     * panel.
     *
     * @param comp
     * @param row specified row that these components will be added to.
     */
    public void addSingleComponentLHS(Component comp, int row) {
        comp.setPreferredSize(dimComponentsHalfWidth);
        addComponentSimple(comp, row, 0);
    }

    /**
     * Adds a single component to the right hand side of this double column
     * panel.
     *
     * @param comp
     */
    public void addSingleComponentLHS(Component comp) {
        comp.setPreferredSize(dimComponentsHalfWidth);
        addComponentSimple(comp, row, 0);
        row++;
    }

    /**
     * Adds two {@link Component}s to this JPanel at the specified row.
     *
     * @param compLeft
     * @param compRight
     * @param row specified row that these components will be added to.
     */
    public void addTwoComponentsToRow(Component compLeft, Component compRight, int row) {
        addSingleComponentLHS(compLeft, row);
        addSingleComponentRHS(compRight, row);
    }

    /**
     *
     * Adds two {@link Component}s to this JPanel to the last row in the JPanel
     *
     * @param compLeft
     * @param compRight
     */
    public void addTwoComponentsToRow(Component compLeft, Component compRight) {
        addTwoComponentsToRow(compLeft, compRight, row);
        row++;
    }

    /**
     * Adds a component across two columns to the specified row.
     *
     * @param comp
     * @param row
     */
    public void addComponentDoubleColumn(Component comp, int row) {
        comp.setPreferredSize(dimComponentsFullWidth);
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.9;
        this.add(comp, c);
    }

    /**
     * Adds a {@link JSeparator} in horizontal orientation to this panel. 
     */
    public void addHorizontalJSeparator(){
        addComponentDoubleColumn(new JSeparator(JSeparator.HORIZONTAL), row);
        row++;
    }
    
    
    /**
     * Adds a component across two columns to panel.
     *
     * @param comp
     */
    public void addComponentDoubleColumn(Component comp) {
        comp.setPreferredSize(dimComponentsFullWidth);
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        this.add(comp, c);
        row++;
    }

}
