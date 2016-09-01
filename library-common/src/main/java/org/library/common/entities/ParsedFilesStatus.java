package org.library.common.entities;

import java.util.concurrent.atomic.LongAdder;

public class ParsedFilesStatus {
    private final int count;
    private final LongAdder fileInfoUpdatedCount = new LongAdder();
    private final LongAdder xmlParsedCount = new LongAdder();
    private final LongAdder infoParsedCount = new LongAdder();
    private final LongAdder savedCount = new LongAdder();

    public ParsedFilesStatus(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public LongAdder getXmlParsedCount() {
        return xmlParsedCount;
    }

    public LongAdder getInfoParsedCount() {
        return infoParsedCount;
    }

    public LongAdder getSavedCount() {
        return savedCount;
    }

    public LongAdder getFileInfoUpdatedCount() {
        return fileInfoUpdatedCount;
    }

    @Override
    public String toString() {
        return "ParsedFilesStatus{" +
                "count=" + count +
                ", fileInfoUpdatedCount=" + fileInfoUpdatedCount +
                ", xmlParsedCount=" + xmlParsedCount +
                ", infoParsedCount=" + infoParsedCount +
                ", savedCount=" + savedCount +
                '}';
    }
}
