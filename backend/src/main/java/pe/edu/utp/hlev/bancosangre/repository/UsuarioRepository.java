package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.dni = :identificador OR u.email = :identificador")
    Optional<Usuario> findByDniOrEmail(@Param("identificador") String identificador);

    boolean existsByDniOrEmail(String dni, String email);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.rol.nombre <> 'Administrador' ORDER BY u.id DESC")
    List<Usuario> findTodosLosMedicos();
}
