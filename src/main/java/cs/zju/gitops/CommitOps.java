package cs.zju.gitops;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitOps {

    public static String getCommitId(RevCommit rc){
        return rc.getId().getName();
    }

    public static String getCommitMessage(RevCommit rc){
        return rc.getFullMessage();
    }

    public static PersonIdent getCommitterIdentity(RevCommit rc){
        return rc.getCommitterIdent();
    }

    public static String getCommitter(RevCommit rc){
        return rc.getCommitterIdent().getName();
    }

    public static String getCommitterEmail(RevCommit rc){
        return rc.getCommitterIdent().getEmailAddress();
    }

    public static int getCommitTime(RevCommit rc){
        return rc.getCommitTime();
    }

    public static int getParentCount(RevCommit rc){
        return rc.getParentCount();
    }

    public static RevCommit[] getParents(RevCommit rc){
        return rc.getParents();
    }

    public static boolean isMerge(RevCommit rc) {
        return rc.getParentCount() > 1;
    }

    public static boolean isChild(RevCommit child, RevCommit parent){
        if (child.getParentCount() > 0){
            for (RevCommit p: child.getParents()){
                String pId = getCommitId(p);
                String parentId = getCommitId(parent);
                if (pId.equals(parentId))
                    return true;
            }

        }
        return false;
    }

    public static List<String> getBugzillaIdFromMessage(String message){
        List<String> ids = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();
        Pattern p = Pattern.compile("(\\s|^)(\\d+)(\\W|\\r\\n|\\n)");
        Matcher matcher = p.matcher(message);
        while(matcher.find()){
            String numStr = matcher.group(2);
            String id = "";
            try {
                id = numStr;
            } catch (NumberFormatException e){
                // do nothing.
            }
            if (id.length() > 8)
                continue;
            if (addedIds.contains(id))
                continue;

            long idInt = Long.parseLong(id.trim());
            if (idInt <= 100)
                continue;

            if (idInt > 10000000)
                continue;

            if (!id.equals("")) {
                ids.add(id);
                addedIds.add(id);
            }
        }
        return ids;
    }


    public static List<String> getJIRAIdFromMessage(String commitMsg){
        List<String> ret = new ArrayList<>();

        String pattern = "[A-Z]+-\\d+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(commitMsg);

        while(m.find()){
            String issueID = m.group(0);
            if (issueID.startsWith("UTF"))
                continue;
            if (issueID.startsWith("AMQQ-"))
                issueID = issueID.replace("AMQQ-", "AMQ-");
            if (issueID.startsWith("ACTIVEMQ-"))
                issueID = issueID.replace("ACTIVEMQ-", "AMQ-");
            if (issueID.startsWith("MQ-"))
                issueID = issueID.replace("MQ-", "AMQ-");
            if (!ret.contains(issueID))
                ret.add(issueID);
        }
        return ret;
    }

    public static List<String> getParentCommitIds(RevCommit rc){
        List<String> parentIds = new ArrayList<>();
        RevCommit[] parents = rc.getParents();
        if (rc.getParentCount() == 0)
            return null;
        else{
            for(RevCommit commit: parents)
                parentIds.add(commit.getId().getName());
            return parentIds;
        }
    }
}
