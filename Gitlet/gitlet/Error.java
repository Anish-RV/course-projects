package gitlet;

import java.io.Serializable;

/** Class will return an error if input is not supplied correctly. */
public class Error implements Serializable {

	/**If no command is entered, a message will be print and the program will exist.*/
	public static void noArgs(String command) {
		if (command.compareTo("commit") == 0) {
			System.out.println("Please enter a commit message.");
		} else {
			System.out.println("Please enter a command.");
		}
		System.exit(0);
	}

	/**If command does not exist, a message will be printed and the program will exist */
	public static void notExisting() {
		System.out.println("No command with that name exists.");
		System.exit(0);
	}

	/**If operands are not inputted correctly,
     *a message will be printed and the program will exist.*/
	public static void incorrectOperands() {
		System.out.println("Incorrect operands.");
		System.exit(0);
	}

	/**If the command is not inputted in an initialized gitlet directory,
     * a message will be printed. */
	public static void notInitalized() {
	    System.out.println("Not in an initialized gitlet directory.");
	    System.exit(0);
	}

	/** If command tries to create a new branch whose name already exists,
     * a message will be printed. */
	public static void branchDuplicate() {
		System.out.println("A branch with that name already exists.");
		System.exit(0);
	}

	public static void noFileName() {
		System.out.println("Invalid file name.");
		System.exit(0);
	}

	public static void failSerializing() {
		System.out.println("Internal error serializing commit.");
		System.exit(0);
	}

	public static void existingDir() {
		System.out.println("A gitlet version-control system already exists in the current directory.");
		System.exit(0);
	}

	public static void wrongDir() {
		System.out.println("You are in the wrong directory. Please move to .gitlet.");
		System.exit(0);
	}
}
