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
public class Set
{
	@JacksonXmlProperty (localName = "w")
	private int winner;
	
	@JacksonXmlProperty (localName = "t1")
	private int points1;

	@JacksonXmlProperty (localName = "t2")
	private int points2;
	
	private Scores scores;
	
	private Stats stats;
}