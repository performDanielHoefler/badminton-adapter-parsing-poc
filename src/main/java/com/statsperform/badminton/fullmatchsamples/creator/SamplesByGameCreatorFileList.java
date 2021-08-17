package com.statsperform.badminton.fullmatchsamples.creator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.statsperform.badminton.infronet.input.Court;
import com.statsperform.badminton.infronet.input.Ping;

public class SamplesByGameCreatorFileList
{
	private final String rootPathStringInputFiles = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\BWF\\Sample data 2021-08-17\\HSBC World Tour Finals 2020\\test";
	private final String rootPathStringOutputFiles = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\BWF\\Sample data 2021-08-17\\HSBC World Tour Finals 2020\\test\\fullmatchfiles";
	
	private XmlMapper xmlMapper;
	
	public SamplesByGameCreatorFileList()
	{
		xmlMapper = new XmlMapper();
		xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public void createFullMatchSamples() throws IOException
	{
		File rootPathInputFiles = new File (rootPathStringInputFiles);
		File[] inputFiles = rootPathInputFiles.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname)
			{
				return !pathname.isDirectory();
			}
		});
		
		Map<Integer, List<String>> messagesByGame = new HashMap<>();
		
		for (File inputFile : inputFiles)
		{
			try (FileInputStream fis = new FileInputStream(inputFile))
			{
				List<String> lines = IOUtils.readLines(fis, "UTF-8");
				String fullContent = "";
				boolean isFirst = true;
				for (int i=1;i<lines.size();i++)
				{
					if (!isFirst)
					{
						fullContent += " ";
					}
					else
					{
						isFirst = false;
					}
					fullContent += lines.get(i);
				}
				fullContent = fullContent.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
				int gameId = -1;
				if (fullContent.contains("<ping"))
				{
					Ping ping = xmlMapper.readValue(fullContent, Ping.class);
					gameId = ping.getMatchId();
				}
				else if (fullContent.contains("<court"))
				{
					Court court = xmlMapper.readValue(fullContent, Court.class);
					gameId = court.getMatchId();
				}
				
				List<String> messagesForGame = messagesByGame.get(gameId);
				if (messagesForGame == null)
				{
					messagesForGame = new ArrayList<>();
					messagesByGame.put(gameId, messagesForGame);
				}
				messagesForGame.add(fullContent);
			}
		}
		
		File rootDirOutputFiles = new File (rootPathStringOutputFiles);
		if (!rootDirOutputFiles.exists())
		{
			rootDirOutputFiles.mkdirs();
		}
		for (Entry<Integer, List<String>> entry : messagesByGame.entrySet())
		{
			Integer gameId = entry.getKey();
			List<String> messages = entry.getValue();
			
			File outputFile = new File (rootDirOutputFiles, "fullmatch ID " + gameId + ".txt");
			outputFile.createNewFile();
			
			try (FileOutputStream fos = new FileOutputStream(outputFile))
			{
				IOUtils.writeLines(messages, null, fos, "UTF-8");
			}
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		SamplesByGameCreatorFileList creator = new SamplesByGameCreatorFileList();
		creator.createFullMatchSamples();
	}
}