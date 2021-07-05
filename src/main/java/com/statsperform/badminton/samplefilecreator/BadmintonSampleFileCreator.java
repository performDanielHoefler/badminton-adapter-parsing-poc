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
import com.statsperform.badminton.infronet.input.Score;
import com.statsperform.badminton.infronet.input.Scores;
import com.statsperform.badminton.infronet.input.Set;
import com.statsperform.badminton.infronet.input.Sets;
import com.statsperform.badminton.legacy.output.CurrentGameScore;
import com.statsperform.badminton.legacy.output.GameScores;
import com.statsperform.badminton.legacy.output.MatchFinished;
import com.statsperform.badminton.legacy.output.MatchStateChanged;
import com.statsperform.badminton.legacy.output.PointScored;
import com.statsperform.badminton.legacy.output.PreviousGameScore;

public class BadmintonSampleFileCreator
{
	private String inputFileLocation = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\BWF\\Denmarkopen\\fullmatchfile_per_game\\fullmatch ID 127.txt";
	private String outputFileRootPathString = "C:\\Users\\daniel.hoefler\\Desktop\\Badminton\\base investigation\\samples\\IMG\\fullmatchfile";
	
	private XmlMapper xmlMapper;
	private ObjectMapper jsonObjMapper = new ObjectMapper();
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private final Map<String, String> infronetToIMGMatchStateMapping = new HashMap<>();
	private final Map<Integer, String> infronetToIMGWinnerStatusMapping = new HashMap<>();
	private final Map<Integer, String> infronetToIMGWinnerMapping = new HashMap<>();
	private final Map<Integer, String> teamPlayerMapping = new HashMap<>();
	private final Map<Integer, String> setWinnerMapping = new HashMap<>();
	
	private final String FINISHED_STATUS = "F";
	private final String UNKNOWN = "Unknown";
	private final String TEAM_A = "TeamA";
	private final String TEAM_B = "TeamB";
	
