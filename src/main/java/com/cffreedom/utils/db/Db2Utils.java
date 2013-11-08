package com.cffreedom.utils.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cffreedom.beans.DbConn;
import com.cffreedom.beans.DbDriver;
import com.cffreedom.beans.DbType;
import com.cffreedom.exceptions.InfrastructureException;
import com.cffreedom.exceptions.ProcessingException;
import com.cffreedom.exceptions.ValidationException;
import com.cffreedom.utils.SystemUtils;
import com.cffreedom.utils.file.FileUtils;

/**
 * Original Class: com.cffreedom.utils.db.Db2Utils
 * @author markjacobsen.net (http://mjg2.net/code)
 * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
 * 
 * Free to use, modify, redistribute.  Must keep full class header including 
 * copyright and note your modifications.
 * 
 * If this helped you out or saved you time, please consider...
 * 1) Donating: http://www.communicationfreedom.com/go/donate/
 * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
 * 3) Linking to: http://visit.markjacobsen.net
 * 
 * Changes:
 * 2013-09-01 	markjacobsen.net 	Created
 * 2013-10-01	MarkJacobsen.net 	Added connectToAlias option necessary for catalog entries on remote servers
 * 2013-10-17 	MarkJacobsen.net 	Improvements for when running on windows vs *nux
 */
public class Db2Utils
{
	public final static String DRIVER = DbUtils.getDefaultDriver(DbType.DB2);
	public final static String DRIVER_JCC = DbDriver.DB2_JCC.value;
	public final static String DRIVER_APP = DbDriver.DB2_APP.value;
	public final static String DRIVER_NET = DbDriver.DB2_NET.value;
	
	private static final Logger logger = LoggerFactory.getLogger(Db2Utils.class);
	private static final String TYPE_IMPORT = "import";
	private static final String TYPE_EXPORT = "export";
	private static final String TYPE_RUNSTATS = "runstats";
	private static final String TYPE_REORG = "reorg";
	private static final String TYPE_TRUNCATE = "truncate";
	private static final String TYPE_RAW = "raw";
	
	public static void exportToFile(String fileDirectory, String name, Map<String, String> nameSqlMap, DbConn dbconn, boolean connectToAlias) throws ProcessingException
	{		
		try
		{
			execCommands(TYPE_EXPORT, fileDirectory, name, nameSqlMap, dbconn, connectToAlias);
		}
		catch (ProcessingException | ValidationException | InfrastructureException e)
		{
			logger.error("Error during processing: " + e.getMessage(), e);
			throw new ProcessingException(e);
		}
	}
	
	public static void importFromFile(String fileDirectory, String name, Map<String, String> nameSqlMap, DbConn dbconn, boolean connectToAlias) throws ProcessingException
	{
		try
		{
			execCommands(TYPE_IMPORT, fileDirectory, name, nameSqlMap, dbconn, connectToAlias);
		}
		catch (ProcessingException | ValidationException | InfrastructureException e)
		{
			logger.error("Error during processing: " + e.getMessage(), e);
			throw new ProcessingException(e);
		}
	}
	
	public static void runStatsOnTables(String fileDirectory, String name, ArrayList<String> tables, DbConn dbconn, boolean connectToAlias) throws ProcessingException
	{
		Map<String, String> nameSqlMap = new LinkedHashMap<String, String>();
		for (String table : tables)
		{
			nameSqlMap.put(table, table);
		}
		
		try
		{
			execCommands(TYPE_RUNSTATS, fileDirectory, name, nameSqlMap, dbconn, connectToAlias);
		}
		catch (ProcessingException | ValidationException | InfrastructureException e)
		{
			logger.error("Error during processing: " + e.getMessage(), e);
			throw new ProcessingException(e);
		}
	}
	
	public static void reorgTables(String fileDirectory, String name, ArrayList<String> tables, DbConn dbconn, boolean connectToAlias) throws ProcessingException
	{
		Map<String, String> nameSqlMap = new LinkedHashMap<String, String>();
		for (String table : tables)
		{
			nameSqlMap.put(table, table);
		}
		
		try
		{
			execCommands(TYPE_REORG, fileDirectory, name, nameSqlMap, dbconn, connectToAlias);
		}
		catch (ProcessingException | ValidationException | InfrastructureException e)
		{
			logger.error("Error during processing: " + e.getMessage(), e);
			throw new ProcessingException(e);
		}
	}
	
	public static void truncateTables(String fileDirectory, String name, ArrayList<String> tables, DbConn dbconn, boolean connectToAlias) throws ProcessingException
	{
		Map<String, String> nameSqlMap = new LinkedHashMap<String, String>();
		for (String table : tables)
		{
			nameSqlMap.put(table, table);
		}
		
		try
		{
			execCommands(TYPE_TRUNCATE, fileDirectory, name, nameSqlMap, dbconn, connectToAlias);
		}
		catch (ProcessingException | ValidationException | InfrastructureException e)
		{
			logger.error("Error during processing: " + e.getMessage(), e);
			throw new ProcessingException(e);
		}
	}
	
