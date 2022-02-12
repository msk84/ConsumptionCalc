package net.msk.consumptionCalc.model.clientDto;

public class ProjectDto {
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
