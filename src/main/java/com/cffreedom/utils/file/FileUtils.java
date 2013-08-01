package com.cffreedom.utils.file;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cffreedom.utils.DateTimeUtils;
import com.cffreedom.utils.SystemUtils;

/**
 * Original Class: com.cffreedom.utils.file.FileUtils
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
 * 2013-04-14 	markjacobsen.net 	Added concatFiles()
 * 2013-04-26 	markjacobsen.net 	Added getDateStampedFileName() and getTimeStampedFileName()
 * 2013-05-08	markjacobsen.net 	Added getFirstXLines() and getLastXLines()
 * 2013-05-08 	markjacobsen.net 	Added appendFile()
 * 2013-05-17 	markjacobsen.net 	Fixed getFileContents() to not add an additional CRLF at the end of the file
 */
public class FileUtils
{
	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.file.FileUtils");
	
	/**
	 * Get the file extension
	 * 
	 * @param file File to get the extension for
	 * @return the file extension
	 */
	public static String getFileExtension(String file)
	{
		int period = file.lastIndexOf(".");
		return file.substring(period + 1, file.length());
	}

	/**
	 * Get just the file name from a full path ex: c:\temp\junk.txt would return
	 * junk.txt
	 * 
	 * @param fullPath Full path of the file to get the file name for
	 * @return File name
	 */
	public static String getFileName(String fullPath)
	{
		File file = new File(fullPath);
		return file.getName();
	}

	/**
	 * Same thing as getFileName, but removes the extension
	 * 
	 * @param fullPath Full path of the file to get the file name for
	 * @return File name (minus the extension)
	 */
	public static String getFileNameWithoutExtension(String fullPath)
	{
		String ret;
		File file = new File(fullPath);
		ret = file.getName();
		ret = ret.substring(0, ret.length() - (getFileExtension(fullPath).length() + 1));
		return ret;
	}

	/**
	 * Append a line to a file
	 * @param line The text to append
	 * @param file File to append to
	 */
	public static boolean appendLine(String line, String file)
	{
		boolean success;

		try
		{
			if (new File(file).exists() == false)
			{
				success = new File(file).createNewFile();
			}

			FileWriter fw = new FileWriter(file, true);
			fw.write(line + SystemUtils.getNewline());
			fw.flush();
			fw.close();

			success = true;
		}
		catch (Exception e)
		{
			success = false;
			e.printStackTrace();
		}

		return success;
	}
	
	public static boolean appendFile(String fileToAppend, String fileToAppendTo)
	{
		String[] files = { fileToAppend };
		return concatFiles(files, fileToAppendTo);
	}

	/**
	 * Get the entire contents of a file as a string
	 * 
	 * @param file File to get contents of
	 * @return The entire contents of a file as a string
	 */
	public static String getFileContents(String file)
	{
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		String line = "";

		try
		{
			br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			while (line != null)
			{
				sb.append(line);
				line = br.readLine();
				if (line != null) { sb.append(SystemUtils.getNewline()); }
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return sb.toString();
	}

	/**
	 * Get the contents of a file line by line into an ArrayList
	 * 
	 * @param file
	 *            File to get contents of
	 * @return ArrayList of lines in the file
	 */
	public static ArrayList<String> getFileLines(String file) { return getFileLines(file, null); }
	public static ArrayList<String> getFileLines(String file, String appendToLines) { return getFileLines(file, appendToLines, null); }
	public static ArrayList<String> getFileLines(String file, String appendToLines, String prependToLines)
	{
		ArrayList<String> lines = new ArrayList<String>();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";

			while ((line = br.readLine()) != null)
			{
				if (prependToLines != null) { line = prependToLines + line; }
				if (appendToLines != null) { line += appendToLines; }
				lines.add(line);
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return lines;
	}

	public static boolean writeStringToFile(String file, String content, boolean append)
	{
		boolean success = false;

		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, append));
			bw.write(content);
			bw.close();

			success = true;
		}
		catch (Exception e)
		{
			success = false;
			e.printStackTrace();
		}

		return success;
	}
	
