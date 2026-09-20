package pe.edu.utp.hlev.bancosangre.model;

/**
 * RF-07: panel de 7 marcadores infecciosos obligatorios del tamizaje serológico
 * (PRONAHEBAS considera HBsAg y anti-core como determinaciones independientes
 * dentro del marcador "Hepatitis B", de ahí que sean 7 y no 6 determinaciones).
 */
public enum MarcadorSerologico {
    VIH_1_2,
    HEPATITIS_B_HBSAG,
    HEPATITIS_B_ANTI_CORE,
    HEPATITIS_C,
    SIFILIS,
    CHAGAS,
    HTLV_1_2
}
