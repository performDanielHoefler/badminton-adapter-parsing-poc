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
import com.statsperform.badminton.legacy.output.MatchFinished;
import com.statsperform.badminton.legacy.output.MatchStateChanged;

public class BadmintonSampleFileCreator
{
	private String inputFileLocation = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\BWF\\Denmarkopen\\fullmatchfile_per_game\\fullmatch ID 127.txt";
	private String outputFileRootPathString = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\IMG\\fullmatchfile";
	
	private XmlMapper xmlMapper;
	private ObjectMapper jsonObjMapper = new ObjectMapper();
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private final Map<String, String> infronetToIMGMatchStateMapping = new HashMap<>();
	
	private final String FINISHED_STATUS = "F";
	
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
		infronetToIMGMatchStateMapping.put(FINISHED_STATUS, "Finished");
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
		Map<Integer, List<String>> outputContentByGameId = new HashMap<>();
		
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
			//for now we'll just focus on court messages, ping (basically the heartbeats) are irrelevant at this stage)
			for (Court msg : messagesForGame)
			{
				//first message of the game -> MatchStateChange
				if (previousMessage == null)
				{
					createAndHandleMatchStateChangedMsg (msg, outputContentByGameId);
				}
				
				else
				{
					//if state has changed, we create another matchStateChanged message
					if (hasMatchStateChanged(msg, previousMessage))
					{
						createAndHandleMatchStateChangedMsg (msg, outputContentByGameId);
						//TODO when game finishes, do we want to have MatchStateChanged and MatchFinished, or only MatchFinished? For now generate both
						if (hasMatchStateChangedToFinish(msg, previousMessage))
						{
							createAndHandleMatchFinishedMsg (msg, outputContentByGameId);
						}
					}
				}
				previousMessage = msg;
			}
		}
		
		File rootPathOutputFile = new File (outputFileRootPathString);
		if (!rootPathOutputFile.exists())
		{
			rootPathOutputFile.mkdirs();
		}
		for (Entry<Integer, List<String>> entry : outputContentByGameId.entrySet())
		{
			Integer gameId = entry.getKey();
			List<String> outputForGame = entry.getValue();
			String outputFileName = "fullmatchsample ID " + gameId + ".txt";
			File outputFile = new File (rootPathOutputFile, outputFileName);
			outputFile.createNewFile();
			
			try (FileOutputStream fos = new FileOutputStream(outputFile))
			{
				IOUtils.writeLines(outputForGame, null, fos, "UTF-8");
			}
		}
	}
	
	private void createAndHandleMatchFinishedMsg(Court msg, Map<Integer, List<String>> outputContentByGameId) throws JsonProcessingException
	{
		MatchFinished matchFinishedMsg = createMatchFinishedMsg (msg);
		String jsonOutput = convertToJsonString (matchFinishedMsg);
		addToOutputContentMap (jsonOutput, msg.getMatchId(), outputContentByGameId);
	}

	private MatchFinished createMatchFinishedMsg(Court msg)
	{
		MatchFinished matchFinished = new MatchFinished();
		matchFinished.setDurationInMinutes(msg.getDurationInMinutes());
		matchFinished.setReason(reason);
		matchFinished.setSeqNum(seqNum);
		matchFinished.setTimestamp(timestamp);
		matchFinished.setWinner(winner);
		
		//TODO ignore delayStatus for now
		return null;
	}

	private boolean hasMatchStateChangedToFinish(Court curMsg, Court previousMessage)
	{
		boolean isFinished = false;
		if (hasMatchStateChanged(curMsg, previousMessage) && isFinishedMatchState (curMsg))
		{
			isFinished = true;
		}
		return isFinished;
	}

	private boolean isFinishedMatchState(Court msg)
	{
		boolean isFinishedState = false;
		if (msg.getMatchState()!=null && FINISHED_STATUS.equals(msg.getMatchState().getMatchState()))
		{
			isFinishedState = true;
		}
		return isFinishedState;
	}

	private void createAndHandleMatchStateChangedMsg(Court msg,
			Map<Integer, List<String>> outputContentByGameId) throws JsonProcessingException
	{
		MatchStateChanged matchStateChangedMsg = createMatchStateChangedMsg (msg);
		String jsonOutput = convertToJsonString (matchStateChangedMsg);
		addToOutputContentMap (jsonOutput, msg.getMatchId(), outputContentByGameId);
	}

	private boolean hasMatchStateChanged(Court currentMessage, Court previousMessage)
	{
		boolean changed = false;
		
		MatchState curMatchState = currentMessage.getMatchState();
		MatchState prevMatchState = previousMessage.getMatchState();

		if (curMatchState!=null && prevMatchState!=null && !curMatchState.getMatchState().equals(prevMatchState.getMatchState()))
		{
			changed = true;
		}
		return changed;
	}

	private void addToOutputContentMap(String jsonOutput, int matchId, Map<Integer, List<String>> outputContentByGameId)
	{
		List<String> contentForGameId = outputContentByGameId.get(matchId);
		if (contentForGameId == null)
		{
			contentForGameId = new ArrayList<>();
			outputContentByGameId.put(matchId, contentForGameId);
		}
		contentForGameId.add(jsonOutput);
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
		matchStateChangedMsg.setSeqNum(msg.getPacketId()); //TODO maybe custom sequencing will have to be introduced here?
		matchStateChangedMsg.setTimestamp(convertToFormattedTimestamp(System.currentTimeMillis())); //TODO infronet doesn't provide a timestamp, so we will always have to use current timestamp?
		//TODO ignore delayStatus for now
		
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