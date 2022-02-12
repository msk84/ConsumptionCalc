package net.msk.consumptionCalc.model.clientDto;

public class CounterMeasurementDto {

    private String timestamp;
    private double value;
    private String comment;

    public CounterMeasurementDto() {
    }

    public CounterMeasurementDto(final String timestamp, final double value, final String comment) {
        this.timestamp = timestamp;
        this.value = value;
        this.comment = comment;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
