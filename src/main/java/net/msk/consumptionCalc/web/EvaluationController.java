package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.service.EvaluationService;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.service.exception.DataLoadingException;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationController.class);

    private final EvaluationService evaluationService;
    private final DataService dataService;

    public EvaluationController(final EvaluationService evaluationService, final DataService dataService) {
        this.evaluationService = evaluationService;
        this.dataService = dataService;
    }

    @PostMapping("/{project}/{counter}/{period}/evaluateSimple")
    public ModelAndView evaluateSimple(@PathVariable("project") final String project,
                                 @PathVariable("counter") final String counter,
                                 @PathVariable("period") final String period) {

        final RawCounterData rawCounterData;

        try {
            rawCounterData = this.dataService.getRawCounterData(project, counter, period);
            final String fileId = this.evaluationService.evaluateSimple(project, rawCounterData);
            return new ModelAndView("redirect:/" + project + "/evaluationResult?id=" + fileId);
        }
        catch (final DataPersistanceException | DataLoadingException e) {
            LOGGER.error("Failed to evaluate simple.", e);
        }

        return new ModelAndView("redirect:/error");
    }
}
