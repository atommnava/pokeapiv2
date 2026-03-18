const express = require("express");
const http = require("http");
const cors = require("cors");

const app = express();
app.use(cors());

const server = http.createServer(app);
const io = require("socket.io")(server);

let players = [];
let selections = {};

// 🔥 POKEMONES LOCALES (rápidos)
const POKEMONS = [
  { id: 1, name: "Bulbasaur", hp: 45, attack: 49 },
  { id: 4, name: "Charmander", hp: 39, attack: 52 },
  { id: 7, name: "Squirtle", hp: 44, attack: 48 },
  { id: 25, name: "Pikachu", hp: 35, attack: 55 },
  { id: 39, name: "Jigglypuff", hp: 115, attack: 45 },
  { id: 52, name: "Meowth", hp: 40, attack: 45 },
  { id: 133, name: "Eevee", hp: 55, attack: 55 },
  { id: 54, name: "Psyduck", hp: 50, attack: 52 },
  { id: 16, name: "Pidgey", hp: 40, attack: 45 },
  { id: 19, name: "Rattata", hp: 30, attack: 56 }
];

// generar 10 pokémon random (sin repetir)
function getRandomTeam() {
  return POKEMONS.sort(() => 0.5 - Math.random()).slice(0, 10);
}

io.on("connection", (socket) => {
  console.log("Jugador conectado:", socket.id);

  players.push(socket);

  if (players.length === 2) {
    console.log("🔥 2 jugadores conectados → enviando equipos");

    players[0].emit("asignar_pokemones", getRandomTeam());
    players[1].emit("asignar_pokemones", getRandomTeam());
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
  console.log("Funciona Fernanda!");
});