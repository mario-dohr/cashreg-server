package at.marido.chashregserver.controller;
import java.math.BigDecimal;
import java.util.List;

public record ChartDataset(String label, List<BigDecimal> data, String backgroundColor, String borderColor) {

}
