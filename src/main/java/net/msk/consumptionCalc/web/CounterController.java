package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.model.Counter;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.clientDto.CounterDto;
import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import net.msk.consumptionCalc.model.clientDto.CounterMeasurementDto;
import net.msk.consumptionCalc.service.exception.DataLoadingException;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/{project}")
public class CounterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterController.class);

    private final DataService dataService;

    public CounterController(final DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping
    public String projectHome(@PathVariable("project") final String projectName, final Model model) {
        try {
            model.addAttribute("projects", this.dataService.getProjectList());
            model.addAttribute("counters", this.dataService.getCounterList(projectName));
            model.addAttribute("project", projectName);
            model.addAttribute("newCounter", new CounterDto(projectName));
            return "projectHome";
        }
        catch (final DataLoadingException e) {
            LOGGER.error("Failed loading counterList for project '{}'.", projectName, e);
            return "error";
        }
    }

    @PostMapping("/addCounter")
    public String addCounter(@PathVariable("project") final String projectName, @Valid @ModelAttribute("newCounter") CounterDto counterDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("projects", this.dataService.getProjectList());
                model.addAttribute("counters", this.dataService.getCounterList(projectName));
                model.addAttribute("project", projectName);
            }
            catch (final DataLoadingException e) {
                LOGGER.error("Failed loading counterList for project '{}'.", projectName, e);
            }
            return "projectHome";
        }
        else {
            try {
                this.dataService.addCounter(counterDto.getProject(), counterDto.getCounterName(), counterDto.getUnit());
                return "redirect:/" + projectName;
            }
            catch (final DataPersistanceException | DataLoadingException e) {
                LOGGER.error("Failed to add new counter.", e);
            }
        }

        return "redirect:/error";
    }

    @GetMapping("/{counter}/counterData")
    public String counterHome(@PathVariable("project") final String projectName,
                              @PathVariable("counter") final String counterName,
                              @RequestParam(name = "periodFrom", required = false) Integer periodFrom,
                              @RequestParam(name = "periodUntil", required = false) Integer periodUntil,
                              final Model model) {
        try {
            periodFrom = periodFrom == null ? LocalDateTime.now().getYear() - 2 : periodFrom;
            periodUntil = periodUntil == null ? LocalDateTime.now().getYear() + 1 : periodUntil;

            final RawCounterData rawCounterData = this.dataService.getRawCounterData(projectName, counterName, periodFrom, periodUntil);
            final Counter counter = this.dataService.getCounter(projectName, counterName);

            model.addAttribute("projects", this.dataService.getProjectList());
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

    @PostMapping("/{counter}/addCounterData")
    public String addCounterData(@PathVariable("project") final String projectName,
                                 @PathVariable("counter") final String counterName,
                                 @RequestParam(name = "periodFrom", required = false) Integer periodFrom,
                                 @RequestParam(name = "periodUntil", required = false) Integer periodUntil,
                                 @Valid @ModelAttribute("newCounterValue") CounterMeasurementDto counterData,
                                 BindingResult bindingResult,
                                 Model model) {

        if (bindingResult.hasErrors()) {

            try {
                final RawCounterData rawCounterData = this.dataService.getRawCounterData(projectName, counterName, periodFrom, periodUntil);
                final Counter counter = this.dataService.getCounter(projectName, counterName);

                model.addAttribute("projects", this.dataService.getProjectList());
                model.addAttribute("project", projectName);
                model.addAttribute("counter", counter);
                model.addAttribute("periodFrom", periodFrom);
                model.addAttribute("periodUntil", periodUntil);
                model.addAttribute("counterData", rawCounterData);
                return "counterData";
            }
            catch (final DataLoadingException e) {
                LOGGER.error("Failed loading counter data for project '{}' and counter '{}' in periodFrom '{}' - periodUntil '{}'.", projectName, counterName, periodFrom, periodUntil, e);
                return "error";
            }
        }
        else {
            try {
                this.dataService.addCounterData(projectName, counterName, new RawCounterDataRow(LocalDateTime.parse(counterData.getTimestamp()), counterData.getValue(), counterData.getComment()));
                return "redirect:/" + projectName + "/" + counterName + "/counterData?periodFrom=" + periodFrom + "&periodUntil=" + periodUntil;
            }
            catch (final DataPersistanceException | DataLoadingException e) {
                LOGGER.error("Failed to add counter data.", e);
            }
        }
        return "error";
    }
}
