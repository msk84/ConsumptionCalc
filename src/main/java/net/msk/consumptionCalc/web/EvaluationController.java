package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationController.class);

    private final DataService dataService;

    public EvaluationController(final DataService dataService) {
        this.dataService = dataService;
    }
}
