///**
// * In init() create all relevant class directories.
// * 		Init takes body of Gitlet() and uses them, saving them as
// * 		subdirectories.
// * After if check to see if .gitlet is initialized: if it is,
// * in MAIN, use reading/serializing, save each class as an instance
// * which we will call and edit within the commands.
// */
//
//package gitlet;
//
//import java.io.*;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//
///**When init command is invoked, a new Initialized object will be instiantiated. */
//public class Gitlet {
//	public static gitlet.Commit.InitialCommit initial;
//	public static gitlet.Branches master;
//	public static boolean existingDirectory = false;
//	public static final String gitletDir = gitlet.Gitlet.currentDir;
//	public static File gitletDirectory = new File(".gitlet");
//    public static String currentDir = gitletDirectory.getAbsolutePath();
//	public static final String dir = System.getProperty("user.dir");
//	public static Staging stage;
//
//	//put these implementations in other files and reread them in MAIN. but don't turn main into this lol.
//
//	public static List<String> rm = new ArrayList<>();
//
//    /** Method will create a new gitlet directory if one does not already exist inside the current directory. */
//    public static void init() {
//    	try {
//			if (existingDirectory) {
//				throw new IOException("Directory already exists!");
//			}
//			gitletDirectory.mkdir();
//			initial = new Commit.InitialCommit();
//			master = new Branches(initial);
//			existingDirectory = true;
//			stage = new Staging();
//
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//	}
//
//	/**If a gitlet directory has already been instantiated, a message will be printed and the program will exit.*/
//
//
//	public static void directory() throws IOException{
//        //Path variable gets the current Path.
//        //Path path = FileSystems.getDefault().getPath(".");
//		String path = currentDir;
//        /**Making an iterator to iterate through each name element in the path.
//         Taken from https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html#iterator(). */
//        //Iterator<Path> pathiterator = path.iterator();
//        //boolean in_gitlet = false;
//        //String dirPath = "";
//        //while (pathiterator.hasNext()) {
//        //    String p = pathiterator.next().toString();
//        //    dirPath += p;
//        //}
//        /* //Throws Exception if not in gitlet directory. Needs an Error message though*/
//        if (!path.contains("gitlet")) {
//            throw new IOException();
//        }
//    }
//
//
//}