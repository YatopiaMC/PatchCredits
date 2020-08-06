package dev.tr7zw.patchcredits;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import dev.tr7zw.patchcredits.PatchParser.PatchInfo;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Mojo(name = "update", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class PatchCredits extends AbstractMojo {

	/**
	 * Root directory of the project
	 */
	@Parameter(defaultValue = "${project.basedir}", property = "basedir", required = true)
	private File projectDirectory;
	
	/**
	 * Patch directory
	 */
	@Parameter(defaultValue = "${project.basedir}/patches", property = "patchdir", required = true)
	private File patchDirectory;
	
	/**
	 * Output filename
	 */
	@Parameter(defaultValue = "PATCHES.md", property = "outputFile", required = true)
	private String outputFileName;
	/**
	 * src filename
	 */
	@Parameter(defaultValue = ".template.md", property = "srcFile", required = true)
	private String srcFileName;

	public void execute() throws MojoExecutionException {
		File src = new File(projectDirectory, srcFileName);
		if (!src.exists()) {
			super.getLog().warn("Unable to find src at '" + src.getAbsolutePath() + "'! Skipping!");
			return;
		}
		
		if (!patchDirectory.exists()) {
			super.getLog().warn("Unable to find patch directory at '" + patchDirectory.getAbsolutePath() + "'! Skipping!");
			return;
		}

		super.getLog().info("Scanning '" + patchDirectory + "' for patches!");
		
		List<PatchInfo> patches = new ArrayList<>();
		scanFolder(patchDirectory, patches);
		
		if(patches.isEmpty()) {
			super.getLog().warn("Unable to find any patches! Skipping!");
			return;
		}
		
		patches.sort((a, b) -> a.getSubject().compareTo(b.getSubject()));
		
		Output output = new Output();
		output.setPatches(patches);
		
		try {
			MustacheFactory mf = new DefaultMustacheFactory();
			@Cleanup
			FileReader srcReader = new FileReader(src);
		    Mustache mustache = mf.compile(srcReader, "template");
		    File outputFile = new File(projectDirectory, outputFileName);
		    if(outputFile.exists()) {
		    	outputFile.delete();
		    }
		    @Cleanup
		    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
		    mustache.execute(writer, output).flush();
		}catch(IOException ex) {
			throw new MojoExecutionException("Error while writing the output file!", ex);
		}
	}
	
	@Getter
	@NoArgsConstructor
	@Setter
	public static class Output{
		List<PatchInfo> patches;
	}
	
	private void scanFolder(File folder, List<PatchInfo> patches) {
		for(File f : folder.listFiles()) {
			if(f.isDirectory()) {
				scanFolder(f, patches);
			}else if(f.getName().endsWith(".patch")) {
				try {
					patches.add(PatchParser.parsePatch(f));
				}catch(IOException ex) {
					super.getLog().warn("Exeption while parseing '" + f.getAbsolutePath() + "'!", ex);
				}
			}
		}
	}

}