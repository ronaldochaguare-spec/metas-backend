package com.metas.repo;

import com.metas.model.Ganancia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GananciaRepository extends JpaRepository<Ganancia, Long> {

    List<Ganancia> findByFechaOrderByCreadoEnDesc(LocalDate fecha);

    Optional<Ganancia> findFirstByFechaOrderByCreadoEnDesc(LocalDate fecha);

    @Query("select coalesce(sum(g.monto), 0) from Ganancia g where g.fecha = :fecha")
    BigDecimal totalPorFecha(@Param("fecha") LocalDate fecha);

    @Query("select g.fecha, sum(g.monto) from Ganancia g where g.fecha < :hasta group by g.fecha order by g.fecha")
    List<Object[]> totalesPorDiaAntes(@Param("hasta") LocalDate hasta);
}
