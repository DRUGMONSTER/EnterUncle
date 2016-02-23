import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logg{
	public static Logger logger;
	public static boolean on;

	@SuppressWarnings("UnusedDeclaration")
	public static void init(){
		on = true;
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.ALL);
		try{
			logger.addHandler(new FileHandler("E:\\EnterUncle.log"));
		}catch(IOException e1){
			try{
				logger.addHandler(new FileHandler("C:\\Users\\RZamberg\\Desktop\\EnterUncle.log"));
			}catch(IOException e2){
				System.out.println("Problem with creating the log file");
			}
		}

		//Suppress logging output to the console
		logger.setUseParentHandlers(false);
	}

	//bad stuff
	public static void severe(String msg){
		if(on)
			logger.severe(msg);
	}

	//good stuff
	public static void fine(String msg){
		if(on)
			logger.fine(msg);
	}

	//better stuff
	public static void good(String msg){
		if(on)
			logger.finer(msg);
	}

	//neutral stuff
	public static void info(String msg){
		if(on)
			logger.info(msg);
	}

	//potentially dangerous stuff
	public static void warning(String msg){
		if(on)
			logger.warning(msg);
	}
}
