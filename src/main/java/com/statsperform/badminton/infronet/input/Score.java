package com.statsperform.badminton.infronet.input;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score
{
	@JacksonXmlProperty (localName = "t1")
	private int points1;

	@JacksonXmlProperty (localName = "t2")
	private int points2;
	
	private String st1p1;
	private String st1p2;
	private String st2p1;
	private String st2p2;
}
