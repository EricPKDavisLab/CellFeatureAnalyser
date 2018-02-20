/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import ij.IJ;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A class of static methods commonly used IO operations such as getting the
 * file names of a set type of files from a specified file location.
 *
 * @author mqbssep5
 */
public class ImageIOutils {

    /**
     * Returns the list of file names of any files in the specified file
     * location with the specified file extension.
     *
     * @param directoryPath
     * @param fileExt
     * @return an array of the files names ending with the specified file
     * extension. An array of length zero will be returned if no files are found
     * with the specified file location.
     */
    public static String[] getFilesNames(String directoryPath, String fileExt) {

        if (directoryPath == null) {
            IJ.error("Null or invalid file path. ");
            return new String[0];
        }

        File ff = new File(directoryPath);// .listFiles().length

        File[] fl = ff.listFiles();
        // sort the files so that we get a consistent order. 
        Arrays.sort(fl);
        int nfiles = fl.length;

        String s;
        ArrayList<String> as = new ArrayList<String>();
        for (int i = 0; i < nfiles; i++) {
            s = fl[i].getName();
            if (s.endsWith(fileExt)) {
                as.add(s);
            }
        }

        return (String[]) as.toArray(new String[as.size()]);
    }

    /**
     * Returns the list of file names of any files in the specified file
     * location with the specified file extensions. e.g.
     * String[]{".tif",".tiff"}.
     *
     * @param directoryPath
     * @param fileExt an array of file extensions String[]{".tif",".tiff"}.
     * @return an array of the files names ending with the specified file
     * extension. An array of length zero will be returned if no files are found
     * with the specified file location.
     */
    public static String[] getFilesNames(String directoryPath, String[] fileExt) {

        if (directoryPath == null) {
            IJ.error("Null or invalid file path. ");
            return new String[0];
        }

        File ff = new File(directoryPath);// .listFiles().length

        if (!ff.exists()) {
            IJ.error("Null or invalid file path. ");
            return new String[0];
        }

        File[] fl = ff.listFiles();
        // sort the files so that we get a consistent order. 
        Arrays.sort(fl);
        int nfiles = fl.length;

        String s, currExt;
        ArrayList<String> as = new ArrayList<String>();
        for (int j = 0; j < fileExt.length; j++) {
            currExt = fileExt[j];
            for (int i = 0; i < nfiles; i++) {
                s = fl[i].getName();
                if (s.endsWith(currExt)) {
                    as.add(s);
                }
            }
        }

        return (String[]) as.toArray(new String[as.size()]);
    }

    /**
     * Returns the list of file names of any files in the specified file
     * location with the specified file extensions. e.g.
     * String[]{".tif",".tiff"}.
     *
     * @param directoryPath
     * @param fileExt an array of file extensions String[]{".tif",".tiff"}.
     * @return an array of the files names ending with the specified file
     * extension. An array of length zero will be returned if no files are found
     * with the specified file location.
     */
    public static HashMap<String, String[]> getSpecifiedFileNamesWithinFolders(String directoryPath, String[] fileExt) {
  
        if (directoryPath == null) {
            IJ.error("Null or invalid file path. ");
            return new HashMap<String, String[]>();
        }

        File ff = new File(directoryPath);// .listFiles().length

        if (!ff.exists()) {
            IJ.error("Null or invalid file path. ");
            return new HashMap<String, String[]>();
        }
        
        HashMap<String, String[]> outputNames = new HashMap<>();
        // first check if the there the user has pointed to a folder containing the files with the define file extension. 
        String[] names = getFilesNames( directoryPath, fileExt );
        if( names.length > 0){
            outputNames.put(directoryPath, names);
        }

        File[] fl = ff.listFiles();
        // sort the files so that we get a consistent order. 
        Arrays.sort(fl);
        int nfiles = fl.length;
        String dirname;
        HashMap<String, String[]> fullFileList = new HashMap<String, String[]>();
        for (int i = 0; i < nfiles; i++) {
            // We are only interested in directories
            if(fl[i].isDirectory()){
                // current sub-directory
                dirname = fl[i].getPath() + "\\";
                // check this directory for files in this directory
                String[] localFiles = getFilesNames(dirname, fileExt);
                System.out.println(dirname + " contains " + localFiles.length);
                // check we have files
                if( localFiles.length > 0 ){

                }else{
                    continue;
                }

            }
        }

        return outputNames;
    }

}
