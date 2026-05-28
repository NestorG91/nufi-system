package com.nufi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.time.Duration;

public class AsistenteIA {

    // =========================================
    // API KEY desde variable de entorno
    // =========================================
    private static final String API_KEY =
            System.getenv("CLAUDE_API_KEY");

    // =========================================
    // URL oficial Anthropic
    // =========================================
    private static final String URL =
            "https://api.anthropic.com/v1/messages";

    // =========================================
    // Cliente HTTP
    // =========================================
    private final OkHttpClient cliente =
            new OkHttpClient.Builder()
                    .callTimeout(Duration.ofSeconds(180))
                    .build();

    // =========================================
    // Método principal IA
    // =========================================
    public String preguntar(String pregunta) {

        try {

            // =========================================
            // Verificar API KEY
            // =========================================
            if (API_KEY == null || API_KEY.isEmpty()) {

                return "❌ No se encontró CLAUDE_API_KEY";
            }

            // DEBUG API KEY
            System.out.println("\n🔑 API KEY DETECTADA:");
            System.out.println(API_KEY.substring(0, 20) + "...");

            // =========================================
            // Contexto del sistema
            // =========================================
            String contexto =
                    "Eres NUFI IA, asistente agrícola especializado en " +
                            "caficultura colombiana. " +

                            "Trabajas para la finca La Quinta ubicada en " +
                            "Vereda Cordoncillar, Albania, Santander, Colombia. " +

                            "La finca pertenece a Filimon y Nubia. " +

                            "La finca tiene 1.2 hectáreas y 6 lotes de café. " +

                            "Ayudas con:\n" +
                            "- cosechas\n" +
                            "- fertilización\n" +
                            "- manejo de trabajadores.fxml\n" +
                            "- control agrícola\n" +
                            "- producción de café\n" +
                            "- inventario agrícola\n" +
                            "- estimaciones de pergamino\n" +
                            "- administración de finca\n\n" +

                            "IMPORTANTE:\n" +
                            "- responde SIEMPRE en español\n" +
                            "- responde claro y práctico\n" +
                            "- usa lenguaje sencillo\n" +
                            "- evita respuestas extremadamente técnicas\n" +
                            "- da recomendaciones útiles";

            // =========================================
            // Prompt usuario
            // =========================================
            String promptFinal =
                    "Pregunta del usuario:\n" +
                            pregunta;

            // =========================================
            // Construcción JSON
            // =========================================
            JsonObject body = new JsonObject();

            // MODELO CORRECTO
            body.addProperty(
                    "model",
                    "claude-sonnet-4-6"
            );

            // Tokens máximos
            body.addProperty("max_tokens", 2048);

            // Creatividad controlada
            body.addProperty("temperature", 0.3);

            // Contexto sistema
            body.addProperty("system", contexto);

            // =========================================
            // Messages
            // =========================================
            JsonArray messages = new JsonArray();

            JsonObject userMessage = new JsonObject();

            userMessage.addProperty(
                    "role",
                    "user"
            );

            userMessage.addProperty(
                    "content",
                    promptFinal
            );

            messages.add(userMessage);

            body.add("messages", messages);

            // DEBUG JSON
            System.out.println("\n📦 JSON ENVIADO:");
            System.out.println(body.toString());

            // =========================================
            // RequestBody
            // =========================================
            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.get("application/json")
            );

            // =========================================
            // Request HTTP
            // =========================================
            Request request = new Request.Builder()
                    .url(URL)
                    .post(requestBody)

                    // HEADERS OBLIGATORIOS
                    .addHeader("x-api-key", API_KEY)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")

                    .build();

            // =========================================
            // Ejecutar petición
            // =========================================
            Response response =
                    cliente.newCall(request).execute();

            // DEBUG STATUS
            System.out.println("\n🌐 HTTP STATUS:");
            System.out.println(response.code());

            // =========================================
            // Obtener respuesta RAW
            // =========================================
            String jsonRespuesta =
                    response.body().string();

            // DEBUG respuesta completa
            System.out.println("\n🤖 RESPUESTA RAW:");
            System.out.println(jsonRespuesta);

            // =========================================
            // Validar HTTP
            // =========================================
            if (!response.isSuccessful()) {

                return "❌ Error HTTP: " +
                        response.code() +
                        "\n" +
                        jsonRespuesta;
            }

            // =========================================
            // Parsear JSON
            // =========================================
            JsonObject json =
                    JsonParser.parseString(jsonRespuesta)
                            .getAsJsonObject();

            // =========================================
            // Validar errores Claude
            // =========================================
            if (json.has("error")) {

                return "❌ Error Claude: " +
                        json.getAsJsonObject("error")
                                .get("message")
                                .getAsString();
            }

            // =========================================
            // Obtener respuesta IA
            // =========================================
            return json.getAsJsonArray("content")
                    .get(0)
                    .getAsJsonObject()
                    .get("text")
                    .getAsString();

        } catch (Exception e) {

            e.printStackTrace();

            return "❌ Error IA: " +
                    e.getMessage();
        }
    }

    // Preguntar con datos reales de la BD
    public String preguntarConDatos(String pregunta, BaseDatos db, int usuarioId) {

        String contextoBD = db.obtenerContextoCompletoIA();
        String promptCompleto = contextoBD + "\n\nPREGUNTA DEL USUARIO:\n" + pregunta;

        // Intentar hasta 3 veces si hay timeout
        String respuesta = "❌ Error IA: timeout";
        for (int intento = 1; intento <= 3; intento++) {
            respuesta = preguntar(promptCompleto);
            if (!respuesta.startsWith("❌")) {
                break; // Si funcionó salimos del ciclo
            }
            System.out.println("⏳ Reintentando... intento " + intento + " de 3");
        }
// Guardar automáticamente en historial
        db.guardarChatHistorial(usuarioId, pregunta, respuesta);
        return respuesta;
    }
}