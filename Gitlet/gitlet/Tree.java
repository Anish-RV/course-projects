package gitlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import gitlet.Branches;

import static gitlet.Main.workingDir;
import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * This class will be reconstructed at runtime from information using
 * deserialized Branches & commit objects.
 */

public class Tree {

    String currCommit;
    String initialID;
    static HashMap<String, gitlet.Commit> tree;
    public static final String gitletDir = Main.currentDir;
    public static final String dir = System.getProperty("user.dir"); //from
    //https://stackoverflow.com/questions/3153337/get-current-working-directory-in-java/3153440
    public Error e = new Error();


    public Tree(Branches b, Commit initialCommit) {
        tree = new HashMap<>();
        tree.put(initialCommit.getID(), initialCommit ); //replace SHA1 with SHA1 of Initialcommit
        initialID = initialCommit.getID();
        currCommit = initialID;
        b.head = "master";
    }

    public void add(gitlet.Commit c) {
        currCommit = c.getID(); //Add method for getting SHA1 id of commit.
        tree.put(currCommit, c);
    }

    public String getCurrCommit() {
        return currCommit;
    }

    /**
     * If a file is not tracked then it will be unstaged if the file was staged
     */
    public static void rm(Staging s, Branches b, String fileName) {
        //File dummy = new File(gitletDir + "\\" + file);
        Commit headCommit = Commit.readCommit(b.branchMap.get(b.head));
        if (s.stage.isEmpty() && headCommit.getCommitFiles() == null) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (headCommit.getCommitFiles() == null) {
            Staging.unStage(s, fileName);
        } else if (headCommit.getCommitFiles().containsKey(fileName)) {
            s.rm.add(fileName);
            Utils.restrictedDelete(fileName);
            Staging.rmStage(s, fileName);
        } else if (s.stage.containsKey(fileName)) {
            Staging.rmStage(s, fileName);
        }
    }

    public static void adder(Staging s, Branches b, String fileName) {
        Commit headCommit = Commit.readCommit(b.branchMap.get(b.head));
        if (s.rm.contains(fileName)) {
            s.rm.remove(fileName);
        } else if (headCommit.getCommitFiles() != null && headCommit.getCommitFiles().containsKey(fileName)) {
            try {
                File current = new File(Utils.join(workingDir,fileName).getPath()); //fil
                // e in working directory
                File checked = new File(Utils.join(headCommit.commitfilesdir, fileName).getPath()); //file in head commit -- need to save path of file w/ fileName
                String checkedSHA = Utils.sha1(readContents(checked));
                String currentSHA = Utils.sha1(readContents(current));
                if (checkedSHA.compareTo(currentSHA) == 0) {
                    System.exit(0);
                }
            } catch (IllegalArgumentException readingExcp) {
                Staging.read_and_hash(s, fileName);
            }
                try {
                    Staging.read_and_hash(s, fileName);
                } catch (IllegalArgumentException excp) {
                    System.out.println("File does not exist.");
                    System.exit(0);
                }
        } else {
            try {
                Staging.read_and_hash(s, fileName);
            } catch (IllegalArgumentException excp) {
                System.out.println("File does not exist.");
                System.exit(0);
            }
        }

    }




    /**
     * Prints out all of the commits ever made (regardless of what branch they were committed in.
     */
    public static void globalLog(Branches b) {
//        for (String c : tree.keySet()) {
//            gitlet.Commit currCommit = tree.get(c);
//            System.out.println("===\nCommit " + c
//                    + "\n" + currCommit.getDate()
//                    + "\n" + currCommit.getMessage()
//                    + "\n\n");
//        }
        for (String commitID : b.branchMap.values()) {
            Commit currCommit = Commit.readCommit(commitID);
            globalLogPrinter(currCommit);
            while (currCommit.parent != null) {
                currCommit = Commit.readCommit(currCommit.parent);
                globalLogPrinter(currCommit);
            }
        }
    }

    public static void globalLogPrinter(Commit c) {
        System.out.println("===\nCommit " + c.getID()
                + "\n" + c.getDate()
                + "\n" + c.getMessage()
                + "\n\n");
    }




