package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    private String telefono;

    private String direccion;

    // RF-03: ficha clínica integral (datos biométricos del último triaje registrado).
    @Column(name = "peso_kg")
    private BigDecimal pesoKg;

    @Column(name = "talla_cm")
    private BigDecimal tallaCm;

    @Column(name = "presion_sistolica")
    private Integer presionSistolica;

    @Column(name = "presion_diastolica")
    private Integer presionDiastolica;

    private Integer pulso;

    private BigDecimal hemoglobina;

    // RF-03/RF-04: aptitud vigente y diferimiento calculados automáticamente por TriajeService.
    @Column(nullable = false)
    private Boolean apto = true;

    @Column(name = "tipo_diferimiento")
    private String tipoDiferimiento;

    @Column(name = "motivo_diferimiento")
    private String motivoDiferimiento;

    @Column(name = "diferido_hasta")
    private LocalDate diferidoHasta;

    @Column(name = "fecha_ultima_donacion")
    private LocalDate fechaUltimaDonacion;
}
