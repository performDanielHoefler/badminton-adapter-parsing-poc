package com.statsperform.badminton.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointScored
{
	private String server;
	private String timestamp;
	private boolean undo;
	private String scoringTeam;
	private String receiver;
	private GameScores gameScores;
	private String eventElementType;
	private String delayStatus;
	private int seqNum;
}