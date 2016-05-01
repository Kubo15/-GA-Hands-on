package grafAlgo.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

	public static List<File> getAllFileNamesFromDirectory(String filesDirectory){
		List<File> fileNames = new ArrayList<File>();
		
		File dir = new File(filesDirectory);
		File[] directoryFiles = dir.listFiles();
		
		for (File file : directoryFiles) {
			fileNames.add(file);
		}
		
		return fileNames;
	}
	
	public static String removeFileSuffix(String fileName){
		return fileName.substring(0, fileName.indexOf('.'));
	}
}
