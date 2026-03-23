const express = require("express");
const http = require("http");
const cors = require("cors");
const axios = require("axios"); // 👈 NUEVO

const app = express();
app.use(cors());

const server = http.createServer(app);
const io = require("socket.io")(server);

let players = [];
let selections = {};

// 🔥 FUNCIÓN PARA OBTENER POKEMONES REALES
async function getRandomTeam() {
  const team = [];
  const usedIds = new Set();

  while (team.length < 10) {
    const id = Math.floor(Math.random() * 151) + 1;

    if (usedIds.has(id)) continue;
    usedIds.add(id);

    try {
      const res = await axios.get(`https://pokeapi.co/api/v2/pokemon/${id}`);

      team.push({
        id: res.data.id,
        name: res.data.name,
        hp: res.data.stats.find(s => s.stat.name === "hp").base_stat,
        attack: res.data.stats.find(s => s.stat.name === "attack").base_stat
      });

    } catch (error) {
      console.log("Error al obtener pokemon:", id);
    }
  }

  return team;
}

io.on("connection", (socket) => {
  console.log("Jugador conectado:", socket.id);
  players.push(socket);

  socket.emit("player_index", players.length - 1);

  if (players.length === 2) {
    console.log("🔥 2 jugadores conectados → enviando equipos");

    // 👇 AHORA ES ASYNC
    assignTeams();
  }

  socket.on("elegir_equipo", (team) => {
    console.log("Equipo recibido de", socket.id);
    selections[socket.id] = team;

    if (Object.keys(selections).length === 2) {
      startBattle();
    }
  });

  socket.on("disconnect", () => {
    console.log("Jugador desconectado:", socket.id);
    players = players.filter(p => p.id !== socket.id);
    delete selections[socket.id];
  });
});

// 🔥 NUEVA FUNCIÓN PARA ESPERAR API
async function assignTeams() {
  const team1 = await getRandomTeam();
  const team2 = await getRandomTeam();

  players[0].emit("asignar_pokemones", team1);
  players[1].emit("asignar_pokemones", team2);
}

// 👇 TU LÓGICA ORIGINAL (NO SE TOCA)
function fight(p1, p2) {
  const score1 = p1.attack + Math.random() * 20;
  const score2 = p2.attack + Math.random() * 20;
  return score1 > score2 ? 1 : 2;
}

function startBattle() {
  const ids = Object.keys(selections);
  const team1 = selections[ids[0]];
  const team2 = selections[ids[1]];

  let wins1 = 0;
  let wins2 = 0;

  for (let i = 0; i < 3; i++) {
    const r = fight(team1[i], team2[i]);
    if (r === 1) wins1++;
    else wins2++;
  }

  const result = {
    wins1,
    wins2,
    winner: wins1 > wins2 ? ids[0] : ids[1]
  };

  players.forEach(p => p.emit("resultado_batalla", result));
  selections = {};
}

server.listen(3000, "0.0.0.0", () => {
  console.log("Funciona!");
});