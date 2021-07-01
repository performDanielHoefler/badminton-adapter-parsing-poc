package com.statsperform.badminton.legacy.output;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchStateChanged
{
	private int seqNum;
	private String timestamp;
	private String delayStatus;
	private String matchState;
	private final String eventElementType = "MatchStateChanged";
}
