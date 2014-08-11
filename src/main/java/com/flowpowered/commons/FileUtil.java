/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class FileUtil {
    private static final HashMap<String, String> fileNameCache = new HashMap<>();

    /**
     * Computes a long CRC of a File
     *
     * @param file the file to process
     * @param a buffer for temporary data
     * @return the CRC or 0 on failure
     */
    public static long getCRC(File file, byte[] buffer) {

        FileInputStream in = null;

        try {
            in = new FileInputStream(file);
            return getCRC(in, buffer);
        } catch (FileNotFoundException e) {
            return 0;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Computes a long CRC of the file stored at a URL
     *
     * @param url the URL that the file is stored at
     * @param a buffer for temporary data
     * @return the CRC or 0 on failure
     */
    public static long getCRC(URL url, byte[] buffer) {

        InputStream in = null;

        try {
            URLConnection urlConnection = url.openConnection();

            in = urlConnection.getInputStream();
            return getCRC(in, buffer);
        } catch (IOException e) {
            return 0;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Computes a long CRC of the data in an InputStream until the stream ends.
     *
     * @param in the InputStream to process
     * @param a buffer for temporary data
     * @return the CRC or 0 on failure
     */
    public static long getCRC(InputStream in, byte[] buffer) {
        if (in == null) {
            return 0;
        }

        long hash = 1;

        int read = 0;
        int i;
        while (read >= 0) {
            try {
                read = in.read(buffer);
                for (i = 0; i < read; i++) {
                    hash += (hash << 5) + buffer[i];
                }
            } catch (IOException ioe) {
                return 0;
            }
        }

        return hash;
    }

    /**
     * Converts the String representation of a URL into its corresponding filename.
     *
     * @param the url to process
     * @return true the coresponding filename
     */
    public static String getFileName(String url) {
        if (fileNameCache.containsKey(url)) {
            return fileNameCache.get(url);
        }
        int end = url.lastIndexOf('?');
        int lastDot = url.lastIndexOf('.');
        int slash = url.lastIndexOf('/');
        int forwardSlash = url.lastIndexOf('\\');
        slash = slash > forwardSlash ? slash : forwardSlash;
        end = end == -1 || lastDot > end ? url.length() : end;
        String result = url.substring(slash + 1, end).replaceAll("%20", " ");
        fileNameCache.put(url, result);
        return result;
    }

    /**
     * Writes a Collection of Strings to a File, overwriting any previous file contents.
     *
     * Each String is converted into a line in the File.
     *
     * @param strings the Collection of Strings
     * @param file the file to write
     * @return true on success
     */
    public static boolean stringToFile(Collection<String> strings, File file) {
        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(file));
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (IOException ioe) {
            return false;
        }

        try {
            for (String line : strings) {
                bw.write(line);
                bw.newLine();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        } finally {
            try {
                bw.close();
            } catch (IOException ioe2) {
            }
        }
    }

    /**
     * Reads a File and places the contents into a collection of Strings.
     *
     * Each line in the File is converted into a String.
     *
     * Iterators on the List will iterate through the Strings in the order the lines appear in the file
     *
     * @param the file to read
     * @return the Collection of Strings or null on failure
     */
    public static Collection<String> fileToString(File file) {

        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException fnfe) {
            return null;
        }

        String line;

        try {
            Collection<String> strings = new LinkedList<>();
            while ((line = br.readLine()) != null) {
                strings.add(line);
            }
            return strings;
        } catch (IOException | NumberFormatException ioe) {
            return null;
        } finally {
            try {
                br.close();
            } catch (IOException ioe) {
            }
        }
    }

    private static final Collection<String> emptyCollection = new ArrayList<>();

    /**
     * Creates a blank file
     *
     * @param the file to create
     * @return true on success
     */
    public static boolean createFile(File file) {
        if (file == null) {
            return false;
        }
        File dir = file.getParentFile();
        if (dir != null) {
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return false;
                }
            } else {
                if (!dir.isDirectory()) {
                    return false;
                }
            }
        }
        return FileUtil.stringToFile(emptyCollection, file);
    }

    /**
     * Copies one file to another location
     *
     * @param inFile the source filename
     * @param outFile the target filename
     * @return true on success
     */
    @SuppressWarnings ("resource")
    public static boolean copy(File inFile, File outFile) {
        if (!inFile.exists()) {
            return false;
        }

        FileChannel in = null;
        FileChannel out = null;

        try {
            in = new FileInputStream(inFile).getChannel();
            out = new FileOutputStream(outFile).getChannel();

            long pos = 0;
            long size = in.size();

            while (pos < size) {
                pos += in.transferTo(pos, 10 * 1024 * 1024, out);
            }
        } catch (IOException ioe) {
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                return false;
            }
        }

        return true;
    }
}
