package net.msk.consumptionCalc.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<String> getCounterList(final String project) throws IOException {
        final Path dataFolder = this.getDataFolder(project);
        return this.getFolderList(dataFolder);
    }

    public Path getDataFile(final String project, final String counter, final String period) throws IOException {
        final Path counterFolder = this.getCounterFolder(project, counter);
        final Path dataFile = counterFolder.resolve(period + ".csv");
        if(Files.exists(dataFile)) {
            return dataFile;
        }
        else {
            throw new IOException("Data file " + dataFile + " not found.");
        }
    }

}
