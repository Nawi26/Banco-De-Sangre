package pe.edu.utp.hlev.bancosangre.model;

/**
 * RF-06: catálogo de hemocomponentes que se obtienen al fraccionar una donación
 * de sangre total. Vida útil de referencia (simplificada) según conservación habitual
 * PRONAHEBAS; el Jefe de Banco de Sangre puede ajustar estos valores en una fase posterior.
 */
public enum TipoHemocomponente {

    CONCENTRADO_HEMATIES("Concentrado de Hematíes", "E", 42, 280),
    PLASMA_FRESCO_CONGELADO("Plasma Fresco Congelado", "P", 365, 220),
    CRIOPRECIPITADO("Crioprecipitado", "K", 365, 20),
    CONCENTRADO_PLAQUETAS("Concentrado de Plaquetas", "T", 5, 55);

    private final String nombre;
    private final String prefijoIsbt;
    private final int diasVidaUtil;
    private final int volumenEstimadoMl;

    TipoHemocomponente(String nombre, String prefijoIsbt, int diasVidaUtil, int volumenEstimadoMl) {
        this.nombre = nombre;
        this.prefijoIsbt = prefijoIsbt;
        this.diasVidaUtil = diasVidaUtil;
        this.volumenEstimadoMl = volumenEstimadoMl;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrefijoIsbt() {
        return prefijoIsbt;
    }

    public int getDiasVidaUtil() {
        return diasVidaUtil;
    }

    public int getVolumenEstimadoMl() {
        return volumenEstimadoMl;
    }
}
