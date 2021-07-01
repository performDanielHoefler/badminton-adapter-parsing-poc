package com.statsperform.badminton.legacy.output;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntervalBreak
{
	private int seqNum;
	private String timestamp;
	private final String eventElementType = "IntervalBreak";
}
