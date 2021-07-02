package com.statsperform.badminton.fullmatchsamples.creator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class SamplesByGameIdCreator
{
	private final String pathInputFile = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\BWF\\Denmarkopen\\fullmatchfile\\fullmatchfile.txt";

	private final String rootPathOutputFiles = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\BWF\\Denmarkopen\\fullmatchfile_per_game";
	
	private XmlMapper xmlMapper;
	public SamplesByGameIdCreator()
	{
		xmlMapper = new XmlMapper();
		xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public void splitDataById() throws FileNotFoundException, IOException
	{
		Map<Integer, List<String>> messagesByGame = new HashMap<>();
		File inputFile = new File (pathInputFile);
		try (FileInputStream fis = new FileInputStream(inputFile))
		{
			List<String> lines = IOUtils.readLines(fis, "UTF-8");
			for (String line : lines)
			{
				int gameId = Integer.MAX_VALUE;
				if (line.startsWith("<ping"))
				{
					Ping ping = xmlMapper.readValue(line, Ping.class);
					gameId = ping.getMatchId();
				}
				else if (line.startsWith("<court"))
				{
					Court court = xmlMapper.readValue(line, Court.class);
					gameId = court.getMatchId();
				}
				List<String> messagesForGame = messagesByGame.get(gameId);
				if (messagesForGame == null)
				{
					messagesForGame = new ArrayList<>();
					messagesByGame.put(gameId, messagesForGame);
				}
				messagesForGame.add(line);
			}
		}
		
		File rootDirOutputFiles = new File (rootPathOutputFiles);
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
	
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		SamplesByGameIdCreator creator = new SamplesByGameIdCreator();
		creator.splitDataById();
	}
}