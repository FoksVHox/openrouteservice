/*
 *  Copyright 2011 Peter Karich info@jetsli.de
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.jetsli.graph.util;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Peter Karich, info@jetsli.de
 */
public class Helper {

    public static final int MB = 1 << 20;

    public static BufferedReader createBuffReader(File file) throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }

    public static BufferedReader createBuffReader(InputStream is) throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(is, "UTF-8"));
    }

    public static BufferedWriter createBuffWriter(File file) throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
    }

    public static List<String> readFile(String file) throws IOException {
        return readFile(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }

    public static List<String> readFile(Reader simpleReader) throws IOException {
        BufferedReader reader = new BufferedReader(simpleReader);
        try {
            List<String> res = new ArrayList();
            String line = null;
            while ((line = reader.readLine()) != null) {
                res.add(line);
            }
            return res;
        } finally {
            reader.close();
        }
    }

    /**
     * @return a sorted list where the object with the highest integer value comes first!
     */
    public static <T> List<Entry<T, Integer>> sort(Collection<Entry<T, Integer>> entrySet) {
        List<Entry<T, Integer>> sorted = new ArrayList<Entry<T, Integer>>(entrySet);
        Collections.sort(sorted, new Comparator<Entry<T, Integer>>() {

            @Override
            public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
                int i1 = o1.getValue();
                int i2 = o2.getValue();
                if (i1 < i2)
                    return 1;
                else if (i1 > i2)
                    return -1;
                else
                    return 0;
            }
        });

        return sorted;
    }

    /**
     * @return a sorted list where the string with the highest integer value comes first!
     */
    public static <T> List<Entry<T, Long>> sortLong(Collection<Entry<T, Long>> entrySet) {
        List<Entry<T, Long>> sorted = new ArrayList<Entry<T, Long>>(entrySet);
        Collections.sort(sorted, new Comparator<Entry<T, Long>>() {

            @Override
            public int compare(Entry<T, Long> o1, Entry<T, Long> o2) {
                long i1 = o1.getValue();
                long i2 = o2.getValue();
                if (i1 < i2)
                    return 1;
                else if (i1 > i2)
                    return -1;
                else
                    return 0;
            }
        });

        return sorted;
    }

    public static void deleteDir(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteDir(f);
            }
        }

        file.delete();
    }

    public static void deleteFilesStartingWith(String string) {
        File specificFile = new File(string);
        File pFile = specificFile.getParentFile();
        if (pFile != null) {
            for (File f : pFile.listFiles()) {
                if (f.getName().startsWith(specificFile.getName()))
                    f.delete();
            }
        }
    }

    public static String getBeanMemInfo() {
        java.lang.management.OperatingSystemMXBean mxbean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean sunmxbean = (com.sun.management.OperatingSystemMXBean) mxbean;
        long freeMemory = sunmxbean.getFreePhysicalMemorySize();
        long availableMemory = sunmxbean.getTotalPhysicalMemorySize();
        return "free:" + freeMemory / MB + ", available:" + availableMemory / MB + ", rfree:" + Runtime.getRuntime().freeMemory() / MB;
    }

    public static String getMemInfo() {
        return "totalMB:" + Runtime.getRuntime().totalMemory() / MB
                + ", usedMB:" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB;
    }

    public static int sizeOfObjectRef(int factor) {
        // pointer to class, flags, lock
        return factor * (4 + 4 + 4);
    }

    public static int sizeOfLongArray(int length, int factor) {
        // pointer to class, flags, lock, size
        return factor * (4 + 4 + 4 + 4) + 8 * length;
    }

    public static int sizeOfObjectArray(int length, int factor) {
        // TODO add 4byte to make a multiple of 8 in some cases
        // TODO compressed oop
        return factor * (4 + 4 + 4 + 4) + 4 * length;
    }

    public static void cleanMappedByteBuffer(MappedByteBuffer mapping) {
        if (mapping == null)
            return;

        sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) mapping).cleaner();
        if (cleaner != null)
            cleaner.clean();
    }

    public static void close(XMLStreamReader r) {
        try {
            if (r != null)
                r.close();
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Couldn't close xml reader", ex);
        }
    }

    public static void close(Closeable cl) {
        try {
            if (cl != null)
                cl.close();
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't close resource", ex);
        }
    }

    public static CmdArgs readCmdArgs(String[] args) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (String arg : args) {
            String strs[] = arg.split("\\=");
            if (strs.length != 2)
                continue;

            String key = strs[0];
            if (key.startsWith("-")) {
                key = key.substring(1);
            }
            if (key.startsWith("-")) {
                key = key.substring(1);
            }
            String value = strs[1];
            map.put(key, value);
        }

        return new CmdArgs(map);
    }

    public static class CmdArgs {

        private final Map<String, String> map;

        private CmdArgs(Map<String, String> map) {
            this.map = map;
        }

        public long getLong(String key, long _default) {
            String str = map.get(key);
            if (!Helper.isEmpty(str)) {
                try {
                    return Long.parseLong(str);
                } catch (Exception ex) {
                }
            }
            return _default;
        }

        public boolean getBool(String key, boolean _default) {
            String str = map.get(key);
            if (!Helper.isEmpty(str)) {
                try {
                    return Boolean.parseBoolean(str);
                } catch (Exception ex) {
                }
            }
            return _default;
        }

        public double getDouble(String key, double _default) {
            String str = map.get(key);
            if (!Helper.isEmpty(str)) {
                try {
                    return Double.parseDouble(str);
                } catch (Exception ex) {
                }
            }
            return _default;
        }

        public String get(String key, String _default) {
            String str = map.get(key);
            if (Helper.isEmpty(str))
                return _default;
            return str;
        }
    }

    public static boolean isEmpty(String strOsm) {
        return strOsm == null || strOsm.trim().isEmpty();
    }
}