	public static void grantAccessToTables(String fileDirectory, String name, ArrayList<String> tables, DbConn dbconn, boolean connectToAlias, String grantTo, boolean select, boolean insert, boolean update, boolean delete) throws ProcessingException
	{
		Map<String, String> nameSqlMap = new LinkedHashMap<String, String>();
		for (String table : tables)
		{
			if (select == true) { nameSqlMap.put(table + ".SELECT", "GRANT SELECT ON TABLE " + table + " TO " + grantTo + " WITH GRANT OPTION;"); }
			if (insert == true) { nameSqlMap.put(table + ".INSERT", "GRANT INSERT ON TABLE " + table + " TO " + grantTo + " WITH GRANT OPTION;"); }
			if (update == true) { nameSqlMap.put(table + ".UPDATE", "GRANT UPDATE ON TABLE " + table + " TO " + grantTo + " WITH GRANT OPTION;"); }
			if (delete == true) { nameSqlMap.put(table + ".DELETE", "GRANT DELETE ON TABLE " + table + " TO " + grantTo + " WITH GRANT OPTION;"); }
		}
		
		try
		{
			execCommands(TYPE_RAW, fileDirectory, name, nameSqlMap, dbconn, connectToAlias);
		}
		catch (ProcessingException | ValidationException | InfrastructureException e)
		{
			logger.error("Error during processing: " + e.getMessage(), e);
			throw new ProcessingException(e);
		}
	}
	
