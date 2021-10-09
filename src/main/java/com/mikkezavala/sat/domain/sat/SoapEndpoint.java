package com.mikkezavala.sat.domain.sat;

public enum SoapEndpoint {
  AUTENTICA("http://DescargaMasivaTerceros.gob.mx/IAutenticacion/Autentica",
      "https://cfdidescargamasivasolicitud.clouda.sat.gob.mx/Autenticacion/Autenticacion.svc"),
  SOLICITA_DESCARGA(
      "http://DescargaMasivaTerceros.sat.gob.mx/ISolicitaDescargaService/SolicitaDescarga",
      "https://cfdidescargamasivasolicitud.clouda.sat.gob.mx/SolicitaDescargaService.svc"),
  VALIDA_DESCARGA(
      "http://DescargaMasivaTerceros.sat.gob.mx/IVerificaSolicitudDescargaService/VerificaSolicitudDescarga",
      "https://cfdidescargamasivasolicitud.clouda.sat.gob.mx/VerificaSolicitudDescargaService.svc"),
  DESCARGA_MASIVA(
      "http://DescargaMasivaTerceros.sat.gob.mx/IDescargaMasivaTercerosService/Descargar",
      "https://cfdidescargamasiva.clouda.sat.gob.mx/DescargaMasivaService.svc"
  );

  private final String action;
  private final String url;

  SoapEndpoint(String action, String url) {
    this.url = url;
    this.action = action;
  }

  public String getAction() {
    return this.action;
  }

  public String getEndpoint() {
    return this.url;
  }

}

