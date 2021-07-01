package com.statsperform.badminton.infronet.input;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Sets
{
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Set> set;
}