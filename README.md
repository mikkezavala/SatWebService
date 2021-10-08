# Servicio Web SAT

### Como iniciar
- Instalar docker

## FIEL

En este caso, para protegernos un poco y tener multi-usuarios lo ideal es crear keystores por usuario. Agregue una librería ayuda a crear el PFX.
Solo necesitas correr `$ ./create-pxf -k FIEL.key -c FIEL.cer -p passwordFiel`. Esto creara un archivo `TEST.pfx` en `src/main/resources`.

El objetivo en el futuro es, ofrecer un endpoint, que pueda cargar la kEY y el CERT y crear un BLOB en la DB, o tal vez en el FILE_SYSTEM (Inclusive S3) este store.


## Base de datos
Por ahora solo contiene una tabla `sat_client` con `rfc, password_plain, kestore` donde:
- rfc: El RFC del cliente que pide sus CFDI
- password_plain: El password del keystore (creado en el paso de creación de PFX)
- kestore: El archivo creado por la utilidad `create-pxf` (Por default se guarda como `src/main/resources/TEST.pfx`)

## Iniciando la APP
Usa docker, te hara la vida sencilla.

- Primero crea tu PFX con la utilidad `create-pxf.sh`.
- Docker necesita estar instalado en tu máquina.
- Corre `mvn -U clean install`
- Si el setup esta completo, corre `docker-compose up --build`. Esto creara una base de datos en docker y creara la tabla `sat_client`
- Una vez, ya se levantaron los servicios. Agrega a la base de datos los detalles del cliente (del que creaste con la utilidad `create-pxf.sh`).
  - En la tabla `sat_client` agrega:
    - `rfc` en texto plano quien es dueño de ese KeyStore
    - `password_plain` Password que usaste durante la generación del PFX
    - La ubicación del `kestore`, si usaste default, esto se creó en `src/main/resources/TEST.pfx`. en la DB solo coloca `TEST.pfx`

Listo tu servicio está listo para obtener peticiones.

## Peticiones en order

### Autenticando y recibiendo token
En el campo RFC, se necesita un RFC registrado en la base de datos.

#### Request:
```shell 
curl -X "POST" "http://localhost:8080/v1/token" \
     -H 'Content-Type: application/json' \
     -d $'{
  "rfc": "SUL010720JN8"
}'
```

#### Response:
```json 
{ "autenticaResult": "SAT-TOKEN-JWT" }

```

### Solicitud Descarga

#### Request
El header de autenticación requiere el token generado anteriormente. En el body el RFC es requerido que asocia la peticion.

```shell
curl -X "POST" "http://localhost:8080/v1/cfdi/request" \
     -H 'Content-Type: application/json' \
     -H 'Authorization: SAT-TOKEN-JWT' \
     -d $'{
  "rfc": "SUL010720JN8"
}'
```

#### Response
```json
{
  "result": {
    "requestId": "uuid-peticion",
    "status": "5000",
    "message": "Solicitud Aceptada"
  }
}
```

### Validación

Usando la respuesta anterior, el campo `requestId` se pasara en el campo `requestId`. Y un token (puede ser el anterior) para validar la respuesta.

#### Request
```shell
curl -X "POST" "http://localhost:8080/v1/cfdi/validate" \
     -H 'Content-Type: application/json' \
     -H 'Authorization: eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE2MzM1OTUyNjUsImV4cCI6MTYzMzU5NTg2NSwiaWF0IjoxNjMzNTk1MjY1LCJpc3MiOiJMb2FkU29saWNpdHVkRGVjYXJnYU1hc2l2YVRlcmNlcm9zIiwiYWN0b3J0IjoiMzAzMDMwMzAzMTMwMzAzMDMwMzAzMDM1MzAzOTMyMzQzNzMxMzkzMyJ9.1_bhxp9kYWHCZBquZY5vagH2-Q19vUwMIqtgq84VYHw%26wrap_subject%3d3030303031303030303030353039323437313933' \
     -d $'{
  "rfc": "SUL010720JN8",
  "requestId": "uuid-peticion"
}'
```

#### Response
Esta respuesta es importante el código de `state` si no es `3` es necesario re-verificar de nuevo hasta que sea un código exitoso (`3`). Cuando es 3 el arreglo `idsPaquetes` estará presente.
```shell
{
  "result": {
    "status": "5000",
    "state": 3,
    "message": "Solicitud Aceptada",
    "cfdiCount": 31,
    "idsPaquetes": ["UUID_PACK_01"]
  }
}
```

### Descarga
Usando la respuesta anterior, usaremos `idPaquetes` y lo pasaremos a esta petición, de nuevo un token válido es necesario como cabecera, en el cuerpo el rfc y el `packetId` que pertenece a `idsPaquetes`.

#### Request
```shell
curl -X "POST" "http://localhost:8080/v1/cfdi/download" \
     -H 'Content-Type: application/json' \
     -H 'Authorization: eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE2MzM1OTUyNjUsImV4cCI6MTYzMzU5NTg2NSwiaWF0IjoxNjMzNTk1MjY1LCJpc3MiOiJMb2FkU29saWNpdHVkRGVjYXJnYU1hc2l2YVRlcmNlcm9zIiwiYWN0b3J0IjoiMzAzMDMwMzAzMTMwMzAzMDMwMzAzMDM1MzAzOTMyMzQzNzMxMzkzMyJ9.1_bhxp9kYWHCZBquZY5vagH2-Q19vUwMIqtgq84VYHw%26wrap_subject%3d3030303031303030303030353039323437313933' \
     -d $'{
  "rfc": "SUL010720JN8",
  "packetId": "UUID_PACK_01"
}'
```

#### Response

```json
[
  {
    "serial": "PH",
    "folio": "0000000001",
    "issuer": {
      "rfc": "TME960709LR2",
      "name": "COMPANIA 1",
      "fiscalRegime": "601"
    },
    "receptor": {
      "rfc": "SUL010720JN8",
      "name": "NOMBRE CLIENTE",
      "cfdiUse": "P01"
    },
    "compliment": {
      "payments": {
        "payment": {
          "date": "2021-08-27T12:00:00",
          "paymentForm": "04",
          "currency": "MXN",
          "amount": 498.99
        }
      },
      "payroll": null
    }
  },
  {
    "serial": null,
    "folio": "545555",
    "issuer": {
      "rfc": "ULC051129GC0",
      "name": "COMPANIA 2",
      "fiscalRegime": "601"
    },
    "receptor": {
      "rfc": "SUL010720JN8",
      "name": "NOMBRE CLIENTE",
      "cfdiUse": "P01"
    },
    "compliment": {
      "payments": null,
      "payroll": {
        "totalDeductions": 24860.95,
        "totalPerception": 68176.92,
        "daysPaid": 14.0,
        "dateStart": "2021-08-09",
        "paymentDate": "2021-08-27"
      }
    }
  }
]
```

## Open Source
La idea de esto es que la comunidad lo haga robusto. No temas en crear Issues y colaborar!