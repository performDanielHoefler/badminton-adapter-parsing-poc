package com.statsperform.badminton.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alarm
{
	private int seqNum;
	private String timestamp;
	private String eventElementType;
	private String lastReceivedTimestamp;
}