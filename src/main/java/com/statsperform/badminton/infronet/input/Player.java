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
public class Player
{
	private String name;
	private String country;
	
	@JacksonXmlProperty (localName = "memberid")
	private String memberId;
}