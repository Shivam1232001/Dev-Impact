package com.example.vc_data_fetcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContributorWithCommits {
	private String authorName;
	private String githubUrl;
	private int contributions;
	private List<Commit> commits;  // reuse your existing Commit DTO
}