	private static void execCommands(String type, String fileDirectory, String name, Map<String, String> nameSqlMap, DbConn dbconn, boolean connectToAlias) throws ProcessingException, ValidationException, InfrastructureException
	{
		int execRet = -1;
		ArrayList<String> filesToDelete = new ArrayList<String>();
		
		if (
			(type.equalsIgnoreCase(TYPE_EXPORT) == false) &&
			(type.equalsIgnoreCase(TYPE_IMPORT) == false) &&
			(type.equalsIgnoreCase(TYPE_RUNSTATS) == false) &&
			(type.equalsIgnoreCase(TYPE_REORG) == false) &&
			(type.equalsIgnoreCase(TYPE_TRUNCATE) == false) &&
			(type.equalsIgnoreCase(TYPE_RAW) == false)
			)
		{
			throw new ValidationException("Invalid type: " + type);
		}
		
		logger.debug("File Directory for {} exection: {}", type, fileDirectory);
		
		String dbCommandFile = FileUtils.buildPath(fileDirectory, name + "." + type + ".cmd");
		String dbCommandLogFile = FileUtils.buildPath(fileDirectory, name + "." + type + ".cmd.log");
		String lastDataLogFileName = null;
		
		filesToDelete.add(dbCommandFile);
		
		if (FileUtils.fileExists(dbCommandFile) == true)
		{
			logger.debug("Deleting old command file: {}", dbCommandFile);
			FileUtils.deleteFile(dbCommandFile);
		}
		
		if (FileUtils.fileExists(dbCommandLogFile) == true)
		{
			logger.debug("Deleting old command log file: {}", dbCommandLogFile);
			FileUtils.deleteFile(dbCommandLogFile);
		}
		
		logger.debug("Creating command file");
		ArrayList<String> lines = new ArrayList<String>();
		String connectTo = dbconn.getDb();
		if ((connectToAlias == true) && (dbconn.getAlias() != null)) { connectTo = dbconn.getAlias(); }
		lines.add("connect to "+connectTo+" user "+dbconn.getUser()+" using \""+dbconn.getPassword()+"\";");
		lines.add("");
		for (String curName : nameSqlMap.keySet())
		{
			String sql = nameSqlMap.get(curName);
			String dataFileName = FileUtils.buildPath(fileDirectory, name + ".data." + curName + ".ixf");
			String dataLogFileName = FileUtils.buildPath(fileDirectory, name + ".data." + curName + "." + type + ".log");
			
			if (type.equalsIgnoreCase(TYPE_EXPORT) == true)
			{
				if (FileUtils.fileExists(dataFileName) == true)
				{
					logger.debug("Deleting old data file: {}", dataFileName);
					FileUtils.deleteFile(dataFileName);
				}
			}
			else
			{
				// Note: We do not want to delete the data after an export because we're assuming an import will happen
				// and that process should delete the file if appropriate
				logger.info("Clean up {} after your export. It will not happen here.", dataFileName);
				filesToDelete.add(dataFileName);
			}
			
			if (FileUtils.fileExists(dataLogFileName) == true)
			{
				logger.debug("Deleting old data log file: {}", dataLogFileName);
				FileUtils.deleteFile(dataLogFileName);
			}
			
			if (type.equalsIgnoreCase(TYPE_EXPORT) == true) {
				lines.add("export to "+dataFileName+" of ixf messages "+dataLogFileName+" "+sql+";");
			} else if (type.equalsIgnoreCase(TYPE_IMPORT) == true) {
				lines.add("import from "+dataFileName+" of ixf commitcount 20000 messages "+dataLogFileName+" "+sql+";");
			} else if (type.equalsIgnoreCase(TYPE_RUNSTATS) == true) {
				lines.add("RUNSTATS ON TABLE "+curName+" ON KEY COLUMNS WITH DISTRIBUTION ON ALL COLUMNS AND INDEX ALL ALLOW READ ACCESS;");
			} else if (type.equalsIgnoreCase(TYPE_REORG) == true) {
				lines.add("REORG TABLE "+curName+" ALLOW READ ACCESS;");
			} else if (type.equalsIgnoreCase(TYPE_TRUNCATE) == true) {
				lines.add("TRUNCATE TABLE "+curName+" IMMEDIATE;");
				lines.add("COMMIT;");
			} else if (type.equalsIgnoreCase(TYPE_RAW) == true) {
				lines.add(sql);
			}
			lastDataLogFileName = dataLogFileName;
			
			lines.add("");
		}
		lines.add("connect reset;");
		lines.add("terminate;");
		FileUtils.writeLinesToFile(dbCommandFile, lines);
		
		String execCmd = "db2 -stf " + dbCommandFile + " -z" + dbCommandLogFile;
		String command;
		if (SystemUtils.isWindows() == true)
		{
			command = "db2cmd " + execCmd;
			logger.debug("Executing command file \"" + dbCommandFile + "\" with output in \"" + dbCommandLogFile + "\"");
			execRet = SystemUtils.exec(command);
		}
		else
		{
			String stubFile = FileUtils.buildPath(fileDirectory, name + ".stub.cmd");
			ArrayList<String> commands = new ArrayList<String>();
			if ((dbconn.getProfileFile() != null) && (dbconn.getProfileFile().length() > 0))
			{
				commands.add(". " + dbconn.getProfileFile());  // ex: /dba/db2/cdXX/sqllib/db2profile
			}
			else
			{
				logger.warn("No ProfileFile specified in DbConn for " + dbconn.getDb() + ". Unexpected results may ensue.");
			}
			commands.add(execCmd);
			FileUtils.writeLinesToFile(stubFile, commands);
			command = stubFile;
			filesToDelete.add(stubFile);
			
			try
			{
				FileUtils.chmod(stubFile, "744");
			}
			catch (Exception e)
			{
				logger.error("Unable to set permissions on " + stubFile + ": " + e.getMessage(), e);
			}
			
			logger.debug("Executing command file \"" + dbCommandFile + "\" via \""+command+"\" with output in \"" + dbCommandLogFile + "\"");
			execRet = SystemUtils.exec(command, new String[]{}, fileDirectory);
		}
		logger.debug("Execution returned {}", execRet);
		
		if (execRet != 0)
		{
			throw new ProcessingException("Attempt to run "+command+" returned "+execRet);
		}
		
		// TODO: Enhancement - Check for errors or else we could camp out here all day
		
		String logToWaitFor = dbCommandLogFile;
		if  (
			(type.equalsIgnoreCase(TYPE_IMPORT) == true) ||
			(type.equalsIgnoreCase(TYPE_EXPORT) == true)
			)
		{
			logToWaitFor = lastDataLogFileName;
		}
		
		String cmdLogFileContents = null;
		logger.debug("Waiting for log file to be created: " + logToWaitFor);
		while (FileUtils.fileExists(logToWaitFor) == false)
		{
			if (FileUtils.fileExists(dbCommandLogFile) == true)
			{
				cmdLogFileContents = FileUtils.getFileContents(dbCommandLogFile);
				if (cmdLogFileContents.indexOf("SQL1013N") != -1)
				{
					throw new InfrastructureException("Unable to connect to Database. " + cmdLogFileContents);
				}
			}
			SystemUtils.sleep(5);
		}
			
		logger.debug("Make sure command log file contains expected verbage");
		int counter = 0;
		cmdLogFileContents = FileUtils.getFileContents(dbCommandLogFile);
		while (cmdLogFileContents.contains("The TERMINATE command completed successfully") == false) 
		{
			if (counter > 10)
			{
				logger.warn("\n" + cmdLogFileContents);
				throw new ProcessingException("Command log file has not completed");
			}
			counter++;
			SystemUtils.sleep(6);
			cmdLogFileContents = FileUtils.getFileContents(dbCommandLogFile);
		}
		
		logger.info(dbCommandLogFile + " contents:\n" + cmdLogFileContents);
		
		if (cmdLogFileContents.contains("There is at least one warning message in the message file") == true)
		{
			throw new ProcessingException("There is a warning in a message file");
		}
		
		for (String file : filesToDelete)
		{
			if (FileUtils.fileExists(file) == true)
			{
				logger.debug("Deleting: {}", file);
				FileUtils.deleteFile(file);
			}
		}
		
		logger.debug("Returning");
	}
}
