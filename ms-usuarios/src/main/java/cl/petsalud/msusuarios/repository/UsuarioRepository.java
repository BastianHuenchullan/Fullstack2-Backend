package cl.petsalud.msusuarios.repository;

import cl.petsalud.msusuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByRun(String run);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRun(String run);

    List<Usuario> findAllByOrderByIdAsc();
}
