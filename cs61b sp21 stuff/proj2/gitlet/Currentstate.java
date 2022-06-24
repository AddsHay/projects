package gitlet;

import java.io.File;
import java.util.HashMap;
import java.io.Serializable;


/** for tracking current state of a commit */
public class Currentstate implements Serializable {

    public static final File CWD = new File(".");
    /** Main .gitlet folder. */
    private static File GITLET_DIR = Utils.join(CWD, ".gitlet");
    /** state folder.*/
    private static File statef = Utils.join(GITLET_DIR, "state_f");
    /** sha1 code for this commit.*/
    private String commitsha1;
    /** map of branches including the current branches.*/
    private HashMap<String, String> branches;
    /** files that won't be included in the next branch.*/
    private HashMap<String, String> rmfs;

    public HashMap<String, String> getRmfiles() {
        return rmfs;
    }
    /** file = file to remove */
    public void rmfileremover(String file) {
        rmfs.remove(file);
    }

    private String currentbranch;

    public String getcurbranch() {
        return currentbranch;
    }

    public void removebranch(String branchname) {
        branches.remove(branchname);
    }

    public void setCurrentbranch(String branchname) {
        currentbranch = branchname;
    }

    public void putbranch(String branchname) {
        branches.put(branchname, getCommitsha1());
    }

    public void putbranch(String branchname, String branchcontent) {
        branches.put(branchname, branchcontent);
    }

    public String getbranchof(String branchname) {
        return branches.get(branchname);
    }

    public HashMap<String, String> getbranches() {
        return branches;
    }

    Currentstate() {
        branches = new HashMap<>();
        rmfs = new HashMap<>();
    }

    public void savermfiles(String filename, String content) {
        rmfs.put(filename, content);
    }

    public void clearrmfiles() {
        rmfs.clear();
    }

    public static Currentstate fromFile() {
        File commitFile = Utils.join(statef, "state");
        return Utils.readObject(commitFile, Currentstate.class);
    }

    public void saveState() {
        Utils.writeObject(Utils.join(statef, "state"), this);
    }

    public String getCommitsha1() {
        return commitsha1;
    }

    public void setCommitsha1(String cs1) {
        commitsha1 = cs1;
    }
}
