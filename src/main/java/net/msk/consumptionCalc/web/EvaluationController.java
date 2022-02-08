package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.service.EvaluationService;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping("/persist/{evaluationId}")
    public ResponseEntity<String> persistEvaluationData(@PathVariable("evaluationId") final String evaluationId) {

        try {
            this.dataService.persistEvaluationData(UUID.fromString(evaluationId));
            return ResponseEntity.ok().build();
        }
        catch (final DataPersistanceException e) {
            LOGGER.error("Failed to persist evaluation data.", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
