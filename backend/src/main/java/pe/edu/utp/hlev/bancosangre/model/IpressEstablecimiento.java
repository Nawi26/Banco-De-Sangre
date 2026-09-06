package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ipress_establecimientos")
@Getter
@Setter
@NoArgsConstructor
public class IpressEstablecimiento {

    @Id
    private Long id;

    @Column(name = "codigo_renipress")
    private String codigoRenipress;

    @Column(name = "nombre_establecimiento")
    private String nombreEstablecimiento;

    @Column(name = "categoria_ipress")
    private String categoriaIpress;

    @Column(name = "tipo_banco_sangre")
    private String tipoBancoSangre;
}
