'use strict';
// Client réel, non modifié, pour observer les changements de dimension provoqués en CI.
const mineflayer = require('mineflayer');
const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'CamboBot', version: '1.21.1', auth: 'offline' });
let transitions = 0;
let clicks = 0;
let spawned = false;
const timeout = setTimeout(() => { console.error('Expiration du bot'); process.exit(1); }, 100000);
bot.on('spawn', () => {
  if (!spawned) { spawned = true; console.log('CAMBO_BOT_LOGIN_OK'); }
});
bot.on('messagestr', message => {
  const match = /^CAMBO_CLICK (-?\d+) (-?\d+) (-?\d+)$/.exec(message);
  if (!match) return;
  const location = { x: Number(match[1]), y: Number(match[2]), z: Number(match[3]) };
  bot._client.write('block_place', {
    hand: 0, location, direction: 1, cursorX: 0.5, cursorY: 1, cursorZ: 0.5,
    insideBlock: false, sequence: ++clicks
  });
  console.log('CAMBO_BOT_CLICK', clicks, JSON.stringify(location));
});
bot._client.on('respawn', packet => {
  transitions++;
  console.log('CAMBO_BOT_RESPAWN', transitions, JSON.stringify(packet));
});
bot.on('kicked', reason => {
  if (!JSON.stringify(reason).includes('multiplayer.disconnect.server_shutdown')) {
    console.error('Bot expulsé', reason); process.exitCode = 1;
  }
});
bot.on('error', error => { console.error('Erreur du bot', error); process.exitCode = 1; });
bot.on('end', reason => {
  clearTimeout(timeout);
  if (spawned && clicks >= 2 && transitions >= 2 && !process.exitCode) {
    console.log('CAMBO_BOT_ROUNDTRIP_OK');
  } else {
    console.error('Test client incomplet', reason, 'transitions', transitions);
    process.exitCode = 1;
  }
});
