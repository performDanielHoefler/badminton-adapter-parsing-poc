package com.statsperform.badminton.infronet.input;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Scores
{
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty (localName = "score")
	private List<Score> scoreList;
}
