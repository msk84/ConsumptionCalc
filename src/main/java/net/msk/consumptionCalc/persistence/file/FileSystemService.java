package net.msk.consumptionCalc.persistence.file;

import net.msk.consumptionCalc.model.Counter;
import net.msk.consumptionCalc.model.Project;
import net.msk.consumptionCalc.service.exception.DataLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
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
        if (Files.exists(folderPath)) {
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

    private Path getDataFolder(final String project) throws IOException, DataLoadingException {
        this.ensureFolder(this.baseFolderPath);
        this.ensureFolder(this.projectsFolderPath);
        final Path projectPath = this.projectsFolderPath.resolve(project);
        if (Files.exists(projectPath)) {
            this.ensureFolder(projectPath);
            final Path dataFolder = projectPath.resolve("data");
            return this.ensureFolder(dataFolder);
        }
        else {
            throw new DataLoadingException("Project folder path not found for project: " + project);
        }
    }

    private Path getCounterFolder(final String project, final String counter) throws IOException, DataLoadingException {
        final Path dataFolder = this.getDataFolder(project);
        final Path counterFolder = dataFolder.resolve(counter);
        if(Files.exists(counterFolder)) {
            return counterFolder;
        }
        else {
            throw new DataLoadingException("Counter folder path not found for counter: " + counter + " in project: " + project);
        }
    }

    public void addProject(final String name) throws IOException {
        this.ensureFolder(this.projectsFolderPath.resolve(name));
    }

    public List<Project> getProjects() throws IOException, DataLoadingException {
        this.ensureFolder(this.baseFolderPath);
        this.ensureFolder(this.projectsFolderPath);
        final List<String> projectFolders = this.getFolderList(this.projectsFolderPath);

        final List<Project> result = new ArrayList<>();
        for (final String project : projectFolders) {
            final List<Counter> counterList = this.getCounterList(project);
            result.add(new Project(project, counterList));
        }
        return result;
    }

    public void addCounter(final String project, final Counter counter) throws IOException, DataLoadingException {
        final Path dataFolder = this.getDataFolder(project);
        this.ensureFolder(dataFolder.resolve(counter.counterName()));

        final Path counterFolder = this.getCounterFolder(project, counter.counterName()).resolve("counter.info");
        final FileOutputStream f = new FileOutputStream(counterFolder.toFile());
        final ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(counter);
        o.close();
        f.close();
    }

    public List<Counter> getCounterList(final String project) throws IOException, DataLoadingException {
        final Path dataFolder = this.getDataFolder(project);
        final List<String> counterFolders = this.getFolderList(dataFolder);
        final List<Counter> result = new ArrayList<>();
        counterFolders.forEach(c -> {
            try {
                final Counter counter = this.getCounter(project, c);
                result.add(counter);
            }
            catch (final IOException | ClassNotFoundException | DataLoadingException e) {
                LOGGER.error("Failed to read counter.info.", e);
            }
        });
        return result;
    }

    public Counter getCounter(final String projectName, final String counterName) throws IOException, ClassNotFoundException, DataLoadingException {
        final Path counterFolder = this.getCounterFolder(projectName, counterName).resolve("counter.info");
        final FileInputStream fis = new FileInputStream(counterFolder.toString());
        final ObjectInputStream ois = new ObjectInputStream(fis);
        final Counter counter = (Counter) ois.readObject();
        return counter;
    }

    public Path getRawDataFilePathForYear(final String project, final String counter, final Integer year) throws IOException, DataLoadingException {
        return this.getCounterFolder(project, counter).resolve(year + ".csv");
    }

    public List<Path> getRawDataFiles(final String project, final String counter, final Integer periodFrom, final Integer periodUntil) throws IOException, DataLoadingException {
        final Path counterFolder = this.getCounterFolder(project, counter);
        final List<Path> result;
        try (Stream<Path> walk = Files.walk(counterFolder)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(Files::isReadable)
                    .filter(p -> !p.toFile().getName().equals("counter.info"))
                    .filter(p -> this.filterDataFiles(p, periodFrom, periodUntil))
                    .collect(Collectors.toList());
        }
        return result;
    }

    private boolean filterDataFiles(final Path path, final Integer periodFrom, final Integer periodUntil) {
        final String filePath = path.getFileName().toString();
        final int fileNameInt = Integer.parseInt(filePath.substring(0, filePath.length() - 4));
        return filePath.toLowerCase(Locale.ROOT).endsWith(".csv") && (fileNameInt >= periodFrom) && (fileNameInt <= periodUntil);
    }
}
