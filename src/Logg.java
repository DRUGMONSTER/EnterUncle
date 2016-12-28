import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class Logg{
	private static Logger logger; // perhaps make not static, check if you can have an on/off switch with a regular logger.
	private static boolean on;

	static{
		on = true;
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.ALL);
		try{
			logger.addHandler(new FileHandler("C:\\EnterUncle.log"));
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
	static void severe(String msg){
		if(on)
			logger.severe(msg);
	}

	//good stuff
	static void fine(String msg){
		if(on)
			logger.fine(msg);
	}

	//better stuff
	static void good(String msg){
		if(on)
			logger.finer(msg);
	}

	//neutral stuff
	static void info(String msg){
		if(on)
			logger.info(msg);
	}

	//potentially dangerous stuff
	static void warning(String msg){
		if(on)
			logger.warning(msg);
	}
}
