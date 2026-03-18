package com.example.pokeapi_v2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket socket;

    private TextView status;

    private ArrayList<Pokemon> listaPokemons = new ArrayList<>();
    private ArrayList<Pokemon> seleccionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);

        socket = SocketManager.getSocket();

        // 🔥 EVENTO: cuando conecta
        socket.on(Socket.EVENT_CONNECT, args -> {
            runOnUiThread(() -> {
                status.setText("✅ Conectado al servidor");
            });
        });

        // 🔥 EVENTO: error de conexión (MUY IMPORTANTE)
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            runOnUiThread(() -> {
                status.setText("❌ Error al conectar");
            });
        });

        socket.connect();

        status.setText("Conectando al servidor...");

        socket.on("asignar_pokemones", onPokemonsReceived);
        socket.on("resultado_batalla", onBattleResult);
    }

    // 🔥 Recibir pokemones del servidor
    private Emitter.Listener onPokemonsReceived = args -> {
        runOnUiThread(() -> {
            try {
                JSONArray array = (JSONArray) args[0];

                listaPokemons.clear();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    Pokemon p = new Pokemon();
                    p.id = obj.getInt("id");
                    p.name = obj.getString("name");
                    p.hp = obj.getInt("hp");
                    p.attack = obj.getInt("attack");

                    listaPokemons.add(p);
                }

                status.setText("Pokémon recibidos. Seleccionando equipo...");

                Log.d("POKEMONS", listaPokemons.toString());

                // 👉 Selección automática de 3 (para pruebas)
                seleccionar3Pokemons();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    };

    // 🎮 Seleccionar 3 pokemones automáticamente
    private void seleccionar3Pokemons() {
        seleccionados.clear();

        for (int i = 0; i < 3 && i < listaPokemons.size(); i++) {
            seleccionados.add(listaPokemons.get(i));
        }

        enviarEquipo();
    }

    // 📤 Enviar equipo al servidor
    private void enviarEquipo() {
        JSONArray team = new JSONArray();

        try {
            for (Pokemon p : seleccionados) {
                JSONObject obj = new JSONObject();

                obj.put("id", p.id);
                obj.put("name", p.name);
                obj.put("hp", p.hp);
                obj.put("attack", p.attack);

                team.put(obj);
            }

            socket.emit("elegir_equipo", team);

            status.setText("Equipo enviado. Esperando resultado...");

            Log.d("TEAM", team.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🏆 Recibir resultado de batalla
    private Emitter.Listener onBattleResult = args -> {
        runOnUiThread(() -> {
            try {
                JSONObject result = (JSONObject) args[0];

                int wins1 = result.getInt("wins1");
                int wins2 = result.getInt("wins2");
                String winner = result.getString("winner");

                String miId = socket.id();

                String mensaje;

                if (winner.equals(miId)) {
                    mensaje = "🏆 ¡GANASTE!\n" + wins1 + " vs " + wins2;
                } else {
                    mensaje = "💀 Perdiste\n" + wins1 + " vs " + wins2;
                }

                status.setText(mensaje);

                Log.d("RESULTADO", result.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}