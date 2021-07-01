package com.statsperform.badminton.infronet.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Team
{
	private String country;
	private String name;
	
	private Player player1;
	private Player player2;
}