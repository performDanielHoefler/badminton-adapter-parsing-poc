package com.statsperform.badminton.infronet.input;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Court
{
	@JacksonXmlProperty (localName = "id")
	private int courtId;
	private String name;
	
	@JacksonXmlProperty (localName = "matchid")
	private int matchId;
	
	@JacksonXmlProperty (localName = "packetid")
	private int packetId;
	
	private Integer winner;
	
	@JacksonXmlProperty (localName = "winnerscorestatus")
	private Integer winnerScoreStatus;
	
	private String event;
	private String round;
	
	@JacksonXmlProperty (localName = "duration")
	private int durationInMinutes;
	
	private int service;
	private int receiver;
	
	@JacksonXmlProperty (localName = "team1challengesremaining")
	private int team1ChallengesRemaining;
	
	@JacksonXmlProperty (localName = "team2challengesremaining")
	private int team2ChallengesRemaining;
	
	@JacksonXmlProperty (localName = "matchstate")
	private MatchState matchState;
	
	private Tournament tournament;
	
	@JacksonXmlProperty (localName = "tournamentcode")
	private TournamentCode tournamentCode;
	
	private Team team1;
	private Team team2;
	
	private Official official1;
	private Official official2;
	
	private Sets sets;
	private Stats stats;
}
