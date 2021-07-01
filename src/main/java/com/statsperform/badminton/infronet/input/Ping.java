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
public class Ping
{
	@JacksonXmlProperty (localName = "tournamentcode")
	private String tournamentCode;
	
	private String court;
	
	@JacksonXmlProperty (localName = "matchid")
	private int matchId;
	
	private String timestamp;
}