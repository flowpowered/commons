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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class PathUtil {
    private static final HashMap<String, String> fileNameCache = new HashMap<>();

    /**
     * Computes a long CRC of a Path
     *
     * @param file the file to process
     * @param buffer a buffer for temporary data
     * @return the CRC or 0 on failure
     */
    public static long getCRC(Path file, byte[] buffer) {
        try (InputStream in = Files.newInputStream(file)) {
            return getCRC(in, buffer);
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException ex) {
            return 0;
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
     * Writes a Collection of Strings to a Path, overwriting any previous file contents.
     *
     * Each String is converted into a line in the Path.
     *
     * @param strings the Collection of Strings
     * @param path the file to write
     * @return true on success
     */
    public static boolean stringToFile(Collection<String> strings, Path path) {

        try (BufferedWriter bw = Files.newBufferedWriter(path, Charset.defaultCharset())) {
            for (String line : strings) {
                bw.write(line);
                bw.newLine();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Reads a Path and places the contents into a collection of Strings.
     *
     * Each line in the Path is converted into a String.
     *
     * Iterators on the List will iterate through the Strings in the order the lines appear in the file
     *
     * @param the file to read
     * @return the Collection of Strings or null on failure
     */
    public static Collection<String> fileToString(Path path) {
        String line;

        try (BufferedReader br = Files.newBufferedReader(path, Charset.defaultCharset())) {
            Collection<String> strings = new LinkedList<>();
            while ((line = br.readLine()) != null) {
                strings.add(line);
            }
            return strings;
        } catch (IOException | NumberFormatException ioe) {
            return null;
        }
    }
}
