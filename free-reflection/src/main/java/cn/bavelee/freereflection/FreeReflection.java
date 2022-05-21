package cn.bavelee.freereflection;

import cn.bavelee.freereflection.hook.ReflectionTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.AccessibleObject;
import java.util.*;

public class FreeReflection {

    private static final String LOG_ENABLED_KEY = "log";
    private static final String PREFIX_CLASS_NAME_KEY = "prefix";
    private static final String FULL_CLASS_NAME_KEY = "name";
    private static final String FULL_DUMP_CLASS = "dump";

    public void run(String args, Instrumentation inst) {
        Map<String, String> map = parseArgs(args);
        String logEnabledVal = map.getOrDefault(LOG_ENABLED_KEY, "true");
        boolean logEnabled = Boolean.parseBoolean(logEnabledVal);
        boolean dumpClass = "true".equals(map.getOrDefault(FULL_DUMP_CLASS, "false"));
        Log.setEnabled(logEnabled);
        Log.info("Agent is starting(" + args + ")...");
        Set<String> prefixClassNames = new HashSet<>();
        Set<String> fullClassNames = new HashSet<>();

        parse(prefixClassNames, map.getOrDefault(PREFIX_CLASS_NAME_KEY, null));
        parse(fullClassNames, map.getOrDefault(FULL_CLASS_NAME_KEY, null));
        ReflectionTransformer transformer = new ReflectionTransformer(inst, prefixClassNames, fullClassNames, dumpClass);
        inst.addTransformer(transformer, true);
        try {
            Log.info("Call re-transformClasses(AccessibleObject)");
            inst.retransformClasses(AccessibleObject.class);
        } catch (Throwable e) {
            Log.error("failed to call re-transformClasses : " + AccessibleObject.class.getName());
            e.printStackTrace();
            transformer.unload();
        }
    }

    private Map<String, String> parseArgs(String args) {
        if (args == null) {
            return Collections.emptyMap();
        }
        String[] split = args.split(";");
        Map<String, String> kvMap = new HashMap<>();
        for (String val : split) {
            int index = val.indexOf("=");
            if (index > 0) {
                kvMap.put(val.substring(0, index), val.substring(index + 1));
            }
        }
        return kvMap;
    }

    private void parse(Set<String> result, String val) {
        if (val != null) {
            String[] split = val.split(",");
            if (split.length > 0) {
                result.addAll(Arrays.asList(split));
            }
        }
    }
}
