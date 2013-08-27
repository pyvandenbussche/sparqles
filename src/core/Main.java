package core;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.cli.CLIObject;
import utils.cli.SPARQLES;

/**
 * This Main class parses all available classes in the JVM for command line objects having the specific PACKAGE_PREFIX.
 * 
 * @author juum
 *
 */
public class Main {


	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final String PACKAGE_PREFIX = "utils.cli";
	private static final String PATH_PREFIX = "/utils/cli";

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		try {
			if (args.length < 1) {
				StringBuffer sb = new StringBuffer();
				sb.append("where <util> one of");
				Class [] classes = getClasses(PACKAGE_PREFIX);

				for(Class c: classes){
					log.info("try to load {}",c.getName());
//					PACKAGE_PREFIX+"."+c.getSimpleName()
					Class cls = Main.class.getClassLoader().loadClass(c.getName());
					if(CLIObject.class.isAssignableFrom(cls) && !cls.getSimpleName().equals("CLIObject")){
						if(cls.getName().replaceAll(PACKAGE_PREFIX, "").equals("."+cls.getSimpleName())){
							CLIObject o = (CLIObject) cls.newInstance(); 
							sb.append("\n\t").append(o.getCommand()).append(" -- ").append(o.getDescription());
						}
					}
				}
				usage(sb.toString());
			}
			CLIObject cli = (CLIObject)Class.forName(PACKAGE_PREFIX + "."+args[0]).newInstance();
			cli.run(Arrays.copyOfRange(args, 1, args.length));
			System.exit(0);	
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
	private static void usage(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}
	private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		String uri;
		ArrayList<Class> classes = new ArrayList<Class>();
		try {
			uri = Main.class.getResource(PATH_PREFIX).toURI().toASCIIString();
			if(uri.startsWith("jar:file:")){
				classes = classesFromJar(uri);
			}
			else{
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				String path = packageName.replace('.', '/');
				Enumeration<URL> resources = classLoader.getResources(path);
				List<File> dirs = new ArrayList<File>();
				while (resources.hasMoreElements()) {
					URL resource = resources.nextElement();
					dirs.add(new File(resource.getFile()));
				}
				for (File directory : dirs) {
					classes.addAll(findClasses(directory, packageName));
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		//		
		//		
		return classes.toArray(new Class[classes.size()]);
	}
	private static ArrayList<Class> classesFromJar(String uri) throws FileNotFoundException, IOException, ClassNotFoundException {
		ArrayList<Class> classes = new ArrayList<Class>();
		String jarURI = uri.substring("jar:file:".length(),uri.lastIndexOf("!"));
		JarInputStream jarFile = new JarInputStream(new FileInputStream(jarURI));
		JarEntry jarEntry;
		while (true) {
			jarEntry = jarFile.getNextJarEntry();
			//            System.out.println(jarEntry);
			if (jarEntry == null) {
				break;
			}
			if ((jarEntry.getName().startsWith(PACKAGE_PREFIX.replace(".", "/"))) &&
					(jarEntry.getName().endsWith(".class"))) {
				String classEntry = jarEntry.getName().replaceAll("/", "\\.");
				classes.add(Class.forName(classEntry.substring(0, classEntry.indexOf(".class"))));
			}
		}
		return classes;
	}
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				if(!file.getName().equals("CLIObject.class"))
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}
}
