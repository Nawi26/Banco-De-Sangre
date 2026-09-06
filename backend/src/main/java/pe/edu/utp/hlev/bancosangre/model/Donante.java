package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "donantes")
@Getter
@Setter
@NoArgsConstructor
public class Donante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_doc")
    private String tipoDoc;

    @Column(name = "num_doc")
    private String numDoc;

    private String nombres;

    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String sexo;

    @Column(name = "grupo_abo")
    private String grupoAbo;

    @Column(name = "factor_rh")
    private String factorRh;

    @Column(name = "estado_diferido")
    private Boolean estadoDiferido;

    @Column(name = "es_ocupacion_riesgo")
    private Boolean esOcupacionRiesgo;
}
