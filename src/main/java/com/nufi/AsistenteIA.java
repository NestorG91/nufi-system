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
                    .callTimeout(Duration.ofSeconds(300)) // ← 5 minutos
                    .connectTimeout(Duration.ofSeconds(30))
                    .readTimeout(Duration.ofSeconds(300))
                    .writeTimeout(Duration.ofSeconds(30))
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
                            "Vereda Cordoncillal, Albania, Santander, Colombia. " +

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
                            "- da recomendaciones útiles\n" +
                            "- NUNCA inventes datos. Solo usa la información del bloque " +
                            "'FINCA LA QUINTA — Datos clave' que viene en el contexto.\n" +
                            "- La 'COSECHA ACTIVA' es ÚNICAMENTE la que aparece como tal " +
                            "en el contexto. Si dice 'NINGUNA', responde que no hay cosecha activa.\n" +
                            "- Los bloques marcados 'HISTÓRICO' son acumulados de TODAS las " +
                            "cosechas pasadas y NO representan la cosecha activa actual. " +
                            "No los confundas: los lotes del histórico NO son los lotes en cosecha hoy.";

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

        // ✅ Contexto reducido — solo lo esencial
        String contextoBD = db.obtenerContextoReducidoIA();
        String promptCompleto = contextoBD + "\n\nPREGUNTA:\n" + pregunta;

        String respuesta = "❌ Error IA: timeout";
        for (int intento = 1; intento <= 3; intento++) {
            respuesta = preguntar(promptCompleto);
            if (!respuesta.startsWith("❌")) break;
            System.out.println("⏳ Reintentando... intento " + intento);
        }
        db.guardarChatHistorial(usuarioId, pregunta, respuesta);
        return respuesta;
    }

    public String obtenerClimaActual() {
        try {
            // ✅ Coordenadas exactas Finca La Quinta
            String url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=5.8555&longitude=-73.7619" +
                    "&current=temperature_2m,relative_humidity_2m," +
                    "precipitation,weather_code,wind_speed_10m" +
                    "&daily=temperature_2m_max,temperature_2m_min," +
                    "precipitation_sum,weather_code" +
                    "&timezone=America%2FBogota&forecast_days=3";

            okhttp3.OkHttpClient cliente = new okhttp3.OkHttpClient();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .build();

            okhttp3.Response response = cliente.newCall(request).execute();
            String json = response.body().string();

            // Parsear con Gson
            com.google.gson.JsonObject root =
                    com.google.gson.JsonParser.parseString(json)
                            .getAsJsonObject();

            com.google.gson.JsonObject current =
                    root.getAsJsonObject("current");
            com.google.gson.JsonObject daily =
                    root.getAsJsonObject("daily");

            double temp     = current.get("temperature_2m").getAsDouble();
            double humedad  = current.get("relative_humidity_2m").getAsDouble();
            double lluvia   = current.get("precipitation").getAsDouble();
            double viento   = current.get("wind_speed_10m").getAsDouble();
            int    codigo   = current.get("weather_code").getAsInt();

            // Pronóstico 3 días
            com.google.gson.JsonArray fechas =
                    daily.getAsJsonArray("time");
            com.google.gson.JsonArray tempMax =
                    daily.getAsJsonArray("temperature_2m_max");
            com.google.gson.JsonArray tempMin =
                    daily.getAsJsonArray("temperature_2m_min");
            com.google.gson.JsonArray lluviaDia =
                    daily.getAsJsonArray("precipitation_sum");
            com.google.gson.JsonArray codigosDia =
                    daily.getAsJsonArray("weather_code");

            StringBuilder clima = new StringBuilder();
            clima.append("🌤️ CLIMA — Vereda Cordoncillal, Albania\n");
            clima.append("════════════════════════════════\n\n");
            clima.append("📍 AHORA MISMO:\n");
            clima.append("🌡️ Temperatura: ").append(temp).append("°C\n");
            clima.append("💧 Humedad: ").append(humedad).append("%\n");
            clima.append("🌧️ Lluvia: ").append(lluvia).append(" mm\n");
            clima.append("💨 Viento: ").append(viento).append(" km/h\n");
            clima.append("☁️ Condición: ")
                    .append(interpretarCodigo(codigo)).append("\n\n");

            clima.append("📅 PRÓXIMOS 3 DÍAS:\n");
            for (int i = 0; i < 3; i++) {
                clima.append("• ").append(fechas.get(i).getAsString())
                        .append(": ").append(tempMin.get(i).getAsDouble())
                        .append("°C / ").append(tempMax.get(i).getAsDouble())
                        .append("°C | Lluvia: ")
                        .append(lluviaDia.get(i).getAsDouble()).append(" mm | ")
                        .append(interpretarCodigo(
                                codigosDia.get(i).getAsInt())).append("\n");
            }

            // Recomendación para la finca
            clima.append("\n🌱 PARA LA FINCA:\n");
            if (lluvia > 5) {
                clima.append("⚠️ Hay lluvia — evita fumigar hoy.\n");
                clima.append("✅ Buen momento para abonar.\n");
            } else if (temp > 28) {
                clima.append("☀️ Día caluroso — ideal para secar café.\n");
                clima.append("💧 Riega si los lotes lo necesitan.\n");
            } else {
                clima.append("✅ Buen día para labores en la finca.\n");
                clima.append("☕ Condiciones favorables para recolección.\n");
            }

            return clima.toString();

        } catch (Exception e) {
            return "❌ No se pudo obtener el clima: " + e.getMessage();
        }
    }

    private String interpretarCodigo(int codigo) {
        if (codigo == 0)              return "Despejado ☀️";
        if (codigo <= 3)              return "Parcialmente nublado 🌤️";
        if (codigo <= 49)             return "Niebla 🌫️";
        if (codigo <= 59)             return "Llovizna 🌦️";
        if (codigo <= 69)             return "Lluvia 🌧️";
        if (codigo <= 79)             return "Nieve 🌨️";
        if (codigo <= 82)             return "Chubascos 🌧️";
        if (codigo <= 99)             return "Tormenta ⛈️";
        return "Desconocido";
    }
}