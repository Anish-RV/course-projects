package gitlet;

import java.io.*;
import java.util.HashMap;

import static gitlet.Commit.readCommit;

public class Branches implements Serializable {

    public HashMap<String, String> branchMap;
    public Error e = new Error();
    public String head; //head stores the SHA of our most recent commit.
    public HashMap<String, String> messageMap;
    public static final String gitletDir = Main.gitletDir;
    public final String dir = System.getProperty("user.dir");
    public static final String branchDir = Utils.join(gitletDir, "branches").getPath();


    /* Creates a new HashMap BRANCHMAP with default branch being master
        and make our head store the name of the master. */
    public Branches(gitlet.Commit initialCommit) {
        branchMap = new HashMap<>();
        branchMap.put("master", initialCommit.getID());
        head = "master";
        messageMap = new HashMap<>();
        messageMap.put(initialCommit.getID(), initialCommit.getMessage());
        File branchesDir = new File(branchDir);
        branchesDir.mkdirs();
    }


    public void findCommit(String msg) {
        if (messageMap.containsValue(msg)) {
            for (String id : messageMap.keySet()) {
                if (messageMap.get(id).equals(msg)) {
                    System.out.println(id);
                }
            }
        } else {
            System.out.print("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Method finds where a split point occurs as soon as the split occurs.
     */
    public static void addBranch(Staging s, Branches b, String name) {
        Commit headCommit = readCommit(b.branchMap.get(b.head));
        if (!b.branchMap.containsKey(name)) {
            b.branchMap.put(name, headCommit.getID());
        } else {
            Error.branchDuplicate();
        }
    }

    /* Runs a loop from the current commit(pointed to by HEAD in the branchmap) till the parent
    is null. After each iteration, currcommit is re-assigned to its parent.
     */
    public void log() {
        String headtemp = head;
        gitlet.Commit currcommit = readCommit(branchMap.get(headtemp));
        while (currcommit != null) {
            System.out.println("===");
            System.out.println("Commit " + currcommit.getID());
            System.out.println(currcommit.getDate());
            System.out.println(currcommit.getMessage());
            System.out.println();
            if (currcommit.getParent() != null) {
                currcommit = readCommit(currcommit.getParent());
            } else {
                break;
            }
        }
    }

    public static void writeBranches(Branches b) {
        File outFile = new File(branchDir + "/branches");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(b);
            out.close();
        } catch (IOException excp) {
            System.out.println(excp + " Can't write these branches, suckah!");
            System.exit(0);
        }
    }

    public static Branches readBranches() {
        Branches br = null;
        File inFile = new File(branchDir + "/branches");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            br = (Branches) inp.readObject();
            inp.close();
            return br;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e + " Can't read branches? Error! C'mon.");
            return br;
        }
    }


    public void rmBranch(String branchName) {
        branchMap.remove(branchName);
    }
}






