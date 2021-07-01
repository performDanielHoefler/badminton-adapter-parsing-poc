package com.statsperform.badminton.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchFinished
{
	private int duration;
	private String winner;
	private String timestamp;
	private String reason;
	private String eventElementType;
	private int seqNum;
	private String delayStatus;
}