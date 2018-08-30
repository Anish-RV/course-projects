package gitlet;
//import java.io.*;
//
//
//import java.util.*;

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//The staging class will represent the staging area for our files
//  (only one object will ever be instantiated in the Initialize class)
public class Staging implements Serializable {
    public HashMap stage;
    public List<String> rm = new ArrayList<>();
    //public static final String gitletDir = gitlet.Gitlet.currentDir;
    public final String dir = System.getProperty("user.dir");
    //public static final String stageDir = gitletDir + "\\" + "staging";
    public static String gitletDirectory = Main.gitletDir;
    public static final String STAGEDIR = Utils.join(gitletDirectory, "staging").getPath();
    public static String workingDir = System.getProperty("user.dir");


    public Staging(String... fileNames) {
        File stagingDir = new File(STAGEDIR);
        stagingDir.mkdirs();
        stage = new HashMap<>();
//        for (int i = 0; i < fileNames.length; i++) {
//            File file = new File(dir + "/" + fileNames[i]);
//            String file_SHA = Utils.sha1(Utils.readContents(file));
//            staging.put(fileNames[i], file_SHA);
//        }
    }

    public Staging() {
        File stagingDir = new File(STAGEDIR);
        stagingDir.mkdirs();
        stage = new HashMap<>();
    }

    public static void main(String[] args) {
        new Staging("wug.txt");
    }


    //Checks if the file already exists in the Map. If so, checks
    // if it is the same file and replaces
    //it if not. Else, adds the filename as key, SHA-1 id as
    // value into the HashMap.
    public static void addFile(Staging staging, String fileName, String hash) {
        try {
            if (staging.stage.containsKey(fileName)) {
                if (staging.stage.get(fileName).equals(hash)) {
                    return;
                } else {
                    staging.stage.replace(fileName, hash);
                }
            } else {
                staging.stage.put(fileName, hash);
            }
        } catch (NullPointerException ne) {
            staging.stage.put(fileName, hash);
        }
    }

    /**
     * Method will
     */
    public static void read_and_hash(Staging staging, String fileName) { //#############
        File file = new File(Utils.join(workingDir, fileName).getPath());
        String fileHash = Utils.sha1(Utils.readContents(file));
        addFile(staging, fileName, fileHash);
    }

    //Clears Staging Area.
    public static void clearStage(Staging s) {
        s.stage.clear();
    }

    //Unstages a file from the staging area by removing it from the HashMap.
    public static void unStage(Staging s, String fileName) {
        s.stage.remove(fileName);
    }

    public static void rmStage(Staging s, String fileName) {
        if (s.rm.contains(fileName) && s.stage.containsKey(fileName)) {
            s.stage.remove(fileName);
        } else {
            return;
        }
    }

    public static HashMap returnStage(Staging s) {
        return s.stage;
    }

    public static void writeStaging(Staging s) {
        File outFile = new File(STAGEDIR + "\\stagingItems");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(s);
            out.close();
        } catch (IOException excp) {
            System.out.println(excp + " Can't write this stage!");
            System.exit(0);
        }
    }

    public static Staging readStaging() {
        Staging staging;
        File inFile = new File(STAGEDIR + "\\stagingItems");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            staging = (Staging) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            System.out.println(excp + " You can't read that stage! Illiterate... smh");
            staging = null;
        }
        return staging;
    }


//    public void writeStage() {
//        Iterator stageIt = Gitlet.stage.stage.entrySet().iterator();
//        String s;
//        while (stageIt.hasNext()) {
//            HashMap.Entry pair = (HashMap.Entry) stageIt.next();
//            s = (String) pair.getKey();
//            File outFile = new File(stageDir + "\\" + s);
//            try {
//                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
//                out.writeObject(this);
//                out.close();
//            } catch (IOException excp) {
//                System.out.println(excp + "Can't write this stage!");
//                System.exit(0);
//            }
//
//        }
//    }

//    public static void writeStaging(Staging s) {
//        Staging staged = s;
//        Iterator stageIt = Gitlet.stage.stage.entrySet().iterator(); //chg
//        while (stageIt.hasNext()) {
//            HashMap.Entry pair = (HashMap.Entry) stageIt.next();
//            String pairString = pair.toString();
//            File file = new File(stageDir + "\\" + pair.toString());
//            try {
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                ObjectOutputStream objectStream = new ObjectOutputStream(stream);
//                objectStream.writeObject(pairString);
//                objectStream.close();
//                byte[] entryByteArr = stream.toByteArray();
//                Utils.writeContents(file, entryByteArr);
//            } catch (IOException excp) {
//                Error.failSerializing();
//            }
//        }
//    }

    /**public void readStage() {
     Staging staged;
     File inFile = new File(stageDir);
     try {
     ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
     staged = (Staging) inp.readObject();
     inp.close();
     } catch (IOException | ClassNotFoundException excp) {
     staged = null;
     System.out.println(excp + "There was an error reading the stage!");
     System.exit(0);
     }
     }*/

//     public static Staging readStaging() {
//        /**Staging stg; //= this;
//        Iterator stageIt = Gitlet.stage.stage.entrySet().iterator();
//        while (stageIt.hasNext()) {
//            HashMap.Entry pair = (HashMap.Entry) stageIt.next();
//            String fileName = stageDir +  "\\" + pair.toString();
//            readFile(fileName);
//        }*/
//        Staging st = null;
//        File stageDirectory = new File(stageDir);
//        File[] stageListing = stageDirectory.listFiles();
//        if (stageListing != null) {
//            for (File child : stageListing) {
//                readFile(child.getName());
//                stage.put()
//            }
//        } else {
//            System.out.println("Your stage is null, dummy!");
//            System.exit(0);
//        }
//    }

//    public static void readFile(String fileName) {
//        Staging staged;
//        File inFile = new File(Gitlet.stage.stageDir + "/" + fileName);
//        try {
//            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
//            staged = (Staging) inp.readObject();
//            inp.close();
//        } catch (IOException | ClassNotFoundException excp) {
//            staged = null;
//            System.out.println(excp + " There was an error reading the stage!");
//            System.exit(0);
//        }
//    }
//
//    public static void writeFile(String fileName) {
//        File outFile = new File(Gitlet.stage.stageDir + "/" + fileName);
//        try {
//            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
//            out.writeObject(Gitlet.stage);
//            out.close();
//        } catch (IOException excp) {
//            System.out.println(excp + " Can't write this object!");
//            System.exit(0);
//        }
//    }

//    public static byte[] byteBlob(File f) {
//        try {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
//            objectStream.writeObject(f);
//            objectStream.close();
//            return stream.toByteArray();
//        } catch (IOException excp) {
//            System.out.println(excp + " Failed to byte it!");
//            System.exit(0);
//            return null;
//        }
//    }

}

