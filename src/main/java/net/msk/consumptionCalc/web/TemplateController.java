package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.model.*;
import net.msk.consumptionCalc.model.clientDto.CounterDto;
import net.msk.consumptionCalc.model.clientDto.ProjectDto;
import net.msk.consumptionCalc.service.DataService;
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

    private final DataService dataService;

    public TemplateController(final DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/")
    public String home(final Model model) {
        final List<Project> projectList;

        try {
            projectList = this.dataService.getProjectList();
            model.addAttribute("newProject", new ProjectDto());
            model.addAttribute("projects", projectList);

            return "index";
        }
        catch (final DataLoadingException e) {
            LOGGER.error("Failed loading projectList.", e);
            return "error";
        }
    }

    @GetMapping("/{project}")
    public String projectHome(@PathVariable("project") final String project, final Model model) {
        final List<Counter> counterList;

        try {
            counterList = this.dataService.getCounterList(project);
        }
        catch (final DataLoadingException e) {
            LOGGER.error("Failed loading counterList for project '{}'.", project, e);
            return "error";
        }

        model.addAttribute("project", project);
        model.addAttribute("newCounter", new CounterDto(project));
        model.addAttribute("counters", counterList);

        return "projectHome";
    }

    @GetMapping("/{project}/{counter}/counterData")
    public String counterHome(@PathVariable("project") final String projectName,
                              @PathVariable("counter") final String counterName,
                              @RequestParam(name = "periodFrom", required = false) Integer periodFrom,
                              @RequestParam(name = "periodUntil", required = false) Integer periodUntil,
                              final Model model) {
        final RawCounterData rawCounterData;

        try {
            periodFrom = periodFrom == null ? 2020 : periodFrom;
            periodUntil = periodUntil == null ? 2030 : periodUntil;

            rawCounterData = this.dataService.getRawCounterData(projectName, counterName, periodFrom, periodUntil);
            final Counter counter = this.dataService.getCounter(projectName, counterName);

            model.addAttribute("project", projectName);
            model.addAttribute("counter", counter);
            model.addAttribute("periodFrom", periodFrom);
            model.addAttribute("periodUntil", periodUntil);
            model.addAttribute("counterData", rawCounterData);
            model.addAttribute("newCounterValue", new CounterMeasurementDto(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), 0.0, ""));

            return "counterData";
        }
        catch (final DataLoadingException e) {
            LOGGER.error("Failed loading counter data for project '{}' and counter '{}' in periodFrom '{}' - periodUntil '{}'.", projectName, counterName, periodFrom, periodUntil, e);
            return "error";
        }
    }

    @GetMapping("/{project}/{counter}/evaluateSimple")
    public String evaluateSimple(@PathVariable("project") final String projectName,
                                 @PathVariable("counter") final String counterName,
                                 @RequestParam(name = "periodFrom", required = false) Integer periodFrom,
                                 @RequestParam(name = "periodUntil", required = false) Integer periodUntil,
                                 @RequestParam(name = "mode", required = false) final String mode,
                                 final Model model) {

        try {
            final EvaluationMode evaluationMode;
            if (mode != null) {
                evaluationMode = EvaluationMode.valueOf(mode);
            }
            else {
                evaluationMode = EvaluationMode.Timeframe;
            }

            final EvaluationData evaluationData = this.dataService.getSimpleEvaluationResult(projectName, counterName, periodFrom, periodUntil, evaluationMode);
            final Counter counter = this.dataService.getCounter(projectName, counterName);

            model.addAttribute("project", projectName);
            model.addAttribute("counter", counter);
            model.addAttribute("evaluationData", evaluationData);
            model.addAttribute("periodFrom", periodFrom);
            model.addAttribute("periodUntil", periodUntil);

            return "evaluationResult";
        }
        catch (final DataLoadingException e) {
            LOGGER.error("Failed to evaluate simple.", e);
            return "error";
        }
    }
}
