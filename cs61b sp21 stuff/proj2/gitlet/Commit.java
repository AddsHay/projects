package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  @author AH
 */
public class Commit implements Serializable {

    public static final File CWD = new File(".");
    /* Main .gitlet folder. */
    private static File GITLET_DIR = Utils.join(CWD, ".gitlet");

    private static File commitf = Utils.join(GITLET_DIR, "commit_f");

    private Date date;

    public Date getDate() {
        return date;
    }

    /** commit message.*/
    private String message;

    public String getMessage() {
        return message;
    }

    private File commitfolder;

    public static Commit fromFile(String name) {
        File commitFile = Utils.join(commitf, name);
        return Utils.readObject(commitFile, Commit.class);
    }

    Commit(Date dat, String mess, File cmf) {
        files = new HashMap<>();
        commitfolder = cmf;
        this.date = dat;
        this.message = mess;
    }

    private String parent;

    public String getParent() {
        return parent;
    }

    public void setParent(String newparent) {
        parent = newparent;
    }

    private String subparent;

    /**
     * subparent commit of sha1
     */
    public void setsubparentsha1(String shacommit) {
        subparent = shacommit;
    }

    public void saveCommit() {
        Utils.writeObject(Utils.join(commitfolder, sha1()), this);
    }


    public String getSubparent() {
        return subparent;
    }

    private HashMap<String, String> files;

    public HashMap<String, String> getFiles() {
        return files;
    }

    public void removefile(String file) {
        files.remove(file);
    }

    public void addfiles(String filename, String contents) {
        files.put(filename, contents);
    }

    public String sha1() {
        return Utils.sha1(date.toString(), message, parent, files.toString());
    }

    public Commit newdescendant(Date d, String m) {
        Commit descendant = new Commit(d, m, commitf);
        for (String filename: files.keySet()) {
            descendant.addfiles(filename, files.get(filename));
        }
        return descendant;
    }

    /** ancestors of the commit*/
    private HashMap<String, Integer> ancestor;

    /* ancestors of the commit with a certain id */
    private ArrayList<String> ancestorwid;

    public void createancestor() {
        ancestor = new HashMap<>();
        ancestorwid = new ArrayList<>();
        ancestor.put(sha1(), 0);
        ancestorhelper(parent, subparent, 1);
    }

    private void ancestorhelper(String first, String second, Integer step) {
        if (!first.equals("null")) {
            if (ancestor.containsKey(first)) {
                if (ancestor.get(first) > step) {
                    ancestor.put(first, step);
                }
            } else {
                ancestor.put(first, step);
            }
            Commit corrcommit = fromFile(first);
            String commitparent = corrcommit.getParent();
            String commitsub = corrcommit.getSubparent();
            ancestorhelper(commitparent, commitsub, step + 1);
        }
        if (second != null) {
            if (ancestor.containsKey(second)) {
                if (ancestor.get(second) > step) {
                    ancestor.put(second, step);
                }
            } else {
                ancestor.put(second, step);
            }
            Commit secondcommit = fromFile(second);
            String commitparent = secondcommit.getParent();
            String commitsub = secondcommit.getSubparent();
            ancestorhelper(commitparent, commitsub, step + 1);
        }
    }

    public void createancestorwid(String givenbranchid) {
        ancestorwid.add(givenbranchid);
        Commit branchidcommit = fromFile(givenbranchid);
        String commitparent = branchidcommit.getParent();
        String commitsub = branchidcommit.getSubparent();
        ancestorwidhelper(commitparent, commitsub);
    }

    public void ancestorwidhelper(String first, String second) {
        if (first != null) {
            if (!ancestorwid.contains(first)) {
                ancestorwid.add(first);
            }
            Commit corrcommit = fromFile(first);
            String commitparent = corrcommit.getParent();
            String commitsub = corrcommit.getSubparent();
            ancestorwidhelper(commitsub, commitsub);
        }
        if (second != null) {
            if (!ancestorwid.contains(second)) {
                ancestorwid.add(second);
            }
            Commit secondcommit = fromFile(second);
            String commitparent = secondcommit.getParent();
            String commitsub = secondcommit.getSubparent();
            ancestorwidhelper(commitparent, commitsub);
        }
    }

    public String findcommonancestor() {
        String common = ancestorwid.get(0);
        Integer num = 1000000;
        for (String index: ancestorwid) {
            Integer temp = ancestor.get(index);
            if (temp != null && temp < num) {
                num = temp;
                common = index;
            }
        }
        return common;
    }

    public void clearmergeset() {
        ancestor = null;
        ancestorwid = null;
    }

    public String findclosestancestor(String givenbranchid, boolean verify) {
        createancestor();
        createancestorwid(givenbranchid);
        String closest = findcommonancestor();
        if (verify) {
            System.out.println(ancestor.toString());
            System.out.println(ancestorwid.toString());
            System.out.println(closest);
        }
        clearmergeset();
        return closest;
    }
}