    /** Takes the version of the file as it exists in the head commit
     * and puts it in the working directory, overwriting the version of
     * the file that's already there if there is one. The new version of
     * the file is not staged.
     * @param fileName
     */
    public static void checkoutFile(String fileName, Branches b) {
        Commit headCommit = Commit.readCommit(b.branchMap.get(b.head));
        if (headCommit.getMessage().compareTo("Initial commit.") == 0) {
            return;
        }
        if (headCommit.getCommitFiles().containsKey(fileName)) {
            File current = new File(Utils.join(workingDir, fileName).getPath()); //file in working directory
            File checked = new File(Utils.join(headCommit.commitfilesdir, fileName).getPath()); //file in head commit -- need to save path of file w/ fileName
//            try {
            byte[] checkedbyte = readContents(checked);
            writeContents(current, checkedbyte);

//                Files.copy(checked.toPath(), current.toPath(), REPLACE_EXISTING, COPY_ATTRIBUTES);

//                String fileID = (String) headCommit.getCommitFiles().get(fileName);
//                File file = new File(headCommit.workingHere + "\\" + fileID);
//                File gotFile = readFile(file);
//                Utils.restrictedDelete(fileID);

//            } catch (IOException e) {
//                Error.noFileName();
//            }
        } else {
            System.out.print("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /** Takes the version of the file as it exists in the commit with the
     * given ID and puts it in the working directory, overwriting the version
     * of the file that's already there if there is one. The new version of the
     * file is not staged.
     * @param commitID
     * @param fileName
     */
    public static void checkoutID(String commitID, String fileName) {
        if (commitID.length() < 40) {
            File commitsDir = new File(Utils.join(gitletDir, "commits").getPath());
            String[] commitsList = commitsDir.list();
            for (String file : commitsList) {
                if (file.startsWith(commitID)) {
                    commitID = file;
                }
            }
        }
        File existingCommit = new File(Utils.join(gitletDir, "commits", commitID, commitID).getPath());
        if (!existingCommit.exists()) {
            System.out.print("No commit with that id exists.");
            System.exit(0);
        }
        /*if (isUntracked(commitID)) {
            System.out.println("There is an untracked file in the way; " +
                    "delete it or add it first.");
            System.exit(0);
        }*/
        Commit checkCommit = Commit.readCommit(commitID);
        if (checkCommit.getCommitFiles().containsKey(fileName)) {
            File current = new File(Utils.join(dir, fileName).getPath()); //file in working directory
            File checked = new File(Utils.join(checkCommit.commitfilesdir, fileName).getPath()); //file in head commit
            while (!checked.isFile()) {
                checkCommit = Commit.readCommit(checkCommit.parent);
                checked = new File(Utils.join(checkCommit.commitfilesdir, fileName).getPath());
            }
            byte[] checkedbyte = readContents(checked);
            writeContents(current, checkedbyte);
        } else {
            System.out.print("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /** Takes ALL files in the commit at the head of the given branch and puts
     * them in the working directory, overwriting the versions of the files that
     * are already there if they exist. ALSO, at the end of this command, the
     * BRANCHNAME will be considered the current branch HEAD. Any files that
     * are tracked in the current branch but are not present in the checked-out
     * branch are deleted. The staging area is cleared, unless the checked-out
     * branch is the current branch.
     * @param branchName
     */
    public static void checkoutBranch(Branches b, Staging s, String branchName) {
        if (!b.branchMap.containsKey(branchName)) {
            System.out.print("No such branch exists.");
            System.exit(0);
        } else if (b.head.equals(branchName)) {
            System.out.print("No need to checkout current branch.");
            System.exit(0);
        } else if (isUntracked(b.branchMap.get(b.head))) {
            System.out.println("There is an untracked file in the way; " +
                    "delete it or add it first.");
            System.exit(0);
        }
        String commitID = b.branchMap.get(branchName);
        gitlet.Commit currCommit = gitlet.Commit.readCommit(commitID);
        HashMap files = currCommit.getCommitFiles();
        String headID = b.branchMap.get(b.head);
        Commit headCommit = Commit.readCommit(headID);
        HashMap tracked = headCommit.getCommitFiles();
        if (tracked != null) {
            Iterator trackedIt = tracked.entrySet().iterator();
            while (trackedIt.hasNext()) {
                HashMap.Entry pair = (HashMap.Entry) trackedIt.next();
                if (files == null) {
                    Utils.restrictedDelete((String) pair.getKey());
                } else if (files.entrySet().contains(pair)) {
                    Utils.restrictedDelete((String) pair.getKey());
                } else if (!currCommit.getCommitFiles().keySet().contains(pair.getKey())) {
                    Utils.restrictedDelete((String) pair.getKey());
                }
            }
        }

        if (files != null) {
            Iterator itCommitFiles = files.entrySet().iterator();
            /*https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap*/
            while (itCommitFiles.hasNext()) {
                HashMap.Entry<String, String> mapping = (HashMap.Entry<String, String>) itCommitFiles.next();
                String fileName = mapping.getKey();
                File current = new File(Utils.join(dir,fileName).getPath()); //file in working directory
                File checked = new File(Utils.join(currCommit.commitfilesdir, fileName).getPath()); //file in head commit
                while (!checked.isFile()) {
                    currCommit = Commit.readCommit(currCommit.parent);
                    checked = new File(Utils.join(currCommit.commitfilesdir, fileName).getPath());
                }
                byte[] checkedbyte = readContents(checked);
                writeContents(current, checkedbyte);
            }
        }

        s.stage.clear();
        b.head = branchName;
    }



    /* Compares untracked files in working directory with those
    in the head commit.
     */
    public static boolean isUntracked(String CommitID) {//we can delete branchname
        List<String> workingFiles = plainFilenamesIn(dir);
        Commit currCommit = Commit.readCommit(CommitID);
        for (String file : workingFiles) {
            if (currCommit.getCommitFiles() == null) {
                return true;
            } else if (!currCommit.getCommitFiles().containsKey(file)) {
                return true;
            }

        }
        return false;
    }
    
    public static void status(Branches b, Staging s) {
        System.out.println("=== Branches ===");
        Object[] toSort = b.branchMap.keySet().toArray();
        Arrays.sort(toSort);
        for (Object i : toSort) {
            String key = i.toString();
            if (key.equals(b.head)) {
                System.out.print("*");
            }
            System.out.println(i);
        }
        System.out.println();
//        System.out.println();
        System.out.println("=== Staged Files ===");
        if (s.stage != null) {
            for (String i: (Set<String>) s.stage.keySet()) {
                System.out.println(i);
            }
        }
        System.out.println();
//        System.out.println();
        System.out.println("=== Removed Files ===");
        if (s.rm != null) {
            for (String i : s.rm) {
                System.out.println(i);
            }
        }
        System.out.println();
//        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
//        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void reset(Branches b, Staging s, String commitSHA) {

        Commit currcommit = Commit.readCommit(commitSHA);
        if (currcommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        for (Object a: currcommit.getCommitFiles().keySet()) {
            checkoutID(commitSHA, (String) a);
        }
        List<String> workingFiles = plainFilenamesIn(dir);
        for (String a: workingFiles) {
            //Should we be checking if the file is tracked here?
            if (!currcommit.getCommitFiles().containsKey(a)) {
                Utils.restrictedDelete(a);
            }
        }
        String branchName = currcommit.branchName;
        b.branchMap.replace(branchName, commitSHA);
        Staging.clearStage(s);

  }

    private static File readFile(File file) {
        try {
            File newFile;
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(file));
            newFile = (File) inp.readObject();
            inp.close();
            return newFile;
        } catch (IOException | ClassNotFoundException excp) {
            return null;
        }
    }
    /*In case if needed later */

    // private class CommitNode {
//        public gitlet.Commit commit;
//        public CommitNode parent;
//
//        /* New CommitNode Object containing current commit C and parent commit PREV */
//        public CommitNode(gitlet.Commit c, CommitNode prev) {
//            commit = c;
//            parent = prev;
//        }
//
//        @Override String toString() {
//
//            return "Tree.CommitNode.toString not implemented yet";
//        }
//
//        @Override
//        public boolean equals(CommitNode c) {
//            if (this == c) return true;
//            if (c == null || getClass() != c.getClass()) return false;
//            CommitNode cNode = (CommitNode) c;
//            return commit == cNode.commit && gitlet.Commit.equals(parent, cNode.parent);
//
//        }
//    }
    //
//   private CommitNode initialCommit;
//    public CommitNode currCommit;


}