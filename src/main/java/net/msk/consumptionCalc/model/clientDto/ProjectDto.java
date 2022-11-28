package net.msk.consumptionCalc.model.clientDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProjectDto {

    @NotNull
    @Size(min=2, max=50)
    @Pattern(regexp = "^[\\w\\d\\s-_]+$")
    private String projectName;

    public ProjectDto() {
    }

    public ProjectDto(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
