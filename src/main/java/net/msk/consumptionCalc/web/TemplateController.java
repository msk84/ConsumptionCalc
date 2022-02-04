package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.file.DataService;
import net.msk.consumptionCalc.file.FileSystemService;
import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.RawCounterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
        final List<String> projectList;

        try {
            projectList = this.fileSystemService.getProjectList();
        }
        catch (final Exception e) {
            LOGGER.error("Failed loading projectList.", e);
            return "error";
        }

        model.addAttribute("projects", projectList);
        return "index";
    }

    @GetMapping("/{project}/")
    public String projectHome(@PathVariable("project") final String project, final Model model) {
        final List<String> counterList;

        try {
            counterList = this.fileSystemService.getCounterList(project);
        }
        catch (final Exception e) {
            LOGGER.error("Failed loading counterList for project '{}'.", project, e);
            return "error";
        }

        model.addAttribute("project", project);
        model.addAttribute("counters", counterList);

        return "projectHome";
    }

    @GetMapping("/{project}/{counter}/{period}")
    public String counterHome(@PathVariable("project") final String project,
                              @PathVariable("counter") final String counter,
                              @PathVariable("period") final String period,
                              final Model model) {
        final RawCounterData rawCounterData;

        try {
             rawCounterData = this.dataService.getRawCounterData(project, counter, period);
        }
        catch (final Exception e) {
            LOGGER.error("Failed loading counter data for project '{}' and counter '{}' in period '{}'.", project, counter, period, e);
            return "error";
        }

        model.addAttribute("project", project);
        model.addAttribute("counter", counter);
        model.addAttribute("period", period);
        model.addAttribute("counterData", rawCounterData);

        return "counterData";
    }

    @GetMapping("/{project}/evaluationResult")
    public String evaluationResult(@PathVariable("project") final String project,
                              @RequestParam(name = "id") String fileId,
                              final Model model) {
        final EvaluationData evaluationData;

        try {
            evaluationData = this.dataService.getEvaluationSimpleData(project, fileId);
        }
        catch (final Exception e) {
            LOGGER.error("Failed loading evaluation data for project '{}' id '{}'.", project, fileId, e);
            return "error";
        }

        model.addAttribute("project", project);
        model.addAttribute("fileId", fileId);
        model.addAttribute("evaluationData", evaluationData);

        return "evaluationResultData";
    }
}
