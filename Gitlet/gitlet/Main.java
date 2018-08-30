package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.util.NoSuchElementException;


/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {
    public static String workingDir = System.getProperty("user.dir");
    public static String gitletDir = Utils.join(workingDir, ".gitlet").getPath();
    public static File gitletDirectory = new File(".gitlet");
    public static String currentDir = gitletDirectory.getAbsolutePath();


    /* Usage: java gitlet.Main ARGS, where ARGS contains
           <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            String command = args[0];
            if (command.compareTo("init") == 0) {
                if (Files.exists(gitletDirectory.toPath())) {
                    System.out.println("A gitlet version-control system already " +
                            "exists in the current directory.");
                    System.exit(0);
                }
                gitletDirectory.mkdirs();
                Commit initial = new Commit("initial commit");
//                Commit initial = new Commit("initial commit");
                Branches b = new Branches(initial);
                Staging s = new Staging();
                Staging.writeStaging(s);
                Branches.writeBranches(b);
                Commit.writeCommit(initial, b);
            }

            Branches branches = Branches.readBranches();
            Staging staging = Staging.readStaging();

            if (command.compareTo("add") == 0) {
                Tree.adder(staging, branches, args[1]);
                Staging.writeStaging(staging);
                Branches.writeBranches(branches);
            } else if (command.compareTo("commit") == 0) {
                Commit.makeCommit(staging, branches, args[1]);
                Branches.writeBranches(branches);
                Staging.clearStage(staging);
                Staging.writeStaging(staging);
            } else if (command.compareTo("rm") == 0) {
                Tree.rm(staging, branches, args[1]);
                Branches.writeBranches(branches);
                Staging.writeStaging(staging);
            } else if (command.compareTo("log") == 0) {
                branches.log();
            } else if (command.compareTo("global-log") == 0) {
                gitlet.Tree.globalLog(branches);
            } else if (command.compareTo("find") == 0) {
                branches.findCommit(args[1]);
            } else if (command.compareTo("status") == 0) {
                Tree.status(branches, staging);
            } else if (command.compareTo("checkout") == 0) {
                if (args.length == 3) {
                    Tree.checkoutFile(args[2], branches);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        Error.incorrectOperands();
                    } else {
                        gitlet.Tree.checkoutID(args[1], args[3]);
                    }
                } else {
                    gitlet.Tree.checkoutBranch(branches, staging, args[1]);
                    Branches.writeBranches(branches);
                    Staging.writeStaging(staging);
                }
            } else if (command.compareTo("branch") == 0) {
                Branches.addBranch(staging, branches, args[1]);
                Branches.writeBranches(branches);
                Staging.writeStaging(staging);
            } else if (command.compareTo("rm-branch") == 0) {
                branches.rmBranch(args[1]);
                Branches.writeBranches(branches);
                Staging.writeStaging(staging);
            } else if (command.compareTo("reset") == 0) {
                gitlet.Tree.reset(branches, staging, args[1]);
                Branches.writeBranches(branches);
                Staging.writeStaging(staging);
            } else if (command.compareTo("merge") == 0) {
                gitlet.Merge.merge(args[1], staging, branches);
                Branches.writeBranches(branches);
                Staging.writeStaging(staging);
            } //Need a way to throw an exception if a wrong command is given. Like 'init1"
//        } catch (FileNotFoundException e) {
//            Error.notExisting();
//        } catch (IOException e) {
//           gitlet.Error.existingDir();
        } catch (IllegalArgumentException e) {
            gitlet.Error.incorrectOperands();
        } catch (ArrayIndexOutOfBoundsException e) {
            gitlet.Error.noArgs(args[0]);
        } catch (AssertionError e) {
            Error.notInitalized();
        } catch (NoSuchElementException e) {
            Error.existingDir();
        }
    }

}
