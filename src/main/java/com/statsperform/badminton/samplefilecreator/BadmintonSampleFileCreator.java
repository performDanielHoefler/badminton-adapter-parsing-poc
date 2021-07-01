package com.statsperform.badminton.samplefilecreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.statsperform.badminton.infronet.input.Court;
import com.statsperform.badminton.infronet.input.MatchState;
import com.statsperform.badminton.infronet.input.Ping;
import com.statsperform.badminton.legacy.output.MatchStateChanged;

public class BadmintonSampleFileCreator
{
	private String inputFileLocation = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\BWF\\Denmarkopen\\fullmatchfile\\fullmatchfile.txt";
	private String outputFileRootPathString = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\IMG\\fullmatchfile";
	private String outputFileName = "fullmatchsample.txt";
	
	private XmlMapper xmlMapper;
	private ObjectMapper jsonObjMapper = new ObjectMapper();
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private final Map<String, String> infronetToIMGMatchStateMapping = new HashMap<>();
	
	public BadmintonSampleFileCreator ()
	{
		initXmlMapper ();
		initMatchStateMappingMap ();
		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private void initMatchStateMappingMap()
	{
		infronetToIMGMatchStateMapping.put("N", "None");
		infronetToIMGMatchStateMapping.put("C", "OnCourt");
		infronetToIMGMatchStateMapping.put("P", "InProgress");
		infronetToIMGMatchStateMapping.put("F", "Finished");
		infronetToIMGMatchStateMapping.put("W", "WarmUp");
		infronetToIMGMatchStateMapping.put("S", "WaitingToStart");
		infronetToIMGMatchStateMapping.put("H", "Challenge");
		infronetToIMGMatchStateMapping.put("O", "OffCourt"); //TODO value not contained on IMG, is this an issue?
	}

	private void initXmlMapper()
	{
		xmlMapper = new XmlMapper();
		xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public void createSampleFile () throws IOException
	{
		Collection<String> outputContent = new ArrayList<>();
		
		File inputFile = new File (inputFileLocation);
		
		Collection<Court> courtMessages = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(inputFile))
		{
			List<String> inputContent = IOUtils.readLines(fis, "UTF-8");
			
			for (String line : inputContent)
			{
				if (line.startsWith("<ping"))
				{
					Ping ping = xmlMapper.readValue(line, Ping.class);
					System.out.println("Ping command received: " + ping.toString());
					
				}
				else if (line.startsWith("<court"))
				{
					Court court = xmlMapper.readValue(line, Court.class);
					if (court!=null)
					{
						System.out.println("Court command received: " + court.toString());
						courtMessages.add(court);
					}
				}
				else
				{
					System.out.println("Data could neither be passed to ping nor court data. Content found: " + line);
				}
			}
		}
		
		Map<Integer, List<Court>> courtMessagesByMatchId = new HashMap<>();
		courtMessages.stream().forEach(e ->
		{
			int matchId = e.getMatchId();
			List<Court> courtMessagesForGame = courtMessagesByMatchId.get(matchId);
			if (courtMessagesForGame == null)
			{
				courtMessagesForGame = new ArrayList<>();
				courtMessagesByMatchId.put(matchId, courtMessagesForGame);
			}
			courtMessagesForGame.add(e);
		});
		
		List<Court> messagesForGame = null;
		for (Entry<Integer, List<Court>> entry : courtMessagesByMatchId.entrySet())
		{
			messagesForGame = entry.getValue();
			Court previousMessage = null;
			for (Court msg : messagesForGame)
			{
				//first message of the game -> MatchStateChange
				if (previousMessage == null)
				{
					MatchStateChanged matchStateChangedMsg = createMatchStateChangedMsg (msg);
					String jsonOutput = convertToJsonString (matchStateChangedMsg);
					outputContent.add(jsonOutput);
				}
				
				previousMessage = msg;
			}
		}
		
		File rootPathOutputFile = new File (outputFileRootPathString);
		if (!rootPathOutputFile.exists())
		{
			rootPathOutputFile.mkdirs();
		}
		File outputFile = new File (rootPathOutputFile, outputFileName);
		outputFile.createNewFile();
		
		//TODO write one file per game
		try (FileOutputStream fos = new FileOutputStream(outputFile))
		{
			IOUtils.writeLines(outputContent, null, fos, "UTF-8");
		}
	}
	
	private String convertToJsonString(Object obj) throws JsonProcessingException
	{
		return jsonObjMapper.writeValueAsString(obj);
	}

	private MatchStateChanged createMatchStateChangedMsg(Court msg)
	{
		MatchStateChanged matchStateChangedMsg = new MatchStateChanged();
		
		MatchState ms = msg.getMatchState();
		String matchStateString = "None";
		if (ms!=null)
		{
			matchStateString = ms.getMatchState();
		}
		matchStateChangedMsg.setMatchState(infronetToIMGMatchStateMapping.getOrDefault(matchStateString, matchStateString));
		matchStateChangedMsg.setSeqNum(msg.getPacketId()); //TODO maybe customer sequencing will have to be introduced here?
		matchStateChangedMsg.setTimestamp(convertToFormattedTimestamp(System.currentTimeMillis())); //TODO infronet doesn't provide a timestamp, so we will always have to use current timestamp?
		
		return matchStateChangedMsg;
	}

	private String convertToFormattedTimestamp(long timestamp)
	{
		return sdf.format(new Date (timestamp));
	}

	public static void main(String[] args) throws IOException
	{
		BadmintonSampleFileCreator sampleFileCreator = new BadmintonSampleFileCreator();
		sampleFileCreator.createSampleFile();
	}
}