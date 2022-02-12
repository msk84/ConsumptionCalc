package net.msk.consumptionCalc.web;

import net.msk.consumptionCalc.model.clientDto.ProjectDto;
import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    private final DataService dataService;

    public ProjectController(final DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/add")
    public ModelAndView addProject(@ModelAttribute ProjectDto project) {

        try {
            this.dataService.addProject(project.getProjectName());
            return new ModelAndView("redirect:/");
        }
        catch (final DataPersistanceException e) {
            LOGGER.error("Failed to add new project.", e);
        }

        return new ModelAndView("redirect:/error");
    }
}
