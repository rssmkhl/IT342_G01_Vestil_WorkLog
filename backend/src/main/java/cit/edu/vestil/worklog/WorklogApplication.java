package cit.edu.vestil.worklog;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
public class WorklogApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(WorklogApplication.class);
		Properties dotenv = loadDotenvProperties();
		dotenv.forEach((key, value) -> {
			String propertyName = String.valueOf(key);
			if (System.getProperty(propertyName) == null) {
				System.setProperty(propertyName, String.valueOf(value));
			}
		});
		app.run(args);
	}

	private static Properties loadDotenvProperties() {
		Properties props = new Properties();
		for (Path candidate : candidatePaths()) {
			if (Files.isRegularFile(candidate)) {
				try (var reader = Files.newBufferedReader(candidate)) {
					props.load(reader);
					if (!props.isEmpty()) {
						return props;
					}
				} catch (IOException ignored) {
				}
			}
		}
		return props;
	}

	private static java.util.List<Path> candidatePaths() {
		Path cwd = Paths.get(System.getProperty("user.dir"));
		return java.util.List.of(
				cwd.resolve(".env"),
				cwd.resolve("..").resolve(".env"),
				cwd.resolve("backend").resolve(".env"),
				cwd.resolve("..").resolve("backend").resolve(".env")
		);
	}

}
