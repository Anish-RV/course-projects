package gitlet;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/** Class is intended to create commit objects that can be serialized and deserialized*/
public class Commit implements Serializable {

    public String message;
    public Date date;
    public String parent;
    public HashMap files; //filename to blob SHA
    public String commitID;
    public String branchName;
    public String workingHere;
    public String commitfilesdir;
    public static final String gitletDir = Main.gitletDir;
    public static final String dir = System.getProperty("user.dir");

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


//    public Commit() {
//    }
//
//    /**Default commit will instianstiate the first ever commit.*/
//    public static class InitialCommit extends Commit {
//
//        public InitialCommit() {
//            message = "Initial commit.";
//            date = new Date();
//            parent = null;
//            files = null;
//            workingHere = System.getProperty("user.dir");
//            commitID = Utils.sha1(message+date);
//            File dir = new File(gitletDir  + "\\commits");
//            dir.mkdirs();        }

       // public InitialCommit initialCommit = new InitialCommit();


        //public InitialCommit getInitialCommit() {
          //  return initialCommit;
        //}



    /**Default Commit constructor will only be instantated for the initial commit*/



    public Commit(String msg, Branches b, Staging s) {
        message = msg;
        date = new Date();
        parent = b.branchMap.get(b.head);
        branchName = b.head;
        files = setCommitFiles(parent, s);
        workingHere = System.getProperty("user.dir");
        try {
            commitID = Utils.sha1(files.keySet() + parent + message + date);
            commitfilesdir = Utils.join(gitletDir, "commits", commitID).getPath();
            File dir = new File(commitfilesdir);
            dir.mkdirs();
        } catch (NullPointerException excp) {
            commitID = Utils.sha1(parent + message + date);
        }
    }

    public static void makeCommit(Staging s, Branches b, String msg) {
            if (msg.equals("")) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            } else if (s.stage.entrySet().isEmpty() && s.rm.isEmpty()) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }
            Commit c = new Commit(msg, b, s);
            Commit.readFiles(c, s);
            b.branchMap.replace(b.head, c.getID());
            Commit.writeCommit(c, b);
            Staging.clearStage(s);
            s.rm.clear();

    }

    public Commit(String msg) {
        message = msg;
        date = new Date();
        parent = null;
        branchName = "master";
        files = null;
        workingHere = System.getProperty("user.dir");
        commitID = Utils.sha1(message + date);
        File dir = new File(Utils.join(gitletDir, "commits").getPath());
        dir.mkdirs();
        File comDir = new File(Utils.join(gitletDir, "commits", commitID).getPath());
        comDir.mkdirs();
    }

//    public HashMap createCopy(String cID) {
//        Commit currCommit = readCommit(cID);
//        HashMap<String, String> copy = new HashMap<>();
//        for (HashMap.Entry<String, String> entry : currCommit.files.entrySet()) {
//            copy.put(entry.getKey(), entry.getValue());
//        }
//        return copy;
//    }


    public String getDate() { //for log
        return dateFormat.format(this.date);
    }

    public String getMessage() { //for log
        return this.message;
    }

    public String getParent() {
        return this.parent;
    }

    public String getID() {
        return this.commitID;
    }



    public void serializeCommit(String msg) {
        //serialization FILEOUT body
    }

    //checkout implementation


    public static Commit readCommit(String commitID) {
        Commit currCommit;
        File inFile = new File(Utils.join(gitletDir, "commits", commitID, commitID).getPath());
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            currCommit = (Commit) inp.readObject();
            inp.close();

        } catch (IOException | ClassNotFoundException e) {
            currCommit = null;
        }
        return currCommit;
    }

    public static void writeCommit(Commit c, Branches b) { //converts commit object to ByteArray, which is then serialized and saved
        b.messageMap.put(c.getID(), c.message);
        File file = null;
        if (c.commitfilesdir == null) {
            file = new File(Utils.join(gitletDir, "commits", c.commitID, c.commitID).getPath());
        } else {
            file = new File(Utils.join(c.commitfilesdir, c.commitID).getPath());
        }
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(c);
            objectStream.close();
            byte[] commitByteArr = stream.toByteArray();
            gitlet.Utils.writeContents(file, commitByteArr);
        } catch (IOException excp) {
            Error.failSerializing();
        }
    }
    public static void readFiles (Commit c, Staging s) {
        assert s.stage != null;
        for (String file : (Set<String>) s.stage.keySet()) {
            File addFile = new File(Utils.join(c.workingHere, file).getPath());
            Path source = Paths.get(addFile.getAbsolutePath());
            File storedFile = new File(Utils.join(c.commitfilesdir, addFile.getName()).getPath());
            Path target = Paths.get(storedFile.getAbsolutePath());
            try {
                Files.copy(source, target, REPLACE_EXISTING, COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /* Creates a shallow copy of parent's files. Then checks the files in
    * staging area, checks to see if those keys exist in parent's map and replaces.
    * Else, just puts in. Also checks if in rm list.*/
    public HashMap setCommitFiles(String par, Staging s) {
        if (s.stage == null) {
            System.out.println("No changes added to the commit");
            System.exit(0);
        }
        HashMap<String, String> parentfiles = new HashMap<>();
        if (readCommit(par) == null) {
            for (String key : (Set<String>) s.stage.keySet()) {
                parentfiles.put(key, (String) s.stage.get(key)); //adds new files
                if (s.rm.contains(key)) {
                    parentfiles.remove(key);
                }
            }
            return parentfiles;
        } else {
            parentfiles = readCommit(par).files;
            if (parentfiles == null) {
                return s.stage;// i changed this t o stage
            }
            Iterator ParentCommitFiles = parentfiles.keySet().iterator();
            while (ParentCommitFiles.hasNext()) {
                String key = (String) ParentCommitFiles.next();
                boolean rmkey = s.rm.contains(key);
                if (rmkey) {
                    ParentCommitFiles.remove();
                }
            }
                for (String key : (Set<String>) s.stage.keySet()) { //accounts for stage
                    boolean rmkey = s.rm.contains(key);
                    if (parentfiles.containsKey(key)) { //check if files have been modified, replaces them if so
                        if (!parentfiles.get(key).equals(s.stage.get(key))) {
                            parentfiles.replace(key, (String) s.stage.get(key));
                        }
                        if (rmkey) {
                            parentfiles.remove(key);
                        }
                    } else {
                        parentfiles.put(key, (String) s.stage.get(key)); //adds new files
                        if (rmkey) {
                            parentfiles.remove(key);
                        }
                    }

                }
                return parentfiles;
            }
        }


    public HashMap getCommitFiles() {
        return this.files;
    }



        /* Several if cases which check if staging area isn't null,
        compare modified files to ones existing in currCommit via SHA-1 comparator (done),
        check if files in staging area and currCommit have been marked by rm,
        finally instantiating files as modified version of parent's files HashMap (done),
        replacing old files with newer versions, adding
        completely new files, and not containing files marked rm.
         */

        /* Method for mutating branches map so that current branch's pointer + head
        are reassigned to currCommit (needs to happen after serialization,
        have to find a way to do this outside of commit instantiation, maybe
        commit serialization should occur / this method should be in Tree class?)
         */

}