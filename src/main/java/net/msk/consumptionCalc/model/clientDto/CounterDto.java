package net.msk.consumptionCalc.model.clientDto;

import net.msk.consumptionCalc.model.Unit;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CounterDto {

    @NotNull
    @Size(min=2, max=50)
    @Pattern(regexp = "^[\\w\\d\\s-_]+$")
    private String project;

    @NotNull
    @Size(min=2, max=50)
    @Pattern(regexp = "^[\\w\\d\\s-_]+$")
    private String counterName;
    private Unit unit;

    public CounterDto() {
    }

    public CounterDto(final String project) {
        this.project = project;
    }

    public CounterDto(final String project, final String counterName, final Unit unit) {
        this.project = project;
        this.counterName = counterName;
        this.unit = unit;
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

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
