package at.marido.chashregserver.controller;

import java.util.List;

public record ChartData(List<String> labels, List<ChartDataset> datasets) {

}
