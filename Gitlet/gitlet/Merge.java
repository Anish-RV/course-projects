package gitlet;
import java.io.*;
import java.util.*;

import static gitlet.Main.gitletDir;
import static gitlet.Main.workingDir;
import static gitlet.Utils.readContents;
import static gitlet.Utils.writeContents;

public class Merge {
    public static void merge(String branchName, Staging staging, Branches branch) {
        String currSHA = branch.branchMap.get(branch.head);
        String givenSHA = branch.branchMap.get(branchName);
        Commit currCommit = Commit.readCommit(currSHA);
        Commit givenCommit = Commit.readCommit(givenSHA);
        String splitPoint = findSplit(currCommit, givenCommit);
        gitlet.Commit shaCommit = gitlet.Commit.readCommit(splitPoint);
        HashMap<String, String> givenOnly = checkingThree(givenCommit, currCommit, shaCommit);
        HashMap<String, String> currOnly = checkingThree(currCommit, givenCommit, shaCommit);
        HashMap<String, String>[] mod_unmod = modifiedFiles(shaCommit, givenCommit, currCommit);
        HashMap<String, String> mod_given = mod_unmod[0];
        HashMap<String, String> mod_curr = mod_unmod[1];
        List<String>[] absent = absentInCommit(shaCommit, givenCommit, currCommit);
        List<String> currAbsent = absent[0];
        List<String> givenAbsent = absent[1];
        List<String> conflictFiles = conflict(shaCommit, givenCommit, currCommit);
        List<String> otherConflict = Modifed_Absent(shaCommit, givenCommit, currCommit);
        boolean conflictedFiles = false;
        if (givenSHA.compareTo(splitPoint) == 0) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (currSHA.compareTo(splitPoint) == 0) {
            branch.branchMap.replace(branch.head, givenSHA);
            System.out.println("Current branch fast-forwarded.");
        }
        if (!mod_given.isEmpty()) {
            for (String file : mod_given.keySet()) {
                gitlet.Tree.checkoutID(givenSHA, file);
                gitlet.Staging.addFile(staging, file, mod_given.get(file));
            }
        }
        if (!mod_curr.isEmpty() || !currOnly.isEmpty() || !currAbsent.isEmpty()) {
            System.out.print("");
        }
        if (!givenOnly.isEmpty()) {
            for (String filename : givenOnly.keySet()) {
                gitlet.Tree.checkoutID(givenSHA, filename);
                gitlet.Staging.addFile(staging, filename, givenOnly.get(filename));
            }
        }
        if (!givenAbsent.isEmpty()) {
            for (String file: givenAbsent) {
                gitlet.Tree.rm(staging, branch, file); //will this rm and untrack the file specified?
            }
        }
        if (!conflictFiles.isEmpty() || !otherConflict.isEmpty()) {
            for (String filename : conflictFiles) {
                gitlet.Utils.restrictedDelete(filename);
            }
            conflictedFiles = true;
        }
        if (conflictedFiles) {
            System.out.println("Encountered a merge conflict.");
            fixConflict(otherConflict, givenSHA, currSHA, givenCommit, currCommit);
            fixConflict(conflictFiles, givenSHA, currSHA, givenCommit, currCommit);

        } else { // do i stage and then commit the files to commit them or do i not stage them and just commit them?
            //for (String filename: (Set<String>) givenCommit.getCommitFiles().keySet()) {
                //gitlet.Staging.addFile(staging, filename, (String) givenCommit.getCommitFiles().get(filename));
            //}
            Commit.makeCommit(staging, branch, "Merged " + branch.head + " with " + branchName + ".");
        }


    }

