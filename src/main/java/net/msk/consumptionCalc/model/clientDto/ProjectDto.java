package net.msk.consumptionCalc.model.clientDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
