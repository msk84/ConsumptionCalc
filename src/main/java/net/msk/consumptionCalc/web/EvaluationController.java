package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.model.Counter;
import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.EvaluationMode;
import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.service.exception.DataLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EvaluationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationController.class);

    private final DataService dataService;

    public EvaluationController(final DataService dataService) {
        this.dataService = dataService;
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

            model.addAttribute("projects", this.dataService.getProjectList());
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
