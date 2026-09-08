# base_httpServer

Proyecto base de un servidor HTTP mínimo escrito en Java con sockets, construido con Maven, junto con utilidades de red de apoyo.

## Contenido

### Servidor HTTP (`HttpServer.java`)
- Servidor secuencial construido sobre `ServerSocket`, escucha en el puerto **35000**.
- Cada petición se procesa una a la vez, el socket del cliente y sus flujos se cierran tras cada respuesta.
- Lee la línea de petición y las cabeceras, extrae el URI y la ruta.
- Ruta `/hello?name=<nombre>`: responde una respuesta de tipo "JSON" con el nombre recibido desde la query string.
- Cualquier otra ruta: devuelve una página HTML embebida (inline) con dos formularios:
  - **GET** a `/hello?name=<valor>` mediante `XMLHttpRequest`.
  - **POST** a `/hellopost?name=<valor>` mediante `fetch()`.
- La página y el JavaScript están embebidos como literales de cadena en el código fuente; no hay recursos estáticos externos.

### Utilidades de red
| Archivo | Descripción |
|---|---|
| `EchoServer.java` | Servidor TCP de eco en el puerto 35000. Acepta una conexión, devuelve cada línea prefijada con `Response: ` y termina al recibir `Bye.` |
| `EchoClient.java` | Cliente TCP de eco: lee líneas desde la consola, las envía a `127.0.0.1:35000` e imprime la respuesta. |
| `URLReader.java` | Obtiene `http://www.google.com/` con `URLConnection` e imprime las cabeceras de respuesta y el cuerpo. |
| `ReadUrl.java` | Descompone un URI de ejemplo (`http://ldbn.escuelaing.edu.co:5678/...`) e imprime protocolo, autoridad, host, puerto, ruta, query y archivo. |

### Punto de entrada
- `App.java`: programa mínimo de ejemplo ("Hello World!"), no usado por el servidor.

## Pruebas
- `AppTest.java`: prueba unitaria de marcador de posición (siempre pasa).

## Requisitos
- Java 17 o superior.
- Maven 3.x.

## Compilar y ejecutar

Compilar el proyecto:

```bash
mvn clean package
```

Ejecutar el servidor HTTP:

```bash
java -cp target/TDSE-1.0.jar LabUrl.HttpServer
```

Abrir luego en el navegador:

```
http://localhost:35000/
```

Ejecutar las utilidades (cada una es un `main` independiente):

```bash
java -cp target/TDSE-1.0.jar LabUrl.EchoServer
java -cp target/TDSE-1.0.jar LabUrl.EchoClient
java -cp target/TDSE-1.0.jar LabUrl.URLReader
java -cp target/TDSE-1.0.jar LabUrl.ReadUrl
```

## Herramientas de desarrollo
- Archivos de configuración del editor en `.vscode/`.
- Configuraciones de Maven en `.mvn/` (`jvm.config`, `maven.config`).

## Alcance / limitaciones conocidas
- El servidor procesa las conexiones de forma secuencial, una a la vez; no usa hilos ni concurrencia.
- No sirve recursos estáticos (HTML, JavaScript, imágenes) desde archivos externos.
- No devuelve códigos de error estándar (404, 405, 400) para recursos o métodos no soportados.
- El puerto está fijado en 35000 y el servidor escucha en todas las interfaces por defecto.