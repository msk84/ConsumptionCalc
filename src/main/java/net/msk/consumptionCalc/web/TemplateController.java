package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.model.EvaluationMode;
import net.msk.consumptionCalc.model.Project;
import net.msk.consumptionCalc.model.clientDto.CounterDto;
import net.msk.consumptionCalc.model.clientDto.ProjectDto;
import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.persistence.file.FileSystemService;
import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.clientDto.CounterMeasurementDto;
import net.msk.consumptionCalc.service.exception.DataLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class TemplateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateController.class);

    private final FileSystemService fileSystemService;
    private final DataService dataService;

    public TemplateController(final FileSystemService fileSystemService, final DataService dataService) {
        this.fileSystemService = fileSystemService;
        this.dataService = dataService;
    }

    @GetMapping("/")
    public String homePage(final Model model) {
        final List<Project> projectList;

        try {
            projectList = this.fileSystemService.getProjects();
            model.addAttribute("newProject", new ProjectDto());
            model.addAttribute("projects", projectList);
            return "index";
        } catch (final Exception e) {
            LOGGER.error("Failed loading projectList.", e);
            return "error";
        }
    }

    @GetMapping("/{project}")
    public String projectHome(@PathVariable("project") final String project, final Model model) {
        final List<String> counterList;

        try {
            counterList = this.fileSystemService.getCounterList(project);
        } catch (final Exception e) {
            LOGGER.error("Failed loading counterList for project '{}'.", project, e);
            return "error";
        }

        model.addAttribute("project", project);
        model.addAttribute("newCounter", new CounterDto(project));
        model.addAttribute("counters", counterList);

        return "projectHome";
    }

    @GetMapping("/{project}/{counter}/counterData")
    public String counterHome(@PathVariable("project") final String project,
                              @PathVariable("counter") final String counter,
                              @RequestParam(name = "periodFrom", required = false) Integer periodFrom,
                              @RequestParam(name = "periodUntil", required = false) Integer periodUntil,
                              final Model model) {
        final RawCounterData rawCounterData;

        try {
            periodFrom = periodFrom == null ? 2020 : periodFrom;
            periodUntil = periodUntil == null ? 2030 : periodUntil;
            rawCounterData = this.dataService.getRawCounterData(project, counter, periodFrom, periodUntil);

            model.addAttribute("project", project);
            model.addAttribute("counter", counter);
            model.addAttribute("periodFrom", periodFrom);
            model.addAttribute("periodUntil", periodUntil);
            model.addAttribute("counterData", rawCounterData);
            model.addAttribute("newCounterValue", new CounterMeasurementDto(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), 0.0, ""));

            return "counterData";
        } catch (final Exception e) {
            LOGGER.error("Failed loading counter data for project '{}' and counter '{}' in periodFrom '{}' - periodUntil '{}'.", project, counter, periodFrom, periodUntil, e);
            return "error";
        }
    }

    @GetMapping("/{project}/{counter}/evaluateSimple")
    public String evaluateSimple(@PathVariable("project") final String project,
                                 @PathVariable("counter") final String counter,
                                 @RequestParam(name = "periodFrom", required = false) Integer periodFrom,
                                 @RequestParam(name = "periodUntil", required = false) Integer periodUntil,
                                 @RequestParam(name = "mode", required = false) final String mode,
                                 final Model model) {

        try {
            final EvaluationMode evaluationMode;
            if (mode != null) {
                evaluationMode = EvaluationMode.valueOf(mode);
            } else {
                evaluationMode = EvaluationMode.Timeframe;
            }

            final EvaluationData evaluationData = this.dataService.evaluateSimple(project, counter, periodFrom, periodUntil, evaluationMode);
            model.addAttribute("project", project);
            model.addAttribute("evaluationData", evaluationData);
            model.addAttribute("periodFrom", periodFrom);
            model.addAttribute("periodUntil", periodUntil);

            return "evaluationResultData";
        } catch (final DataLoadingException e) {
            LOGGER.error("Failed to evaluate simple.", e);
            return "error";
        }
    }
}
