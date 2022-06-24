package gitlet;

import java.io.File;
import java.util.Date;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Serializable;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author AH
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String... args) {
        if (args.length == 0) {
            exitfunc("Please enter a command.");
        }
        commandinupterrors(args);
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                initfunc();
                break;
            case "add":
                addfunc(args);
                break;
            case "commit":
                Date date = new Date();
                commitfunc(args, date, false, null, null);
                break;
            case "rm":
                rmfunc(args);
                break;
            case "log":
                logfunc(args);
                break;
            case "global-log":
                globallogfunc(args);
                break;
            case "find":
                findfunc(args);
                break;
            case "status":
                statusfunc(args);
                break;
            case "checkout":
                checkoutfunc(args);
                break;
            case "branch":
                branchfunc(args);
                break;
            case "rm-branch":
                rmbranchfunc(args);
                break;
            case "reset":
                resetfunc(args, true);
                break;
            case "merge":
                mergefunc(args);
                break;
            default:
                exitfunc("No command with that name exists.");
        }
        System.exit(0);
    }
    public static void commandinupterrors(String[] args) {
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (args.length != 1) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "add":
                if (args.length != 2) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "commit":
                if (args.length != 2) {
                exitfunc("Incorrect operands.");
                }
                if (args[1].equals("")) {
                exitfunc("Please enter a commit message.");
                }
                break;
            case "rm":
                if (args.length != 2) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "log":
                if (args.length != 1) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "global-log":
                if (args.length != 1) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "find":
                if (args.length != 2) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "status":
                if (args.length != 1) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "checkout":
                if (args.length > 4 || args.length < 2) {
                    exitfunc("Incorrect operands.");
                } else if (args.length == 4 && !args[2].equals("--")) {
                    exitfunc("Incorrect operands.");
                } else if (args.length == 3 && !args[1].equals("--")) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "branch":
                if (args.length != 2) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "rm-branch":
                if (args.length != 2) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "reset":
                if (args.length != 2) {
                    exitfunc("Incorrect operands.");
                }
                break;
            case "merge":
                if (args.length != 2) {
                    exitfunc("Incorrect operands.");
                }
                break;
            default:
                exitfunc("No command with that name exists.");
        }
    }

    public static void exitfunc(String message) {
        if (!message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    /** Stage file folder. */
    private static File stagef = Utils.join(GITLET_DIR, "stage_f");
    /** Commit class folder. */
    private static File commitf = Utils.join(GITLET_DIR, "commit_f");
    /** Current State file folder. */
    private static File statef = Utils.join(GITLET_DIR, "state_f");
    /** Blob Content folder. */
    private static File blobf = Utils.join(GITLET_DIR, "blob_f");
    /** Remove files folder. */
    private static File rmf = Utils.join(GITLET_DIR, "rm_f");

    public static void initfunc() {
        if (GITLET_DIR.exists()) {
            exitfunc("A Gitlet version-control system already exists in the current directory.");
        } else {
            GITLET_DIR.mkdirs();
            stagef.mkdirs();
            commitf.mkdirs();
            statef.mkdirs();
            blobf.mkdirs();
            rmf.mkdirs();
            Date startdate = new Date(0);
            String startmessage = "initial commit";
            String[] start = {"commit", startmessage};
            Commit firstcm = commitfunc(start, startdate, true, null, null);
            Currentstate state = new Currentstate();
            firstcm.setParent("null");
            firstcm.saveCommit();
            state.setCommitsha1(firstcm.sha1());
            state.setCurrentbranch("master");
            state.putbranch("master");
            state.saveState();
        }
    }

    public static void addfunc(String[] args) {
        boolean reasontoadd = true;
        Currentstate curstate = Currentstate.fromFile();
        String headsha1 = curstate.getCommitsha1();
        File stageFile = Utils.join(stagef, args[1]);
        File original = Utils.join(CWD, args[1]);
        if (!original.exists()) {
            exitfunc("File does not exist.");
        }
        String content = Utils.readContentsAsString(original);
        if (!headsha1.equals("null")) {
            Commit headcommit = Commit.fromFile(headsha1);
            if (headcommit.getFiles().containsKey(args[1])) {
                File headfile = Utils.join(blobf, headcommit.getFiles().get(args[1]));
                String oldcontent = Utils.readContentsAsString(headfile);
                if (oldcontent.equals(content)) {
                    reasontoadd = false;
                    if (stageFile.exists()) {
                        String stagecontent = Utils.readContentsAsString(stageFile);
                        if (stagecontent.equals(content)) {
                            stageFile.delete();
                        }
                    }
                }
            }
        }
        if (curstate.getRmfiles().containsKey(args[1])) {
            File thatrmfile = Utils.join(rmf, args[1]);
            String rmcontent = Utils.readContentsAsString(thatrmfile);
            if (rmcontent.equals(content)) {
                curstate.rmfileremover(args[1]);
                thatrmfile.delete();
                curstate.saveState();
            }
        }
        if (reasontoadd) {
            Utils.writeContents(stageFile, content);
        }
        curstate.saveState();
    }

    public static Commit commitfunc(String[] args, Date date, boolean first, String branchid, String commitsha1) {
        File stateFile = Utils.join(statef, "state");
        if (first) {
            return new Commit(date, args[1], commitf);
        }
        Currentstate oldstate = Utils.readObject(stateFile, Currentstate.class);
        Commit old = Commit.fromFile(oldstate.getCommitsha1());
        Commit childcommit = old.newdescendant(date, args[1]);
        List<String> stagefiles = Utils.plainFilenamesIn(stagef);
        childcommit.setParent(old.sha1());
        if (oldstate.getRmfiles().isEmpty() && stagefiles.size() == 0) {
            exitfunc("No changes added to the commit.");
        }
        for (String file: oldstate.getRmfiles().keySet()) {
            childcommit.removefile(file);
            File rmFile = Utils.join(rmf, file);
            rmFile.delete();
        }
        for (String name: stagefiles) {
            File temp = Utils.join(stagef, name);
            String tempcontent = Utils.readContentsAsString(temp);
            String blobsha1 = Utils.sha1(name, tempcontent);
            File blobfile = Utils.join(blobf, blobsha1);
            Utils.writeContents(blobfile, tempcontent);
            childcommit.addfiles(name, blobsha1);
            temp.delete();
        }
        if (branchid == null || commitsha1 == null) {
            childcommit.setsubparentsha1(old.getSubparent());
        } else {
            childcommit.setParent(commitsha1);
            childcommit.setsubparentsha1(branchid);
        }
        childcommit.saveCommit();
        Currentstate state = Currentstate.fromFile();
        state.clearrmfiles();
        List<String> rmdirectory = Utils.plainFilenamesIn(rmf);
        if (rmdirectory != null) {
            for (String files: rmdirectory) {
                File rmFile = Utils.join(rmf, files);
                rmFile.delete();
            }
        }
        state.setCommitsha1(childcommit.sha1());
        state.putbranch(state.getcurbranch());
        state.saveState();
        return childcommit;
    }

    public static void rmfunc(String[] args) {
        boolean reasontoremove = true;
        List<String> stagenames = Utils.plainFilenamesIn(stagef);
        if (stagenames != null && stagenames.contains(args[1])) {
            Utils.join(stagef, args[1]).delete();
            reasontoremove = false;
        }
        Currentstate curstate = Currentstate.fromFile();
        String commitsha1 = curstate.getCommitsha1();
        Commit latestcommit = Commit.fromFile(commitsha1);
        if (latestcommit.getFiles().containsKey(args[1])) {
            reasontoremove = false;
            curstate.savermfiles(args[1], latestcommit.getFiles().get(args[1]));
            File filetorm = Utils.join(rmf, args[1]);
            File original = Utils.join(CWD, args[1]);
            String blobadditions = latestcommit.getFiles().get(args[1]);
            String combined = Utils.readContentsAsString(Utils.join(blobf, blobadditions));
            Utils.writeContents(filetorm, combined);
            original.delete();
            curstate.saveState();
        }
        if (reasontoremove) {
            exitfunc("No reason to remove the file.");
        }
        curstate.saveState();
    }

    public static void logfunc(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        Currentstate curstate = Currentstate.fromFile();
        String commitsha1 = curstate.getCommitsha1();
        Commit latestcommit = Commit.fromFile(commitsha1);
        String time;
        if (!latestcommit.getParent().equals("null")) {
            do {
                System.out.println("===");
                System.out.println("commit " + latestcommit.sha1());
                time = format.format(latestcommit.getDate());
                System.out.println("Date: " + time);
                System.out.println(latestcommit.getMessage());
                System.out.println();
                latestcommit = Commit.fromFile(latestcommit.getParent());
            } while (!latestcommit.getParent().equals("null"));
        }
        System.out.println("===");
        System.out.println("commit " + latestcommit.sha1());
        time = format.format(latestcommit.getDate());
        System.out.println("Date: " + time);
        System.out.println(latestcommit.getMessage());
        System.exit(0);
    }

    public static void globallogfunc(String[] args) {
        List<String> commits = Utils.plainFilenamesIn(commitf);
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        int amount = commits.size();
        int count = 0;
        for (String files: commits) {
            count++;
            Commit temp = Commit.fromFile(files);
            System.out.println("===");
            System.out.println("commit " + temp.sha1());
            if (temp.getSubparent() != null) {
                System.out.println("Merge: " + temp.getSubparent().substring(0, 6) + temp.getParent().substring(0, 6));
            }
            String time = format.format(temp.getDate());
            System.out.println("Date: " + time);
            System.out.println(temp.getMessage());
            if (count != amount) {
                System.out.println();
            }
        }
    }

    public static void findfunc(String[] args) {
        boolean abletofind = false;
        List<String> commitfiles = Utils.plainFilenamesIn(commitf);
        for (String files: commitfiles) {
            Commit filesincmf = Commit.fromFile(files);
            if (filesincmf.getMessage().equals(args[1])) {
                abletofind = true;
                System.out.println(filesincmf.sha1());
            }
        }
        if (!abletofind) {
            exitfunc("Found no commit with that message.");
        }
        System.exit(0);
    }

    public static void statusfunc(String[] args) {
        System.out.println("=== Branches ===");
        Currentstate curstate = Currentstate.fromFile();
        String currentbranch = curstate.getcurbranch();
        Set<String> branchlist = curstate.getbranches().keySet();
        int branchcount = branchlist.size();
        String[] listofbranches = new String[branchcount];
        int index = 0;
        for (String branch: branchlist) {
            listofbranches[index] = branch;
            index++;
        }
        Arrays.sort(listofbranches);
        for (String match : listofbranches) {
            if (!currentbranch.equals(match)) {
                System.out.println(match);
            } else {
                System.out.println("*" + match);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> stagedfiles = Utils.plainFilenamesIn(stagef);
        if (stagedfiles != null) {
            for (String file: stagedfiles) {
                System.out.println(file);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> rmfiles = Utils.plainFilenamesIn(rmf);
        if (rmfiles != null) {
            for (String file: rmfiles) {
                System.out.println(file);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    public static void checkoutfunc(String[] args) {
        if (args.length == 2) {
            Currentstate curstate = Currentstate.fromFile();
            if (curstate.getcurbranch().equals(args[1])) {
                exitfunc("No need to checkout the current branch.");
            }
            if (curstate.getbranches().containsKey(args[1])) {
                String branchname = args[1];
                String branchid = curstate.getbranchof(branchname);
                String[] branchnameplusid = {"reset", branchid};
                resetfunc(branchnameplusid, false);
                Currentstate newcurstate = Currentstate.fromFile();
                newcurstate.setCurrentbranch(branchname);
                newcurstate.putbranch(branchname, branchid);
                newcurstate.saveState();
                System.exit(0);
            } else {
                exitfunc("No such branch exists.");
            }
        } 
        if (args[1].equals("--") && args.length == 3) { 
            String filename = args[2];
            Currentstate curstate = Currentstate.fromFile();
            String commitsha1 = curstate.getCommitsha1();
            Commit latestcommit = Commit.fromFile(commitsha1);
            if (latestcommit.getFiles().containsKey(filename)) {
                String filesha1 = latestcommit.getFiles().get(filename);
                File blobfile = Utils.join(blobf, filesha1);
                File original = Utils.join(CWD, filename);
                String blobcontent = Utils.readContentsAsString(blobfile);
                Utils.writeContents(original, blobcontent);
            } else {
                exitfunc("File does not exist in that commit.");
            }
            curstate.saveState();
        }
        if (args[2].equals("--") && args.length == 4) {
            checkouthelpercase3(args);
        }
    }
    /**
     *
     * @param args arguments
     */
    public static void checkouthelpercase3(String[] args) {
        String commitsha1 = args[1];
        String filename = args[3];
        List<String> cmfdir = Utils.plainFilenamesIn(commitf);
        if (cmfdir == null) {
            exitfunc("No commit with that id exists.");
        }
        if (commitsha1.length() < Utils.UID_LENGTH) {
            for (String id: cmfdir) {
                String sub = id.substring(0, commitsha1.length());
                if (sub.equals(commitsha1)) {
                    commitsha1 = id;
                    break;
                }
            }
        }
        if (cmfdir.contains(commitsha1)) {
            Commit commitfile = Commit.fromFile(commitsha1);
            List<String> allfilesinCWD = Utils.plainFilenamesIn(CWD);
            Currentstate curstate = Currentstate.fromFile();
            String currcommitsha1 = curstate.getCommitsha1();
            Commit currcommit = Commit.fromFile(currcommitsha1);
            if (commitfile.getFiles().containsKey(filename)) {
                if (allfilesinCWD != null) {
                    for (String file: allfilesinCWD) {
                        if (!currcommit.getFiles().containsKey(file)) {
                            if (commitfile.getFiles().containsKey(file)) {
                                exitfunc("There is an untracked file in the way; delete it or add and commit it first.");
                            }
                        }
                    }
                }
                String filesha1 = commitfile.getFiles().get(filename);
                File blobfile = Utils.join(blobf, filesha1);
                File original = Utils.join(CWD, filename);
                String blobcontent = Utils.readContentsAsString(blobfile);
                Utils.writeContents(original, blobcontent); }
            else {
                exitfunc("File does not exist in that commit.");
            }
        } else {
            exitfunc("No commit with that id exists.");
        }
    }

    public static void branchfunc(String[] args) {
        Currentstate curstate = Currentstate.fromFile();
        if (curstate.getbranches().containsKey(args[1])) {
            exitfunc("A branch with that name already exists.");
        } else {
            curstate.putbranch(args[1]);
            curstate.saveState();
        }
        System.exit(0);
    }

    public static void rmbranchfunc(String[] args) {
        Currentstate curstate = Currentstate.fromFile();
        if (curstate.getcurbranch().equals(args[1])) {
            exitfunc("Cannot remove the current branch.");
        }
        if (!curstate.getbranches().containsKey(args[1])) {
            exitfunc("A branch with that name does not exist.");
        } else {
            curstate.removebranch(args[1]);
        }
        curstate.saveState();
        System.exit(0);
    }

    public static void resetfunc(String[] args, boolean reset) {
        Currentstate curstate = Currentstate.fromFile();
        String currentcommitsha1 = curstate.getCommitsha1();
        Commit currentcommit = Commit.fromFile(currentcommitsha1);
        List<String> allfilesinCWD = Utils.plainFilenamesIn(CWD);
        List<String> commits = Utils.plainFilenamesIn(commitf);
        if (!commits.contains(args[1])) {
            exitfunc("No commit with that id exists.");
        }
        Commit argscommit = Commit.fromFile(args[1]);
        if (allfilesinCWD != null) {
            for (String file: allfilesinCWD) {
                if (!currentcommit.getFiles().containsKey(file)) {
                    if (argscommit.getFiles().containsKey(file)) {
                        exitfunc("There is an untracked file in the way; delete it or add and commit it first.");
                    }
                }
            }
        }
        for (String previous: currentcommit.getFiles().keySet()) {
            if (!argscommit.getFiles().containsKey(previous)) {
                File previousFile = Utils.join(CWD, previous);
                previousFile.delete();
            }
        }
        for (String originname: argscommit.getFiles().keySet()) {
            String blobname = argscommit.getFiles().get(originname);
            File blobfile = Utils.join(blobf, blobname);
            File original = Utils.join(CWD, originname);
            String blobcontent = Utils.readContentsAsString(blobfile);
            Utils.writeContents(original, blobcontent);
        }
        curstate.setCommitsha1(args[1]);
        if (reset) {
            curstate.putbranch(curstate.getcurbranch(), args[1]);
        }
        List<String> stagefiles = Utils.plainFilenamesIn(stagef);
        for (String file: stagefiles) {
            File todelete = Utils.join(stagef, file);
            todelete.delete();
        }
        stagef.delete();
        stagef.mkdir();
        curstate.saveState();
    }

    public static void mergefuncerrors(String[] args) {
        Currentstate curstate = Currentstate.fromFile();
        List<String> rmfiles = Utils.plainFilenamesIn(rmf);
        List<String> stagedfiles = Utils.plainFilenamesIn(stagef);
        if (rmfiles.size() != 0 || stagedfiles.size() != 0) {
            exitfunc("You have uncommitted changes.");
        }
        if (!curstate.getbranches().containsKey(args[1])) {
            exitfunc("A branch with that name does not exist.");
        }
    }

    public static void mergefunc(String[] args) {
        boolean conflict = false;
        Currentstate curstate = Currentstate.fromFile();
        String headname = curstate.getcurbranch();
        String headid = curstate.getCommitsha1();
        mergefuncerrors(args);
        if (headname.equals(args[1])) {
            exitfunc("Cannot merge a branch with itself.");
        }
        Commit headcommit = Commit.fromFile(curstate.getCommitsha1());
        String branchid = curstate.getbranches().get(args[1]);
        Commit branchcommit = Commit.fromFile(branchid);
        boolean fals = false;
        String closestancestor = headcommit.findclosestancestor(branchid, fals);
        if (closestancestor.equals(branchid)) {
            exitfunc("Given branch is an ancestor of the current branch.");
        }
        if (closestancestor.equals(headid)) {
            curstate.setCommitsha1(closestancestor);
            curstate.saveState();
            String[] tobecheckedout = {"checkout", args[1]};
            checkoutfunc(tobecheckedout);
            exitfunc("Current branch fast-forwarded.");
        }
        Commit ancestorcommit = Commit.fromFile(closestancestor);
        Set<String> headfiles = headcommit.getFiles().keySet();
        Set<String> ancestorfiles = ancestorcommit.getFiles().keySet();
        Set<String> branchfiles = branchcommit.getFiles().keySet();
        conflict = checkpartialmerge(closestancestor, args, conflict);
        conflict = checkmergeagain(closestancestor, args, conflict);
        Date date = new Date();
        String[] commitmessage = aftermergemessage(curstate.getcurbranch(), args);
        commitfunc(commitmessage, date, false, branchid, curstate.getCommitsha1());
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static boolean checkmergeagain(String closestancestor, String[] args, boolean conflict) {
        Currentstate curstate = Currentstate.fromFile();
        Commit headcommit = Commit.fromFile(curstate.getCommitsha1());
        String branchid = curstate.getbranches().get(args[1]);
        Commit branchcommit = Commit.fromFile(branchid);
        Commit anscommit = Commit.fromFile(closestancestor);
        Set<String> headfiles = headcommit.getFiles().keySet();
        Set<String> ansfiles = anscommit.getFiles().keySet();
        Set<String> branchfiles = branchcommit.getFiles().keySet();
        for (String ele: ansfiles) {
            if (headfiles.contains(ele)) {
               boolean first = changed(headcommit, anscommit, ele);
                if (!first && !branchfiles.contains(ele)) {
                    String[] rmdata = {"rm", ele};
                    rmfunc(rmdata);
                }
                if (branchfiles.contains(ele)) {
                    boolean second = changed(headcommit, branchcommit, ele);
                    boolean third = changed(anscommit, branchcommit, ele);
                    if (first && second && third) {
                        conflictfunc(ele, headcommit, branchcommit);
                        conflict = true;
                    }
                } else {
                    if (first) {
                        conflictfunc(ele, headcommit, branchcommit);
                        conflict = true;
                    }
                }
            } else {
                boolean third = changed(anscommit, branchcommit, ele);
                if (third) {
                    conflictfunc(ele, headcommit, branchcommit);
                    conflict = true;
                }
            }
        }
        return conflict;
    }

    public static boolean checkpartialmerge(String closestancestor, String[] args, boolean conflict) {
        Currentstate curstate = Currentstate.fromFile();
        Commit headcommit = Commit.fromFile(curstate.getCommitsha1());
        String branchid = curstate.getbranches().get(args[1]);
        Commit branchcommit = Commit.fromFile(branchid);
        Commit anscommit = Commit.fromFile(closestancestor);
        Set<String> headfiles = headcommit.getFiles().keySet();
        Set<String> ansfiles = anscommit.getFiles().keySet();
        Set<String> branchfiles = branchcommit.getFiles().keySet();
        for (String ele: branchfiles) {
            if (ansfiles.contains(ele)) {
               boolean first = changed(anscommit, branchcommit, ele);
                if (headfiles.contains(ele)) {
                    boolean second = changed(headcommit, anscommit, ele);
                    if (first && !second) {
                        String[] temp = {"checkout", branchid, "--", ele};
                        checkoutfunc(temp);
                        String[] temp2 = {"add", ele};
                        addfunc(temp2);
                    }
                }
            } else {
                if (!headfiles.contains(ele)) {
                    String[] temp = {"checkout", branchid, "--", ele};
                    checkoutfunc(temp);
                    String[] temp2 = {"add", ele};
                    addfunc(temp2);
                } else {
                    boolean second = changed(headcommit, branchcommit, ele);
                    if (second) {
                        conflictfunc(ele, headcommit, branchcommit);
                        conflict = true;
                    }
                }
            }
        }
        return conflict;
    }


    public static String[] aftermergemessage(String branch, String[] args) {
        String aftermergemessage = "Merged ";
        aftermergemessage += args[1];
        aftermergemessage += " into ";
        aftermergemessage += branch + ".";
        String[] commitmessage = {"commit", aftermergemessage};
        return commitmessage;
    }

    public static void conflictfunc(String name, Commit cbranch, Commit gbranch) {
        String cblob = cbranch.getFiles().get(name);
        String gblob = gbranch.getFiles().get(name);
        File cblobfile = Utils.join(blobf, cblob);
        List<String> filesinblob = Utils.plainFilenamesIn(blobf);
        String ccontent = Utils.readContentsAsString(cblobfile);
        String mergecontent = "<<<<<<< HEAD\n";
        mergecontent += ccontent;
        mergecontent += "=======\n";
        if (filesinblob.contains(gblob)) {
            File gblobfile = Utils.join(blobf, gblob);
            String gcontent = Utils.readContentsAsString(gblobfile);
            mergecontent += gcontent;
        }
        mergecontent += ">>>>>>>\n";
        File merge = Utils.join(stagef, name);
        File origin = Utils.join(CWD, name);
        Utils.writeContents(merge, mergecontent);
        Utils.writeContents(origin, mergecontent);
    }

    public static boolean changed(Commit firstcommit, Commit secondcommit, String filename) {
        String firstcommitsha1 = firstcommit.getFiles().get(filename);
        String secondcommitsha1 = secondcommit.getFiles().get(filename);
        File firstblob = Utils.join(blobf, firstcommitsha1);
        String firstcontent = Utils.readContentsAsString(firstblob);
        File secondblob = Utils.join(blobf, secondcommitsha1);
        String secondcontent = Utils.readContentsAsString(secondblob);
        return !firstcontent.equals(secondcontent);
    }
}
