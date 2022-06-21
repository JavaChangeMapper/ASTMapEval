package cs.zju.eva.matchfw.result;

import cs.zju.eva.utils.FileRevision;

import java.util.HashMap;
import java.util.Map;

public class StmtMatchGroup {
    private FileRevision fr;
    private int srcCharPos;
    private int dstCharPos;

    public static Map<FileRevision, Map<Integer, Map<Integer, StmtMatchGroup>>> allStmtMatchGroups;

    public StmtMatchGroup(ComparisonRecord record){
        this.fr = record.getFileRevision();
        this.srcCharPos = record.getSrcCharPos();
        this.dstCharPos = record.getDstCharPos();
    }

    public static void init(){
        allStmtMatchGroups = new HashMap<>();
    }

    public static StmtMatchGroup getStmtMatchGroup(ComparisonRecord record){
        FileRevision fr = record.getFileRevision();
        int srcCharPos = record.getSrcCharPos();
        int dstCharPos = record.getDstCharPos();
        if (!allStmtMatchGroups.containsKey(fr))
            allStmtMatchGroups.put(fr, new HashMap<>());
        if (!allStmtMatchGroups.get(fr).containsKey(srcCharPos))
            allStmtMatchGroups.get(fr).put(srcCharPos, new HashMap<>());
        if (!allStmtMatchGroups.get(fr).get(srcCharPos).containsKey(dstCharPos))
            allStmtMatchGroups.get(fr).get(srcCharPos).put(dstCharPos, new StmtMatchGroup(record));

        return allStmtMatchGroups.get(fr).get(srcCharPos).get(dstCharPos);
    }
}
