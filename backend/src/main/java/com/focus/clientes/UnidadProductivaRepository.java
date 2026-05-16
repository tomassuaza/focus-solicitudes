package com.focus.clientes;

import com.focus.common.UnidadProductiva;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadProductivaRepository extends JpaRepository<UnidadProductivaEntity, Long> {
    Optional<UnidadProductivaEntity> findByNombre(UnidadProductiva nombre);
}
