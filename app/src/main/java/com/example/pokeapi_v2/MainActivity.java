package com.example.pokeapi_v2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private TextView status;
    private RecyclerView recycler;
    private Button btnEnviar;
    private ArrayList<Pokemon> listaPokemons = new ArrayList<>();
    private ArrayList<Pokemon> seleccionados = new ArrayList<>();
    private PokemonAdapter adapter;
    private int playerIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        recycler = findViewById(R.id.recycler);
        btnEnviar = findViewById(R.id.btnEnviar);

        // RecyclerView
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PokemonAdapter(listaPokemons, seleccionados);
        recycler.setAdapter(adapter);

        // Socket
        socket = SocketManager.getSocket();

        socket.on(Socket.EVENT_CONNECT, args -> {
            runOnUiThread(() -> {
                status.setText("✅ Conectado al servidor");
            });
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            runOnUiThread(() -> {
                status.setText("❌ Error al conectar");
            });
        });

        socket.connect();

        status.setText("Conectando al servidor...");

        socket.on("player_index", args -> {
            playerIndex = (int) args[0];
            Log.d("PLAYER", "Soy jugador: " + playerIndex);
        });
        socket.on("asignar_pokemones", onPokemonsReceived);
        socket.on("resultado_batalla", onBattleResult);

        // Botón enviar equipo
        btnEnviar.setOnClickListener(v -> {
            if (seleccionados.size() == 3) {
                enviarEquipo();
                status.setText("Enviando equipo...");
            } else {
                status.setText("Selecciona 3 Pokémon");
            }
        });
    }

    // 🔥 Recibir Pokémon del servidor
    private Emitter.Listener onPokemonsReceived = args -> {
        runOnUiThread(() -> {
            try {
                JSONArray array = (JSONArray) args[0];

                listaPokemons.clear();
                seleccionados.clear();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    Pokemon p = new Pokemon();
                    p.id = obj.getInt("id");
                    p.name = obj.getString("name");
                    p.hp = obj.getInt("hp");
                    p.attack = obj.getInt("attack");

                    listaPokemons.add(p);
                }

                adapter.notifyDataSetChanged();
                status.setText("Elige 3 Pokémon");

                Log.d("POKEMONS", listaPokemons.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    };

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

            Log.d("TEAM", team.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🏆 Resultado de batalla
    private Emitter.Listener onBattleResult = args -> {
        runOnUiThread(() -> {
            try {
                JSONObject result = (JSONObject) args[0];

                int wins1 = result.getInt("wins1");
                int wins2 = result.getInt("wins2");
                String winner = result.getString("winner");

                String miId = socket.id();

                int misWins;
                int susWins;

                // 🔥 AQUÍ ESTÁ LA CORRECCIÓN REAL
                if (playerIndex == 0) {
                    misWins = wins1;
                    susWins = wins2;
                } else {
                    misWins = wins2;
                    susWins = wins1;
                }

                String mensajeFinal;

                if (winner.equals(miId)) {
                    mensajeFinal =
                            "⚔️ RESULTADO FINAL (MEJOR DE 3)\n\n" +
                                    "Tú: " + misWins + "\n" +
                                    "Rival: " + susWins + "\n\n" +
                                    "🏆 GANASTE";
                } else {
                    mensajeFinal =
                            "⚔️ RESULTADO FINAL (MEJOR DE 3)\n\n" +
                                    "Tú: " + misWins + "\n" +
                                    "Rival: " + susWins + "\n\n" +
                                    "💀 PERDISTE";
                }

                status.setText(mensajeFinal);

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