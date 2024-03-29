package net.msk.consumptionCalc.web;

import jakarta.validation.Valid;
import net.msk.consumptionCalc.model.clientDto.ProjectDto;
import net.msk.consumptionCalc.service.DataService;
import net.msk.consumptionCalc.service.exception.DataLoadingException;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProjectsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsController.class);

    private final DataService dataService;

    public ProjectsController(final DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/projects";
    }

    @GetMapping("/projects")
    public String projects(final Model model) {
        try {
            model.addAttribute("projects", this.dataService.getProjectList());
            model.addAttribute("newProject", new ProjectDto());
            return "index";
        }
        catch (final DataLoadingException e) {
            LOGGER.error("Failed loading projectList.", e);
            return "error";
        }
    }

    @PostMapping("/projects/addProject")
    public String addProject(@Valid @ModelAttribute("newProject") ProjectDto project, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            try {
                model.addAttribute("projects", this.dataService.getProjectList());
            }
            catch (final DataLoadingException e) {
                LOGGER.error("Failed loading projectList.", e);
            }

            return "index";
        }
        else {
            try {
                this.dataService.addProject(project.getProjectName());
                return "redirect:/" + project.getProjectName();
            }
            catch (final DataPersistanceException e) {
                LOGGER.error("Failed to add new project.", e);
            }
        }

        return "redirect:/error";
    }
}
