package com.metas.web;

import com.metas.dto.GananciaRequest;
import com.metas.dto.ResumenDia;
import com.metas.dto.ResumenMes;
import com.metas.service.GananciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ganancias")
public class GananciaController {

    private final GananciaService service;

    public GananciaController(GananciaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumenDia registrar(@Valid @RequestBody GananciaRequest req) {
        return service.registrar(req);
    }

    @GetMapping("/hoy")
    public ResumenDia hoy() {
        return service.resumenHoy();
    }

    @DeleteMapping("/ultima")
    public ResumenDia deshacer() {
        return service.eliminarUltima();
    }

    @GetMapping("/mes")
    public ResumenMes mes(@RequestParam(required = false) String mes) {
        try {
            YearMonth ym = mes == null ? YearMonth.now(GananciaService.LIMA) : YearMonth.parse(mes);
            return service.resumenMes(ym);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de mes inválido, usa YYYY-MM");
        }
    }
}