	/**
	 * Write the contents of an ArrayList to a file
	 * 
	 * @param file
	 *            File to write to
	 * @param lines
	 *            ArrayList of strings to write
	 * @param append
	 *            True to append the lines to the file, false to write from the
	 *            top
	 * @return true on success, false otherwise
	 */
	public static boolean writeLinesToFile(String file, ArrayList<String> lines, boolean append)
	{
		boolean success = false;
		String term = SystemUtils.getNewline();
		
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, append));

			for (int i = 0; i < lines.size(); i++)
			{
				bw.write((String) lines.get(i) + term);
			}
			bw.close();

			success = true;
		}
		catch (Exception e)
		{
			success = false;
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * rite the contents of an ArrayList to a file (does NOT append)
	 * 
	 * @param file
	 *            File to write to
	 * @param lines
	 *            ArrayList of strings to write to the file
	 * @return true on success, false otherwise
	 */
	public static boolean writeLinesToFile(String file, ArrayList<String> lines)
	{
		return writeLinesToFile(file, lines, false);
	}

	public static boolean writeObjectToFile(String file, Object content) { return writeObjectToFile(file, content, false); }
	public static boolean writeObjectToFile(String file, Object content, boolean append)
	{
		boolean success = false;

		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(content);
			oos.close();

			success = true;
		}
		catch (Exception e)
		{
			success = false;
			e.printStackTrace();
		}

		return success;
	}

	public static Object readObjectFromFile(String file) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		Object ret = ois.readObject();
		ois.close();
		return ret;
	}

	/*
	 * For some reason these functions aren't working public static boolean
	 * renameFile(String source, String destination) throws Exception { return
	 * moveFile(source, destination); }
	 * 
	 * public static boolean renameFolder(String source, String destination)
	 * throws Exception { return moveFolder(source, destination); }
	 */

	public static boolean deleteFile(String file)
	{
		if (fileExists(file) == true)
		{
			File temp = new File(file);
			return temp.delete();
		}
		else
		{
			return false;
		}
	}
	
	public static boolean deleteFolder(String folder)
	{
		if (folderExists(folder) == true)
		{
			File temp = new File(folder);
			return temp.delete();
		}
		else
		{
			return false;
		}
	}

	/**
	 * Move a file from one location to another (Note: Must specify full src and
	 * dest - including file for both)
	 * 
	 * @param source
	 *            = File to move (full path - including file if necessary)
	 * @param destination
	 *            = New file (full path - including file if necessary)
	 * @return true if the move works, false if it fails
	 * @throws Exception
	 */
	public static boolean moveFile(String source, String destination)
	{
		File oSrc = new File(source);
		File oDst = new File(destination);

		if (oSrc.exists() == false)
		{
			return false;
		}
		return oSrc.renameTo(oDst);
	}

	/**
	 * Move a directory from one location to another (Note: Just calls the
	 * moveFile() function)
	 * 
	 * @param source
	 *            = Folder to move
	 * @param destination
	 *            = New folder
	 * @return true if the move works, false if it failes
	 * @throws Exception
	 */
	public static boolean moveFolder(String source, String destination)
	{
		return moveFile(source, destination);
	}

	/**
	 * Copy a file from one location to another
	 * 
	 * @param source
	 *            = File to copy
	 * @param destination
	 *            = Destination to copy the source file to
	 * @throws Exception
	 */
	public static boolean copyFile(String source, String destination)
	{
		boolean success = false;
		File src;
		File dst;

		try
		{
			logger.debug("Copying \"{}\" to \"{}\"", source, destination);
			if (new File(destination).exists() == false)
			{
				success = new File(destination).createNewFile();
			}

			src = new File(source);
			dst = new File(destination);

			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(dst);
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1)
			{
				fos.write(buf, 0, i);
			}
			fis.close();
			fos.close();

			success = true;
		}
		catch (Exception e)
		{
			success = false;
			e.printStackTrace();
		}

		return success;
	}
	
	/**
	 * Does a byte by byte concatenation of 1 or more files
	 * @param files Array of full file names to concatenate together
	 * @param outputFile The file to output them to
	 * @return true on success
	 */
	public static boolean concatFiles(String[] files, String outputFile)
	{
		boolean success = false;
		File src;
		File dst;

		try
		{
			dst = new File(outputFile);
			if (dst.exists() == false)
			{
				success = new File(outputFile).createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(dst);
			
			// Concatenate the files
			for (int x = 0; x < files.length; x++)
			{
				src = new File(files[x]);
				FileInputStream fis = new FileInputStream(src);
				
				byte[] buf = new byte[1024];
				
				// Get file 1
				int i = 0;
				while ((i = fis.read(buf)) != -1)
				{
					fos.write(buf, 0, i);
				}
				fis.close();
			}
			fos.close();

			success = true;
		}
		catch (Exception e)
		{
			success = false;
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * Copy an entire folder and it's contents from one location to another
	 * (Note: Recurses dir to copy all items)
	 * 
	 * @param source
	 *            = Folder to copy
	 * @param destination
	 *            = Destination to copy the source folder to
	 * @throws Exception
	 */
	public static boolean copyFolder(String source, String destination)
	{
		boolean success = false;
		try
		{
			org.apache.commons.io.FileUtils.copyDirectory(new File(source), new File(destination));
			success = true;
		}
		catch (IOException e)
		{
			success = false;
			e.printStackTrace();
		}
		
		return success;
	}

	/**
	 * Tell if a file exists
	 * 
	 * @param file File to check existance of
	 * @return true if it exists, false if not
	 */
	public static boolean fileExists(String file)
	{
		if (file == null) { return false; }
		
		File oFile = new File(file);

		if ((oFile.exists() == true) && (oFile.isFile() == true))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Tell if a folder exists
	 * 
	 * @param folder Folder to check existance of
	 * @return true if it exists, false if not
	 */
	public static boolean folderExists(String folder)
	{
		if (folder == null) { return false; }
		
		File oFolder = new File(folder);

		if ((oFolder.exists() == true) && (oFolder.isDirectory() == true))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Get a directory listing of file names (file name only)
	 * 
	 * @param folder
	 *            Folder to get listing for
	 * @return Array of file names in the folder
	 * @throws Exception
	 */
	public static String[] list(String folder) throws Exception
	{
		File dir = new File(folder);
		return dir.list();
	}

	/**
	 * Get a directory listing w/ full paths (folder and file)
	 * 
	 * @param folder
	 *            Folder to get listing for
	 * @return Array of file paths in the folder
	 * @throws Exception
	 */
	public static String[] listFullPath(String folder) throws Exception
	{
		File file;
		String[] files = list(folder);

		for (int x = 0; x < files.length; x++)
		{
			file = new File(folder, files[x]);
			files[x] = file.getAbsolutePath();
		}

		return files;
	}

	/**
	 * Get a directory listing of file names (file name only)
	 * 
	 * @param folder Folder to get listing for
	 * @param filter Filter for files (ex: .log)
	 * @return Array of file names in the folder matching the filter
	 * @throws Exception
	 */
	public static String[] list(String folder, String filter) throws Exception
	{
		File dir = new File(folder);
		return dir.list(new DirFilter(filter));
	}

	/**
	 * Get a directory listing w/ full paths (folder and file)
	 * 
	 * @param folder Folder to get listing for
	 * @param filter Filter for files (ex: .log)
	 * @return Array of file paths in the folder matching the filter
	 * @throws Exception
	 */
	public static String[] listFullPath(String folder, String filter) throws Exception
	{
		File file;
		String[] files = list(folder, filter);

		for (int x = 0; x < files.length; x++)
		{
			file = new File(folder, files[x]);
			files[x] = file.getAbsolutePath();
		}

		return files;
	}

	/**
	 * Create a folder
	 * 
	 * @param path Full path name for the folder to create
	 * @param overwrite If true -> delete folder if it already exists
	 * @return True on success, false otherwise
	 * @throws Exception
	 */
	public static boolean createFolder(String path, boolean overwrite)
	{
		try
		{
			File folder = new File(path);
	
			if ((overwrite == true) && (folder.exists() == true))
			{
				folder.delete();
			}
	
			if (folder.exists() == false)
			{
				return folder.mkdir();
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Create a folder
	 * 
	 * @param path Full path name for the folder to create
	 * @return True on success, false otherwise
	 * @throws Exception
	 */
	public static boolean createFolder(String path)
	{
		return createFolder(path, false);
	}
	
	public static boolean createFile(String path) { return createFile(path, false); }
	public static boolean createFile(String path, boolean overwrite)
	{
		try
		{
			File file = new File(path);
	
			if ((overwrite == true) && (file.exists() == true))
			{
				file.delete();
			}
	
			if (file.exists() == false)
			{
				return file.createNewFile();
			}
			else
			{
				return false;
			}
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public static String buildPath(String parent, String child) throws Exception
	{
		return (new File(parent, child)).getAbsolutePath();
	}

	/**
	 * Extract the contents of a zip/jar file to a directory
	 * 
	 * @param zipFile File to unzip
	 * @param destDir Directory to unzip to
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static void extractZip(String zipFile, String destDir) throws Exception
	{
		JarFile jar = new java.util.jar.JarFile(zipFile);
		Enumeration entries = jar.entries();
		while (entries.hasMoreElements())
		{
			JarEntry file = (JarEntry) entries.nextElement();
			File f = new File(destDir + File.separator + file.getName());
			if (file.isDirectory()) // if its a directory, create it
			{
				f.mkdir();
				continue;
			}
			InputStream is = jar.getInputStream(file); // get the input
															// stream
			FileOutputStream fos = new FileOutputStream(f);
			while (is.available() > 0) // write contents of 'is' to 'fos'
			{
				fos.write(is.read());
			}
			fos.close();
			is.close();
		}
		jar.close();
	}
	
	public static boolean replaceInFile(String file, String find, String replace)
	{
		boolean success = false;

		File theFile = new File(file);
		if (theFile.exists() == false)
		{
			logger.warn("The file does not exist: {}", file);
			return false;
		}

		try
		{
			logger.debug("Replacing \"{}\" with \"{}\" in: {}", find, replace, file);
			BufferedReader br = new BufferedReader(new FileReader(file));
			ArrayList<String> lines = new ArrayList<String>();
			String line;

			// Read the contents of the file
			while ((line = br.readLine()) != null)
			{
				lines.add(line);
			}
			br.close();

			// Delete the file because we're going to replace it
			success = theFile.delete();

			// Now write the contents of the original file with the changes needed
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (String writeLine : lines)
			{
				bw.write(writeLine.replaceAll(find, replace) + SystemUtils.getNewline());
			}
			bw.close();

			// Set success flag
			success = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			success = false;
		}	

		return success;
	}
	
	public static boolean stripLinesInFileContaining(String file, String find)
	{
		boolean success = false;

		File theFile = new File(file);
		if (theFile.exists() == false)
		{
			logger.warn("The file does not exist: {}", file);
			return false;
		}

		try
		{
			logger.debug("Stripping lines containing \"{}\" in: {}", find, file);
			BufferedReader br = new BufferedReader(new FileReader(file));
			ArrayList<String> lines = new ArrayList<String>();
			String line;

			// Read the contents of the file
			while ((line = br.readLine()) != null)
			{
				lines.add(line);
			}
			br.close();

			// Delete the file because we're going to replace it
			success = theFile.delete();

			// Now write the contents of the original file with the changes needed
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (String writeLine : lines)
			{
				if (writeLine.indexOf(find) < 0)
				{
					bw.write(writeLine + SystemUtils.getNewline());
				}
			}
			bw.close();

			// Set success flag
			success = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			success = false;
		}	

		return success;
	}
	
	public static String getDateStampedFileName(String prefix, String suffix) { return getTimeStampedFileName(prefix, suffix, "yyyy-MM-dd"); }
	public static String getTimeStampedFileName(String prefix, String suffix) { return getTimeStampedFileName(prefix, suffix, "yyyyMMddHHmmss"); }
	public static String getTimeStampedFileName(String prefix, String suffix, String mask)
	{
		return prefix + DateTimeUtils.formatDate(mask, new Date()) + suffix;
	}
	
	public static boolean touch(String file)
	{
		try
		{
			File temp = new File(file);
			if (temp.exists() == false){
				temp.createNewFile();
			}
			return temp.setLastModified((new Date()).getTime());
		}
		catch (IOException e)
		{
			return false;
		}
	}
	
	/**
	 * Get the first X lines in the file specified
	 * @param file File to read
	 * @param lines Number of lines at the beginning to return
	 * @return The first X lines in the file specified
	 */
	public static ArrayList<String> getFirstXLines(String file, int lines)
	{
		int counter = 0;
		ArrayList<String> ret = new ArrayList<String>();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";

			while ( ((line = br.readLine()) != null) && (counter < lines) )
			{
				counter++;
				ret.add(line);
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return ret;
	}
	
	/**
	 * Get the last X lines in the file specified
	 * @param file File to read
	 * @param lines Number of lines at the end to return
	 * @return The last X lines in the file specified
	 */
	public static ArrayList<String> getLastXLines(String file, int lines)
	{
		ArrayList<String> ret = new ArrayList<String>();
		ArrayList<String> allLines = getFileLines(file);
		int startIndex = 0;
		
		if (allLines.size() > lines)
		{
			startIndex = allLines.size() - lines - 1;
		}
		
		for (int x = startIndex; x < allLines.size(); x++)
		{
			ret.add(allLines.get(x));
		}
		
		return ret;
	}
	
	public static ArrayList<String> getLastLineOfFile(String file, int lines) throws IOException
	{
		ArrayList<String> ret = new ArrayList<String>();
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		int seekBackDistance = 500;
		
		// seek to end of file
		raf.seek(raf.length());
				
		// read backward in chunks to find the newline character
		StringBuilder lastLine = new StringBuilder(1000);
		byte[] byteBuffer = new byte[seekBackDistance];
		boolean foundNewline = false;
		
		while(true)
		{
			// reset flag
			foundNewline = false;
			
			if (raf.getFilePointer() == 0) {
				ret.add(lastLine.toString().trim());
				logger.debug("Breaking because either hit the beginning of the file or the file is empty");
				break; // out of the while loop
			}
			
			// make sure we don't seek to before the beginning of the file on accident
			if(raf.getFilePointer() - seekBackDistance < 0) {
				raf.seek(0);
			} else {
				// seek back a little ways
				raf.seek(raf.getFilePointer() - seekBackDistance);
			}
			
			// read from the file
			raf.read(byteBuffer, 0, seekBackDistance);
			String str = new String(byteBuffer, "UTF-8");
			
			// assume we won't find a newline char (prepare to seek back again)
			long nextSeekLocation = raf.getFilePointer() - seekBackDistance;
			for(int i = str.length() - 1; i >= 0; i--) {
				if(str.charAt(i) == '\n' && i != str.length() - 1) {
					// set flag
					foundNewline = true;
					// get string up until the newline
					str = new String(byteBuffer,i,str.length() - i);
					// seek back to one char before new line (if possible)
					if(raf.getFilePointer() - str.length() >= 0) {
						nextSeekLocation = raf.getFilePointer() - str.length();
					} else {
						nextSeekLocation = 0;
					}
					break;  // out of the for loop
				}
			}
			
			// seek back, regardless of whether or not we found a full line
			raf.seek(nextSeekLocation);
			
			// if line is empty, don't append it to the last line buffer
			if(str == null || str.trim().length() == 0)
			{
				continue;
			}
			else
			{
				// prepend text to buffer
				lastLine.insert(0, str);
			}
			
			// check if we can stop reading backwards (line found)
			if(foundNewline)
			{
				ret.add(lastLine.toString().trim());
				
				if (ret.size() == lines)
				{
					logger.debug("Found {} lines so breaking", lines);
					break;
				}
				else
				{
					logger.debug("Found {} of {} lines. Looking for more.", ret.size(), lines);
					lastLine = new StringBuilder(1000);
				}
			}
		}
		
		return ret;
	}
}

class DirFilter implements FilenameFilter
{
	String afn;

	DirFilter(String afn)
	{
		this.afn = afn;
	}

	public boolean accept(File dir, String name)
	{
		// Strip path information:
		String f = new File(name).getName();
		return f.indexOf(afn) != -1;
	}
}
