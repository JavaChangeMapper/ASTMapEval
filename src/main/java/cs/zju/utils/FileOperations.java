package cs.zju.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOperations {
    public static String getFileContent(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists())
            throw new RuntimeException("Read File Error: cannot find the file: " + filePath);
        return FileUtils.readFileToString(f, Charset.forName("utf-8"));
    }

    public static List<String> listFilesInDir(String dirPath){
        File dir = new File(dirPath);
        if (!dir.isDirectory())
            throw new RuntimeException("Not a directory");
        File[] fileList = dir.listFiles();
        List<String> paths = new ArrayList<>();
        if (fileList == null)
            return paths;
        for (File f: fileList){
            if (f.isFile()){
                paths.add(f.getAbsolutePath());
            }
        }
        return paths;
    }

    public static String getPackageFromBufferReader(BufferedReader br) throws IOException{
        String nextLine;
        String packageName = null;
        while ((nextLine = br.readLine()) != null) {
            if (nextLine.startsWith("package ")) {
                int sepIndex = nextLine.indexOf(";");
                if (sepIndex > 0) {
                    packageName = nextLine.substring(8, sepIndex);
                } else {
                    packageName = nextLine.substring(8);
                    if (packageName.endsWith("."))
                        packageName = packageName.substring(0, packageName.length()-1);
                }
                break;
            }
        }
        br.close();
        return packageName;
    }

    public static String getPackageOfFileReadFromDisk(String filePath) throws IOException{
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            return getPackageFromBufferReader(br);
        }
    }

    public static void serializeObjToFile(Serializable obj, String filePath) throws Exception{
        SerializationUtils.serialize(obj, new FileOutputStream(filePath));
    }

    public static Serializable deserializeObj(String filePath) throws Exception{
        return SerializationUtils.deserialize(new FileInputStream(filePath));
    }

    // ????????????????????????
//    public static String getLineContent(String fileContent, int lineNo){
//        Set<Integer> input = new HashSet<>();
//        input.add(lineNo);
//        Pair<Integer, Integer> range = getRangeOfLines(fileContent, input).get(lineNo);
//        if (range == null)
//            return "";
//        return fileContent.substring(range.first, range.second);
//    }

    // ?????????????????????file content???????????????????????????range
    // ????????? lineIndex ???1 ??????
//    public static Map<Integer, Pair<Integer, Integer>> getRangeOfLines(String fileContent, Set<Integer> lines){
//        Map<Integer, Pair<Integer, Integer>> retMap = new HashMap<>();
//        if (lines.size() == 0)
//            return retMap;
//        char[] contentArray = fileContent.toCharArray();
//        int lineCounter = 0;
//        int curLineStartPosition = 0;
//        boolean lineEnd = false;
//        for (int pos = 0; pos < contentArray.length; pos++){
//            char c = contentArray[pos];
//
//            // ????????????????????????character?????????
//            if (lineEnd){
//                // ?????????????????????????????????
//                lineEnd = false;
//                curLineStartPosition = pos;
//                // lineCounter ??????
//                lineCounter ++;
//            }
//
//            // ?????????????????????
//            if (c == '\n')
//                lineEnd = true;
//
//            int curLineIndex = lineCounter + 1;
//
//            if (lineEnd){
//                // ????????????????????????
//                if (lines.contains(curLineIndex)) {
//                    // ???????????????
//                    String lineContent = fileContent.substring(curLineStartPosition, pos + 1);
//                    String trimContent = lineContent.trim();
//                    int realStartPosOfLine = getStartPosOfLine(trimContent);
//                    if (trimContent.length() == 0){
//                        retMap.put(curLineIndex, null);
//                        continue;
//                    }
//                    char startNonEmptyChar = trimContent.charAt(0);
//                    char endNonEmptyChar = trimContent.charAt(trimContent.length() - 1);
//                    int startPos = curLineStartPosition + lineContent.indexOf(startNonEmptyChar);
//                    int endPos = curLineStartPosition + lineContent.lastIndexOf(endNonEmptyChar) + 1;
//                    retMap.put(curLineIndex, new Pair<>(startPos + realStartPosOfLine, endPos));
//                }
//            }
//
//            // ????????????????????????
//            if (retMap.size() == lines.size())
//                break;
//        }
//        return retMap;
//    }

    // ??????"} else"?????????
//    private static int getStartPosOfLine(String lineContent){
//        String filteredChars = "} \t";
//        for (int i = 0; i < lineContent.length(); i++){
//            String charAtI = lineContent.substring(i, i+1);
//            if (!filteredChars.contains(charAtI))
//                return i;
//        }
//        return -1;
//    }

//    public static String removeComments(String content) {
//        Pattern commentsPattern = Pattern.compile("(//.*?$)|(/\\*.*?\\*/)", Pattern.MULTILINE | Pattern.DOTALL);
//        Pattern stringsPattern = Pattern.compile("(\".*?(?<!\\\\)\")");
//        List<Match> commentMatches = new ArrayList<Match>();
//        Matcher commentsMatcher = commentsPattern.matcher(content);
//        while (commentsMatcher.find()) {
//            Match match = new Match();
//            match.start = commentsMatcher.start();
//            match.text = commentsMatcher.group();
//            commentMatches.add(match);
//        }
//
//        List<Match> commentsToRemove = new ArrayList<Match>();
//
//        Matcher stringsMatcher = stringsPattern.matcher(content);
//        while (stringsMatcher.find()) {
//            for (Match comment : commentMatches) {
//                if (comment.start > stringsMatcher.start() && comment.start < stringsMatcher.end())
//                    commentsToRemove.add(comment);
//            }
//        }
//        for (Match comment : commentsToRemove)
//            commentMatches.remove(comment);
//
//        for (Match comment : commentMatches)
//            content = content.replace(comment.text, "");
//
//        return content;
//    }

//    private static class Match {
//        int start;
//        String text;
//    }
}
