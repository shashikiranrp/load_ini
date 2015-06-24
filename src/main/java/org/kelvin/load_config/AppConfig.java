package org.kelvin.load_config;


import com.sun.istack.internal.Nullable;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:shasrp@yahoo-inc.com">Shashikiran</a>
 */
public class AppConfig
{
    private static final Logger sLogger = Logger.getLogger(AppConfig.class.getName());

    private final Map<String, Map<String, String>> innerMap;
    private AppConfig()
    {
        this.innerMap = new HashMap<String, Map<String, String>>();
    }

    public static AppConfig load(String filePath, boolean ignoreFaultLines, String... overrides)
            throws ConfigLoadException
    {
        if (null == filePath || filePath.isEmpty()) {
            throw new IllegalArgumentException("path is null or empty!");
        }

        /**
         * Override handling setup
         */
        final Map<String, Integer> overrideMap = new HashMap<>(overrides.length);
        final Map<String, Integer> keysWeightMap = new HashMap<>();
        int index = 0;
        for (String override : overrides) {
            overrideMap.put(override, index++);
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            AppConfig appConfig = new AppConfig();
            String nextLine = null;
            Map<String, String> currentGrpMap = null;
            String currentGrpName = null;
            int lineNo = 0;
            while (null != (nextLine = reader.readLine())) {
                ++lineNo;
                nextLine = nextLine.trim();
                if (nextLine.isEmpty()) {
                    continue;
                }

                switch (nextLine.charAt(0)) {
                    case ';':
                        //ignore the comment
                        break;
                    case '[':
                        //reset the currentGroupMap
                        int grpNameLen = nextLine.length();
                        if (grpNameLen < 2 || ']' != nextLine.charAt(grpNameLen - 1)) {
                            if (!ignoreFaultLines) {
                                throw new ConfigLoadException("malformed group specifier at line " + lineNo);
                            }
                            sLogger.warning("parse error: ignoring line " + lineNo);
                            continue;
                        }

                        currentGrpName = nextLine.substring(1, grpNameLen - 1);
                        currentGrpMap = appConfig.innerMap.get(currentGrpName);
                        // is it new group
                        if (null == currentGrpMap) {
                            currentGrpMap = new HashMap<>();
                            appConfig.innerMap.put(currentGrpName, currentGrpMap);
                        }
                        break;
                    default:
                        //handle settings
                        if (null == currentGrpMap || null == currentGrpMap) {
                            throw new ConfigLoadException("setting line encountered with out group at line " + lineNo);
                        }

                        String keyValue[] = nextLine.split("=");
                        if (2 != keyValue.length) {
                            if (!ignoreFaultLines) {
                                throw new ConfigLoadException("malformed key value at line " + lineNo);
                            }
                            sLogger.warning("parse error: ignoring line " + lineNo);
                            continue;
                        }
                        String key = keyValue[0].trim(), value = keyValue[1].trim();
                        String override = null;
                        int overrideStart = key.indexOf('<');
                        if (-1 != overrideStart) {
                            if ('>' != key.charAt(key.length() - 1)) {
                                if (!ignoreFaultLines) {
                                    throw new ConfigLoadException("malformed key value at line " + lineNo);
                                }
                                sLogger.warning("parse error: ignoring line " + lineNo);
                                continue;
                            }
                            override = key.substring(overrideStart + 1, key.length() - 1);
                            key = key.substring(0, overrideStart - 1);
                        }

                        if (null != override) {
                            if (overrideMap.keySet().contains(override)) {
                                final String overrideKey = currentGrpName.concat(".").concat(key);
                                final Integer currentOverrideRank = keysWeightMap.get(overrideKey);
                                if (currentOverrideRank > overrideMap.get(override)) {
                                    continue;
                                }
                            } else {
                                sLogger.warning("ignoring unknown override " + override + " at line " + lineNo);
                                continue;
                            }
                        }

                        value = value.charAt(0) == '"' ? value.substring(1) : value;
                        value = value.charAt(value.length() - 1) == '"' ? value.substring(0, value.length() - 1) : value;

                        currentGrpMap.put(key, value);
                        break;
                }
            }
            return appConfig;
        } catch (IOException e) {
            throw new ConfigLoadException("exception while reading: ".concat(filePath), e);
        }
    }


    public Map<String, String> getGroup(String key)
    {
        if (null == key || key.isEmpty()) {
            return null;
        }
        Map<String, String> grpMap = innerMap.get(key);
        return null == grpMap ? null : Collections.<String, String>unmodifiableMap(grpMap);

    }

    public String get(@Nullable String key)
    {
        if (null == key || key.isEmpty()) {
            return null;
        }

        String[] grpAndKey = key.split("\\.");
        Map<String, String> grpMap = getGroup(grpAndKey[0]);
        if (null == grpMap) {
            return null;
        } else {
            return grpMap.get(grpAndKey[1]);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{\n");
        for (Map.Entry<String, Map<String, String>> entry : innerMap.entrySet()) {
            builder.append("\t[").append(entry.getKey()).append("]: \n");
            for (Map.Entry<String, String> kv : entry.getValue().entrySet()) {
                builder.append("\t\t").append(kv.getKey()).append(" = ").append(kv.getValue()).append("\n");
            }
        }
        builder.append("}\n");
        return builder.toString();
    }
}