    private static String findSplit(gitlet.Commit currCommit, gitlet.Commit givenCommit) {
        String splitPoint = null;
        /*while (splitPoint == null && currCommit.getMessage().compareTo("initial commit") != 0 || givenCommit.getMessage().compareTo("initial commit") != 0) {
            if (currCommit.getMessage().compareTo("initial commit") == 0 && givenCommit.getMessage().compareTo("initial commit") != 0) {
                splitPoint = currCommit.getID();
                givenCommit = Commit.readCommit(givenCommit.getParent());
            } else if (givenCommit.getMessage().compareTo("initial commit") == 0 && currCommit.getMessage().compareTo("initial commit") != 0) {
                splitPoint = givenCommit.getID();
                currCommit = Commit.readCommit(currCommit.getParent());
            } else if (givenCommit.getID().compareTo(currCommit.getID()) == 0){
                splitPoint = givenCommit.getID();
            } else {
                currCommit = Commit.readCommit(currCommit.getParent());
                givenCommit = Commit.readCommit(givenCommit.getParent());
            }
        } */
        Commit iterate;
        Commit smallerCommit;
        if (currCommit.getDate().compareTo(givenCommit.getDate()) < 0) {
            iterate = givenCommit;
            smallerCommit = currCommit;
        } else {
            iterate = currCommit;
            smallerCommit = givenCommit;
        }
        if (iterate.getMessage().compareTo("initial commit") != 0) {
            splitPoint = iterate.getID();
        }
        while (iterate.getMessage().compareTo("initial commit") != 0) {
            if (iterate.getID().compareTo(smallerCommit.getID()) == 0) {
                splitPoint = iterate.getID();
                break;
            }
            while (smallerCommit.getMessage().compareTo("initial commit") != 0 && smallerCommit.getID().compareTo(iterate.getID()) != 0) { //can smaller be initial and iterate be initial
                /**if (smallerCommit.getMessage().compareTo("initial commit") == 0) {
                    splitPoint = smallerCommit.getID();
                    break;
                }
                if (iterate.getID().compareTo(smallerCommit.getID()) == 0) {
                    splitPoint = iterate.getID();
                    break;
                } else { */
                    smallerCommit = Commit.readCommit(smallerCommit.getParent());
            //}
            }
            if (iterate.getID().compareTo(smallerCommit.getID()) == 0) {
                splitPoint = iterate.getID();
                break;
            }
            iterate = Commit.readCommit(iterate.getParent());
            if (currCommit.getDate().compareTo(givenCommit.getDate()) < 0) {
                smallerCommit = currCommit;
            } else {
                smallerCommit = givenCommit;
            }

        }
        return splitPoint;
    }

