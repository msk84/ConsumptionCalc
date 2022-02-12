package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.model.clientDto.CounterDto;
import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import net.msk.consumptionCalc.model.clientDto.CounterMeasurementDto;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/counter")
public class CounterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterController.class);

    private final DataService dataService;

    public CounterController(final DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/add")
    public ModelAndView addCounter(@ModelAttribute CounterDto counterDto) {

        try {
            this.dataService.addCounter(counterDto.getProject(), counterDto.getCounterName());
            return new ModelAndView("redirect:/" + counterDto.getProject());
        }
        catch(final DataPersistanceException e) {
            LOGGER.error("Failed to add new counter.", e);
        }

        return new ModelAndView("redirect:/error");
    }

    @PostMapping("/addCounterData/{project}/{counter}")
    public ModelAndView addCounterData(@PathVariable("project") final String project,
                                       @PathVariable("counter") final String counter,
                                       @RequestParam(name = "periodFrom", required = false) Integer periodFrom,
                                       @RequestParam(name = "periodUntil", required = false) Integer periodUntil,
                                       @ModelAttribute CounterMeasurementDto counterData) {

        try {
            this.dataService.addCounterData(project, counter, new RawCounterDataRow(LocalDateTime.parse(counterData.getTimestamp()), counterData.getValue(), counterData.getComment()));
            return new ModelAndView("redirect:/" + project + "/" + counter + "/counterData?periodFrom=" + periodFrom + "&periodUntil=" + periodUntil);
        }
        catch (final DataPersistanceException e) {
            LOGGER.error("Failed to add counter data.", e);
        }

        return new ModelAndView("redirect:/error");
    }
}
