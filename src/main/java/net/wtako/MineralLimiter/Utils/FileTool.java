package net.wtako.MineralLimiter.Utils;

import java.io.File;

public class FileTool {

    public static File getChildFile(File parent, String child, boolean ignoreCase) {
        try {
            if (ignoreCase) {
                for (final File subFile: parent.listFiles()) {
                    if (subFile.getName().equalsIgnoreCase(child)) {
                        return subFile;
                    }
                }
            } else {
                for (final File subFile: parent.listFiles()) {
                    if (subFile.getName().equals(child)) {
                        return subFile;
                    }
                }
            }
            return null;
        } catch (final Exception e) {
            return null;
        }
    }

}