	public BadmintonSampleFileCreator ()
	{
		initXmlMapper ();
		initMatchStateMappingMap ();
		initWinnerStatusMapping ();
		initWinnerMapping ();
		initTeamPlayerMapping ();
		initSetWinnerMapping ();
		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private void initSetWinnerMapping()
	{
		setWinnerMapping.put(1, TEAM_A);
		setWinnerMapping.put(2, TEAM_B);
	}

	private void initTeamPlayerMapping()
	{
		teamPlayerMapping.put(0, "None"); //TODO there is no None on IMG
		teamPlayerMapping.put(1, "TeamAPlayer1");
		teamPlayerMapping.put(2, "TeamAPlayer2");
		teamPlayerMapping.put(3, "TeamBPlayer1");
		teamPlayerMapping.put(4, "TeamBPlayer1");
	}

	private void initWinnerMapping()
	{
		infronetToIMGWinnerMapping.put(0, "NoWinner");
		infronetToIMGWinnerMapping.put(1, TEAM_A);
		infronetToIMGWinnerMapping.put(2, TEAM_B);
		infronetToIMGWinnerMapping.put(3, "Tie"); //TODO there is no Tie on IMG, just add this value maybe?
	}

	private void initWinnerStatusMapping()
	{
		infronetToIMGWinnerStatusMapping.put(0, "Normally"); //TODO 0 from Infronet means None, but there is no None in IMG, so maybe map 0 to Normally?
		infronetToIMGWinnerStatusMapping.put(1, "Walkover");
		infronetToIMGWinnerStatusMapping.put(2, "Retirement");
		//TODO there is no disqualification on Infronet, but on IMG there is
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
						//TODO when game finishes, do we want to have MatchStateChanged and MatchFinished, or only MatchFinished? For now generate only MatchFinished
						if (hasMatchStateChangedToFinish(msg, previousMessage))
						{
							createAndHandleMatchFinishedMsg (msg, outputContentByGameId);
						}
						else
						{
							createAndHandleMatchStateChangedMsg (msg, outputContentByGameId);
						}
					}
					else if (hasScoreChanged (msg, previousMessage))
					{
						createAndHandlePointScoredMsg (msg, outputContentByGameId);
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
	
	private void createAndHandlePointScoredMsg(Court msg, Map<Integer, List<String>> outputContentByGameId) throws JsonProcessingException
	{
		PointScored pointScoredMsg = createPointScoredMsg (msg);
		String jsonOutput = convertToJsonString (pointScoredMsg);
		addToOutputContentMap (jsonOutput, msg.getMatchId(), outputContentByGameId);
	}

	private PointScored createPointScoredMsg(Court msg)
	{
		PointScored psMsg = new PointScored();
		psMsg.setGameScores(getGameScores (msg));
		psMsg.setReceiver(getReceiver (msg));
		psMsg.setScoringTeam(getScoringTeam (msg));
		psMsg.setSeqNum(msg.getPacketId());
		psMsg.setServer(getServer (msg));
		psMsg.setTimestamp(convertToFormattedTimestamp(System.currentTimeMillis()));
		psMsg.setUndo(false); //TODO for now always assume it's not a score correction
		
		//TODO omit delayStatus for now
		return psMsg;
	}

	private GameScores getGameScores(Court msg)
	{
		GameScores gameScores = new GameScores();
		gameScores.setCurrentGameScore(getCurrentGameScore (msg));
		gameScores.setPreviousGameScores(getPreviousGameScores(msg));
		return gameScores;
	}

	private List<PreviousGameScore> getPreviousGameScores(Court msg)
	{
		List<PreviousGameScore> prevGameScoreList = new ArrayList<>();
		List<Set> setList = getSetList (msg.getSets());
		//exclude last set, this is shown in currentGameScore
		PreviousGameScore prevGameScore = null;
		for (int i=0;i<setList.size()-1;i++)
		{
			prevGameScore = new PreviousGameScore();
			Set set = setList.get(i);
			setScoreInfo(prevGameScore, getLastScore(set));
			
			prevGameScore.setWinner(setWinnerMapping.get(set.getWinner()));
			prevGameScoreList.add(prevGameScore);
		}
		return prevGameScoreList;
	}

	private CurrentGameScore getCurrentGameScore(Court msg)
	{
		CurrentGameScore curGameScore = new CurrentGameScore();
		Score lastScore = getLastScore(getLastSet(getSetList(msg.getSets())));
		setScoreInfo (curGameScore, lastScore);
		
		return curGameScore;
	}

	private void setScoreInfo(CurrentGameScore gameScore, Score score)
	{
		gameScore.setTeamA(score.getPoints1());
		gameScore.setTeamB(score.getPoints2());
	}

	private String getScoringTeam(Court msg)
	{
		String scoringTeam = UNKNOWN; //TODO there is no Unknown on IMG, but use it as fallback?
		
		List<Set> setList = getSetList(msg.getSets());
		if (collectionNotNullAndNotEmpty(setList))
		{
			Set lastSet = getLastSet(setList);
			if (lastSet!=null)
			{
				Score lastScore = getLastScore (lastSet);
				Score secondLastScore = getSecondLastScore (lastSet);
				
				if (lastScore.getPoints1()>secondLastScore.getPoints1())
				{
					scoringTeam = TEAM_A;
				}
				else if (lastScore.getPoints2()>secondLastScore.getPoints2())
				{
					scoringTeam = TEAM_B;
				}
			}
		}
		
		return scoringTeam;
	}

	private Score getSecondLastScore(Set set)
	{
		Score result = null;
		List<Score> scoreList = getScoreList (set);
		if (scoreList!=null)
		{
			result = scoreList.get(scoreList.size()-2);
		}
		return result;
	}

	private Score getLastScore(Set set)
	{
		Score lastScore = null;
		List<Score> scoreList = getScoreList (set);
		if (scoreList!=null)
		{
			lastScore = scoreList.get(scoreList.size()-1);
		}
		return lastScore;
	}

	private List<Score> getScoreList(Set set)
	{
		List<Score> scoreList = null;
		
		Scores scores = set.getScores();
		if (scores!=null)
		{
			scoreList = scores.getScoreList();
		}
		
		return scoreList;
	}

	private String getReceiver(Court msg)
	{
		return teamPlayerMapping.getOrDefault(msg.getReceiver(), UNKNOWN); //TODO there is no Unknown on IMG
	}

	private String getServer(Court msg)
	{
		return teamPlayerMapping.getOrDefault(msg.getService(), UNKNOWN); //TODO there is no Unknown on IMG
	}

	private boolean hasScoreChanged(Court curMsg, Court previousMessage)
	{
		boolean hasChanged = false;
		
		List<Set> curMsgSetList = getSetList (curMsg.getSets());
		List<Set> prevMsgSetList = getSetList (previousMessage.getSets());
		if (collectionNotNullAndNotEmpty (curMsgSetList) && collectionNotNullAndNotEmpty(prevMsgSetList))
		{
			if (curMsgSetList.size()!=prevMsgSetList.size())
			{
				hasChanged = true;
			}
			else
			{
				//get the last set and check if there are differences in sizes. If yes, score has changed
				Set curMsgLastSet = getLastSet (curMsgSetList);
				Set prevMsgLastSet = getLastSet (prevMsgSetList);
				
				List<Score> curMsgScoreList = getScoreList(curMsgLastSet);
				List<Score> prevMsgScoreList = getScoreList(prevMsgLastSet);
				if (collectionNotNullAndNotEmpty(curMsgScoreList)
						&& collectionNotNullAndNotEmpty(prevMsgScoreList))
				{
					if (curMsgScoreList.size()!=prevMsgScoreList.size())
					{
						hasChanged = true;
					}
				}
			}
		}
		return hasChanged;
	}

	private List<Set> getSetList(Sets sets)
	{
		List<Set> setList = null;
		if (sets!=null)
		{
			setList = sets.getSet();
		}
		return setList;
	}

	private Set getLastSet(List<Set> setList)
	{
		Set lastSet = null;
		if (collectionNotNullAndNotEmpty(setList))
		{
			lastSet = setList.get(setList.size()-1);
		}
		return lastSet;
	}

	private boolean collectionNotNullAndNotEmpty(List<?> list)
	{
		return list!=null && !list.isEmpty();
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
		matchFinished.setReason(getWinnerReason (msg));
		matchFinished.setSeqNum(msg.getPacketId());
		matchFinished.setTimestamp(convertToFormattedTimestamp(System.currentTimeMillis()));
		matchFinished.setWinner(getWinner(msg));
		
		return matchFinished;
	}

	private String getWinner(Court msg)
	{
		return infronetToIMGWinnerMapping.getOrDefault(msg.getWinner(), UNKNOWN); //TODO there is no unknown on IMG, but maybe use it as fallback?
	}

	private String getWinnerReason(Court msg)
	{
		return infronetToIMGWinnerStatusMapping.getOrDefault(msg.getWinnerScoreStatus(), UNKNOWN); //TODO there is no unknown on IMG, but maybe use it as fallback?
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