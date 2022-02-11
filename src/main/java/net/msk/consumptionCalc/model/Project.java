package net.msk.consumptionCalc.model;

import java.util.List;

public record Project(String projectName, List<String> counters) {
}
