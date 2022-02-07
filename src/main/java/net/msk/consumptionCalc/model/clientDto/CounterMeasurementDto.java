package net.msk.consumptionCalc.model.clientDto;

public class CounterMeasurementDto {

    private String timestamp;
    private double value;

    public CounterMeasurementDto() {
    }

    public CounterMeasurementDto(final String timestamp, final double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
