# Servicio Web SAT

## Build Status
[![CircleCI](https://circleci.com/gh/mikkezavala/SatWebService/tree/main.svg?style=svg)](https://circleci.com/gh/mikkezavala/SatWebService/tree/main)
[![codecov](https://codecov.io/gh/mikkezavala/SatWebService/branch/main/graph/badge.svg?token=84OAPS6E4O)](https://codecov.io/gh/mikkezavala/SatWebService)

## Como iniciar
- Instalar docker

## FIEL

En este caso, para protegernos un poco y tener multi-usuarios lo ideal es crear keystores por usuario. Agregue una librería ayuda a crear el PFX.
Solo necesitas correr `$ ./create-pxf -k FIEL.key -c FIEL.cer -p passwordFiel -r RFC`. Esto creara un archivo `<RFC>.pfx` en `./kestore/<RFC>.pfx`.

Por ahora creamos en el file system (proteger) el keystore (.pfx)


## Base de datos
Por ahora solo contiene una tabla `sat_client` con `id, rfc, password_plain, kestore` donde:
- id: Se auto-genera 
- rfc: El RFC del cliente que pide sus CFDI
- password_plain: El password del keystore (creado en el paso de creación de PFX)
- keystore: El archivo creado por la utilidad `create-pxf` (Por default se guarda como `./kestore/<RFC>.pfx`)

## Iniciando la APP
Usa docker, te hara la vida sencilla.

- Docker necesita estar instalado en tu máquina.
- Corre `mvn -U clean install`
- Si el setup está completo, corre `docker-compose up --build`. Esto creara una base de datos en docker y creara las tablas necesarias

Una vez, ya se levantaron los servicios. Ya estás listo para empezar.

## Peticiones en order

### Subiendo tu CER, KEY y PASSWORD de tu FIEL


#### Request:
```shell 
curl -X "POST" "http://localhost:8080/v1/persona-fisica/upload-key" \
     -H 'Content-Type: multipart/form-data; charset=utf-8;' \
     -F "file-key='MultiPart KEY'" \
     -F "file-cert='MultiPart CERT'" \
     -F "password=FIEL_PASS" \
     -F "rfc=RFC_CLIENT"
```

#### Response:
```json 
{"message":"Keystore created for RFC_CLIENT"}
```

### Solicitud Descarga (One Shot)
En este método (endpoint). Hacemos en el back todas las necesarias interacciones con el SAT. Si responde inmediatamente obtendrás las facturas, si no un menaje: READY, ACCEPTED o REJECTED.

#### Request
EL header con el token no es necesario el backend administra el manejo de ellos y la peticion de nuevos.

```shell
curl -X "POST" "http://localhost:8080/v1/persona-fisica/download-received" \
     -H 'Content-Type: application/json' \
     -d $'{
  "rfc": "RFC_CLIENT",
  "dateEnd": "2021-10-09T04:09:12Z",
  "dateStart": "2021-08-09T04:09:15Z"
}'
```

#### Response
```json
{"message":null,"satState":"ACCEPTED","invoices":[]}
```

 Si el valor de `satState` es `ACCEPTED` intentante de nuevo con los mismos parametros del request. Esto se guardo en la DB y de forma automatica buscara el limite del SAT (cerca de 10 veces) para intentar descargar.

Por default este servicio wvit mas peticiones al SAT si se ha llamado mas de 5 veces en un rango de 4 horas. Cuando eso pasa tendras una respuesta similar a:

```json
{"message":"Backing off validation request. Wait time: 217 minutes","satState":"ACCEPTED","invoices":[]}
```

## Open Source
La idea de esto es que la comunidad lo haga robusto. No temas en crear Issues y colaborar!

Pasa a revisar nuesta licencia Open Source (GNU General Public License): [GNU GPL](/LICENSE)