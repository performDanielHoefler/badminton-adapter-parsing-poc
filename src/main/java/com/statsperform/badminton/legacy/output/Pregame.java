package com.statsperform.badminton.legacy.output;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pregame
{
	private String tournamentId;
	private Official official2;
	private String server;
	private String timestamp;
	private String event;
	private String delayStatus;
	private String courtName;
	private Official official1;
	private Team teamA;
	private String receiver;
	private String matchState;
	private final String eventElementType = "BadmintonPregame";
	private int seqNum;
	private String round;
	private Team teamB;
	private String tournamentName;
	private String courtId;
}