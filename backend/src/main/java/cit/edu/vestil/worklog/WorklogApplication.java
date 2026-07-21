package cit.edu.vestil.worklog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
public class WorklogApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(WorklogApplication.class);
		app.addInitializers(applicationContext -> {
			ConfigurableEnvironment environment = applicationContext.getEnvironment();
			for (Path candidate : candidatePaths()) {
				if (Files.isRegularFile(candidate)) {
					try {
						Properties props = PropertiesLoaderUtils.loadProperties(new FileSystemResource(candidate));
						if (!props.isEmpty()) {
							Map<String, Object> source = new LinkedHashMap<>();
							props.forEach((key, value) -> source.put(String.valueOf(key), value));
							environment.getPropertySources().addFirst(new MapPropertySource("dotenv", source));
							break;
						}
					} catch (IOException ignored) {
					}
				}
			}
		});
		app.run(args);
	}

	private static java.util.List<Path> candidatePaths() {
		Path cwd = Paths.get(System.getProperty("user.dir"));
		return java.util.List.of(
				cwd.resolve(".env"),
				cwd.resolve("backend").resolve(".env"),
				cwd.resolve("..").resolve("backend").resolve(".env")
		);
	}

}
