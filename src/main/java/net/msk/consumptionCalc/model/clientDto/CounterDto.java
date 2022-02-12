package net.msk.consumptionCalc.model.clientDto;

public class CounterDto {

    private String project;
    private String counterName;

    public CounterDto() {
    }

    public CounterDto(final String project) {
        this.project = project;
    }

    public CounterDto(final String project, final String counterName) {
        this.project = project;
        this.counterName = counterName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }
}
