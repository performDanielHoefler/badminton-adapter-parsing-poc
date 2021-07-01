package com.statsperform.badminton.legacy.output;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameScores
{
	private CurrentGameScore currentGameScore;
	private List<PreviousGameScore> previousGameScores;
}