    /*replace the contents of the conflicted file with the following*/
    private static void fixConflict(List<String> files, String givenSHA, String currSHA, Commit givenCommit, Commit currCommit) {
        for (String filename: files) {
            File givenFile = new File(Utils.join(givenCommit.commitfilesdir, filename).getPath());
            File currFile = new File(Utils.join(currCommit.commitfilesdir, filename).getPath());
            String givenString = null;
            String currString = null;
            try {
                givenString = new String(Utils.readContents(givenFile), "UTF-8");
                currString = new String(Utils.readContents(currFile), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String stringToReplace = "<<<<<<< HEAD" +System.lineSeparator() +  currString + "=======" + System.lineSeparator() + givenString + ">>>>>>>" + System.lineSeparator();
            byte[] byteToReplace = stringToReplace.getBytes();

            File checked = new File(Utils.join(gitletDir, filename).getPath());
            writeContents(checked, byteToReplace);

            File current = new File(Utils.join(Main.workingDir, filename).getPath());
            byte[] checkedbyte = readContents(checked);
            writeContents(current, checkedbyte);




        }
    }

    private static HashMap<String, String>[] modifiedFiles(gitlet.Commit spCommit, gitlet.Commit givenCommit, gitlet.Commit currCommit) {
        HashMap<String, String> modified_given = new HashMap<>();
        HashMap<String, String> modified_curr = new HashMap<>();
        HashMap<String, String>[] lists = new HashMap[]{modified_given, modified_curr};
        if (spCommit.getCommitFiles() != null) {
            for (String filename : (Set<String>) spCommit.getCommitFiles().keySet()) {
                if (currCommit.getCommitFiles().containsKey(filename) && givenCommit.getCommitFiles().containsKey(filename)) {
                    if (!spCommit.getCommitFiles().get(filename).equals(givenCommit.getCommitFiles().get(filename))) {
                        if (spCommit.getCommitFiles().get(filename).equals(currCommit.getCommitFiles().get(filename))) {
                            modified_given.put(filename, (String) givenCommit.getCommitFiles().get(filename));
                        }
                    } else {
                        if (!spCommit.getCommitFiles().get(filename).equals(currCommit.getCommitFiles().get(filename))) {
                            if (spCommit.getCommitFiles().get(filename).equals(givenCommit.getCommitFiles().get(filename))) {
                                modified_curr.put(filename, (String) currCommit.getCommitFiles().get(filename));
                            }
                        }
                    }
                }

            }
        }
        return lists;
    }

    private static List<String> Modifed_Absent(gitlet.Commit spCommit, gitlet.Commit givenCommit, gitlet.Commit currCommit) {
        List<String> present = new ArrayList<>();
        if(spCommit.getCommitFiles() != null) {
            for (String filename : (Set<String>) spCommit.getCommitFiles().keySet()) {
                if (givenCommit.getCommitFiles().containsKey(filename)
                        && !givenCommit.getCommitFiles().get(filename).equals(spCommit.getCommitFiles().get(filename))) {
                    if (!currCommit.getCommitFiles().containsKey(filename)) {
                        present.add(filename);//, (String) givenCommit.getCommitFiles().get(filename));
                    }
                } else if (currCommit.getCommitFiles().containsKey(filename)
                        && !currCommit.getCommitFiles().get(filename).equals(spCommit.getCommitFiles().get(filename))) {
                    present.add(filename);//, (String) currCommit.getCommitFiles().get(filename));
                }
            }
        }
        return present;
    }

    /**Returns two list, one where there are files missing in the given branch
     * and the other contins files that were missing in the current branch. absent in either current or given commit
     * and unmodified in the commit that is present*/
    private static List<String>[] absentInCommit(gitlet.Commit spCommit, gitlet.Commit givenCommit, gitlet.Commit currCommit) {
        List<String> present_curr = new ArrayList<>();
        List<String> present_given = new ArrayList<>();
        List<String>[] lists = new List[]{present_given, present_curr};
        if (spCommit.getCommitFiles() != null) {
            for (String filename : (Set<String>) spCommit.getCommitFiles().keySet()) {
                if (!givenCommit.getCommitFiles().containsKey(filename) && currCommit.getCommitFiles().containsKey(filename)) {
                    if (spCommit.getCommitFiles().get(filename).equals(currCommit.getCommitFiles().get(filename))) {
                        present_curr.add(filename);
                    }
                } else if (!currCommit.getCommitFiles().containsKey(filename) && givenCommit.getCommitFiles().containsKey(filename)) {
                    if (spCommit.getCommitFiles().get(filename).equals(givenCommit.getCommitFiles().get(filename))) {
                        present_given.add(filename);
                    }
                }

            }
        }
        return lists;
    }

    private static List<String> conflict(gitlet.Commit spCommit, gitlet.Commit givenCommit, gitlet.Commit currCommit) {
        //String[] bothFile = new String[2];
        List<String> conflict = new ArrayList<>();
        for (String filename : (Set<String>) givenCommit.getCommitFiles().keySet()) {
            if (currCommit.getCommitFiles().containsKey(filename)) {
                if (!currCommit.getCommitFiles().get(filename).equals(givenCommit.getCommitFiles().get(filename))) {
                    if (spCommit.getCommitFiles().containsKey(filename)) {
                        if (!spCommit.getCommitFiles().get(filename).equals(givenCommit.getCommitFiles().get(filename))
                            && !spCommit.getCommitFiles().get(filename).equals(currCommit.getCommitFiles().get(filename))) {
                        //bothFile[0] = (String) givenCommit.getCommitFiles().get(filename);
                        //bothFile[1] = (String) currCommit.getCommitFiles().get(filename);
                        conflict.add(filename);
                        //Arrays.fill(bothFile, null);
                        }
                    } else {
                        //bothFile[0] = (String) givenCommit.getCommitFiles().get(filename);
                        //bothFile[1] = (String) currCommit.getCommitFiles().get(filename);
                        conflict.add(filename);
                        //Arrays.fill(bothFile, null);
                    }
                }
            }
        }
        return conflict;
    }



    /**
     * Method will take in three commit objects and will return a list of filenames that are only present in the first commit.
     */
    private static HashMap<String, String> checkingThree(gitlet.Commit c1, gitlet.Commit c2, gitlet.Commit c3) {
        HashMap<String, String> absentFiles = new HashMap<>();
        /*int min = Math.min(c3.getCommitFiles().size(), c2.getCommitFiles().size());
        HashMap<String, String> iterate = new HashMap<>();
        if (min <= c1.getCommitFiles().size()) {
            if (min == c3.getCommitFiles().size()) {
                iterate = c3.getCommitFiles();
            } else {
                iterate = c2.getCommitFiles();
            }
        } else {
            iterate = c1.getCommitFiles();
        } */
        for (String filename : (Set<String>) c1.getCommitFiles().keySet()) {
            if (c2.getCommitFiles() != null && !c2.getCommitFiles().containsKey(filename)) {
                if (c3.getCommitFiles() != null && !c3.getCommitFiles().containsKey(filename)) {
                    absentFiles.put(filename, (String) c1.getCommitFiles().get(filename));
                } else if (c3.getCommitFiles() == null) {
                    absentFiles.put(filename, (String) c1.getCommitFiles().get(filename));
                }
            } else if (c2.getCommitFiles() == null) {
                absentFiles.put(filename, (String) c1.getCommitFiles().get(filename));
            }
        }
        return absentFiles;
    }

    /*private static File readFile(File file) {
        try {
            File newFile;
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(file));
            newFile = (File) inp.readObject();
            inp.close();
            return newFile;
        } catch (IOException | ClassNotFoundException excp) {
            return null;
        }
    } */
}
