//package gitlet;
//
//import org.junit.Test;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.File;
//
//import static gitlet.Main.gitletDirectory;
//import static org.junit.Assert.*;
//
///* The suite of all JUnit tests for the gitlet package.
//   @author
// */
//public class UnitTest {
//
//    @Test
//    public void initTest() {
//        gitletDirectory.mkdir();
//        Commit initial = new Commit("Initial commit.");
//        Branches b = new Branches(initial);
//        Commit.writeCommit(initial, b);
//        boolean existingDirectory = true;
//        Staging s = new Staging();
//        Staging.writeStaging(s);
//        Branches.writeBranches(b);
//    }
//
//    @Test
//    public void addTest() {
//        Branches branches = Branches.readBranches();
//        Staging staging = Staging.readStaging();
//
//        Staging.read_and_hash(staging, "magic_word.txt");
//        Staging.writeStaging(staging);
//    }
//
//    @Test
//    public void commitTest() {
//        Branches branches = Branches.readBranches();
//        Staging staging = Staging.readStaging();
//
//        Commit c = new Commit("First commit", branches, staging);
//        Commit.writeCommit(c, branches);
//        branches.branchMap.put(branches.head, c.getID());
//        Staging.writeStaging(staging);
//    }
//
//    @Test
//    public void checkoutTest() {
//        Branches branches = Branches.readBranches();
//        Staging staging = Staging.readStaging();
//
//        Tree.checkoutFile("magic_word.txt", branches);
//
//        Staging.read_and_hash(staging, "wug.txt");
//        Staging.read_and_hash(staging, "magic_word.txt");
//        Commit c = new Commit("magic txt has been changed so that we can checkout", branches, staging);
//
//
//        Tree.checkoutFile("magic_word.txt", branches);
//
//
//
//    }
//}
//
//
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////}
////
////@Test
////public void initTest() {
////        Gitlet.init();
////        //Gitlet.stage.writeStaging();
////        Gitlet.master.writeBranches();
////        }
////
////@Test
////public void addTest() {
////        Gitlet.master.readBranches();
////        Gitlet.stage.read_and_hash("wug.txt");
////        Gitlet.stage.read_and_hash("wug2.txt");
////        Gitlet.stage.writeStaging();
////        }
////
////@Test
////public void initAddCommitTest() {
////        Gitlet.init();
////        Gitlet.stage.read_and_hash("wug.txt");
////        //Staging.writeStage("wug.txt");
////        Gitlet.stage.read_and_hash("wug2.txt");
////        //Staging.writeStage("wug2.txt");
////        Commit c = new Commit("this is first commit with two files");
////        c.writeCommit();
////        Gitlet.master.branchMap.put(Gitlet.master.head, c.getID());
////        Gitlet.stage.read_and_hash("lol.rtf");
////        Commit c1 = new Commit("this is the second commit with one file");
////        c1.writeCommit();
////        Gitlet.master.branchMap.put(Gitlet.master.head, c1.getID());
////        }
////
////@Test
////public void commitTest() {
////        Gitlet.stage.readStaging();
////        //Branches b = new Branches(new Commit.InitialCommit());
////        Gitlet.master.readBranches();
////        Commit c = new Commit("this is the first commit with two files"); //inputCommand will instantiate
////        c.writeCommit();
////        Gitlet.master.branchMap.put(Gitlet.master.head, c.getID());
////        }
////
////@Test
////public void rmTest() {
////        Tree.rm("wug.txt");
////        }
////
////@Test
////public void logTest() {
////        Gitlet.master.log();
////        }
////
////@Test
////public void globalLogTest() {
////        Tree.globalLog();
////        }
////
////@Test
////public void findTest() {
////        Gitlet.master.findCommit("this is first commit with two files");
////        }
////
////@Test
////public void statusTest() {
////        Tree.status();
////        }
////
////@Test
////public void checkoutFileTest() {
////
////        }
////        }
////
////
