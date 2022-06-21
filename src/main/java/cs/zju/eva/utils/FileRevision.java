package cs.zju.eva.utils;

import cs.zju.utils.Pair;

import java.util.HashMap;
import java.util.Map;


public class FileRevision extends Pair<String, String> {

    public FileRevision(String commitId, String filePath){
        super(commitId, filePath);
    }
}
