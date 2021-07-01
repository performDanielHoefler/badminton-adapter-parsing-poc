package com.statsperform.badminton.infronet.input;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stats
{
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty (localName = "stat")
	private List<Stat> statList;
}
