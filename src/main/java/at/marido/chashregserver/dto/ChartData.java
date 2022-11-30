package at.marido.chashregserver.dto;

import java.util.List;

public record ChartData(List<String> labels, List<ChartDataset> datasets) {

}
