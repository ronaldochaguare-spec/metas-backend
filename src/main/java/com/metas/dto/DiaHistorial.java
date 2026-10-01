package com.metas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** total = arrastre + ganado; cumplida = total >= meta diaria. */
public record DiaHistorial(
        LocalDate fecha,
        BigDecimal ganado,
        BigDecimal arrastre,
        BigDecimal total,
        boolean cumplida) {
}
