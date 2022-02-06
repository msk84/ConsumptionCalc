package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.file.DataService;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import net.msk.consumptionCalc.model.thnew.CounterMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/counterData")
public class CounterDataController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterDataController.class);

    private final DataService dataService;

    public CounterDataController(final DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/{project}/{counter}/{period}/addCounterData")
    public ModelAndView addCounterData(@PathVariable("project") final String project,
                                       @PathVariable("counter") final String counter,
                                       @PathVariable("period") final String period,
                                       @ModelAttribute CounterMeasurement counterData) {

        try {
            this.dataService.addCounterData(project, counter, period, new RawCounterDataRow(LocalDateTime.parse(counterData.getTimestamp()), counterData.getValue()));
            return new ModelAndView("redirect:/" + project + "/" + counter + "/" + period);
        }
        catch (final IOException e) {
            LOGGER.error("", e);
        }

        return new ModelAndView("redirect:/error");
    }
}
