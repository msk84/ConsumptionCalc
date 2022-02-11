package net.msk.consumptionCalc.persistence.file;

import net.msk.consumptionCalc.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemService.class);

    private final Path baseFolderPath;
    private final Path projectsFolderPath;

    private final static String BASE_FOLDER_NAME = "consumption-calc";
    private final static String PROJECTS_FOLDER_NAME = "projects";

    public FileSystemService() {
        final String userHome = System.getProperty("user.home");
        this.baseFolderPath = Paths.get(userHome + "/" + BASE_FOLDER_NAME);
        this.projectsFolderPath = this.baseFolderPath.resolve(PROJECTS_FOLDER_NAME);
    }

    private Path ensureFolder(final Path folderPath) throws IOException {
        if(Files.exists(folderPath)) {
            return folderPath;
        }
        else {
            try {
                return Files.createDirectory(folderPath);
            }
            catch (final Exception e) {
                LOGGER.error("Failed to find or create folder {}. Error: {}", folderPath, e);
                throw e;
            }
        }
    }

    private List<String> getFolderList(final Path path) {
        final File[] projectFolders = path.toFile().listFiles(File::isDirectory);
        return Arrays.stream(Objects.requireNonNull(projectFolders))
                .map(File::getName)
                .collect(Collectors.toList());
    }

    private Path getDataFolder(final String project) throws IOException {
        this.ensureFolder(this.baseFolderPath);
        this.ensureFolder(this.projectsFolderPath);
        final Path projectPath = this.projectsFolderPath.resolve(project);
        this.ensureFolder(projectPath);
        final Path dataFolder = projectPath.resolve("data");
        return this.ensureFolder(dataFolder);
    }

    public Path getEvaluationFolder(final String project) throws IOException {
        this.ensureFolder(this.baseFolderPath);
        this.ensureFolder(this.projectsFolderPath);
        final Path projectPath = this.projectsFolderPath.resolve(project);
        this.ensureFolder(projectPath);
        final Path evalFolder = projectPath.resolve("evaluation");
        return this.ensureFolder(evalFolder);
    }

    private Path getCounterFolder(final String project, final String counter) throws IOException {
        final Path dataFolder = this.getDataFolder(project);
        final Path counterFolder = dataFolder.resolve(counter);
        return this.ensureFolder(counterFolder);
    }

    public List<String> getProjectList() throws IOException {
        this.ensureFolder(this.baseFolderPath);
        this.ensureFolder(this.projectsFolderPath);
        return this.getFolderList(this.projectsFolderPath);
    }

    public List<Project> getProjects() throws IOException {
        this.ensureFolder(this.baseFolderPath);
        this.ensureFolder(this.projectsFolderPath);
        final List<String> projectFolders = this.getFolderList(this.projectsFolderPath);

        final List<Project> result = new ArrayList<>();
        for(final String project : projectFolders) {
            final List<String> counterList = this.getCounterList(project);
            result.add(new Project(project, counterList));
        }
        return result;
    }

    public List<String> getCounterList(final String project) throws IOException {
        final Path dataFolder = this.getDataFolder(project);
        return this.getFolderList(dataFolder);
    }

    public List<Path> getRawDataFiles(final String project, final String counter, final Integer periodFrom, final Integer periodUntil) throws IOException {
        final Path counterFolder = this.getCounterFolder(project, counter);
        final List<Path> result;
        try (Stream<Path> walk = Files.walk(counterFolder)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> this.filterDataFiles(p, periodFrom, periodUntil))
                    .collect(Collectors.toList());
        }
        return result;
    }

    private boolean filterDataFiles(final Path path, final Integer periodFrom, final Integer periodUntil) {
        final String filePath = path.getFileName().toString();
        final Integer fileNameInt = Integer.parseInt(filePath.substring(0, filePath.length() - 4));
        return filePath.toLowerCase(Locale.ROOT).endsWith(".csv") && (fileNameInt >= periodFrom) && (fileNameInt <= periodUntil);
    }
}
