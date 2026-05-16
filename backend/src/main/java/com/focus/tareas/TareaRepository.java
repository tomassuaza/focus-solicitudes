package com.focus.tareas;

import com.focus.common.EstadoTarea;
import com.focus.common.UnidadProductiva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    @Query("""
        SELECT t FROM Tarea t
        WHERE (:unidad IS NULL OR t.solicitud.unidad.nombre = :unidad)
          AND (:estado IS NULL OR t.estado = :estado)
        ORDER BY t.solicitud.prioridad DESC, t.fechaCreacion ASC
        """)
    List<Tarea> buscar(@Param("unidad") UnidadProductiva unidad,
                       @Param("estado") EstadoTarea estado);
}
