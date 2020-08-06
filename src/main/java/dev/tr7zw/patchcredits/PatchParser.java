package dev.tr7zw.patchcredits;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Rudimentary parser to get Author, subject and coAuthors of a patch file
 * 
 * @author tr7zw
 *
 */
public class PatchParser {

	public static PatchInfo parsePatch(File file) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		String from = "Unknown";
		String subject = "Unknown";
		List<String> coAuthors = new ArrayList<>();
		for(String line : lines) {
			if(line.startsWith("From: ")) {
				from = line.replace("From: ", "").split("<")[0].trim();
			}else if(line.startsWith("Subject: ")) {
				subject = line.replace("Subject: ", "").replace("[PATCH]", "").trim();
			}else if(line.startsWith("Co-authored-by: ")) {
				coAuthors.add(line.replace("Co-authored-by: ", "").split("<")[0].trim());
			}
		}
		return new PatchInfo(from, subject, coAuthors);
	}
	
	@Getter
	@AllArgsConstructor
	@ToString
	public static class PatchInfo {
		private String from;
		private String subject;
		private List<String> coAuthors;
		
		public Function<String, String> getCoAuthorString() {
			return (s) -> String.join(", ", coAuthors);
		}
	}
	
}