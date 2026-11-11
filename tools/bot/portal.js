'use strict';
// Client headless réel, deux connexions ; seules les préparations du terrain sont côté serveur.
const mineflayer = require('mineflayer');
let transitions = 0;
let clicks = 0;
let recalls = 0;
let connections = 0;
let reconnectRequested = false;
const timeout = setTimeout(() => { console.error('Expiration du bot'); process.exit(1); }, 100000);

function connect () {
  const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'CamboBot', version: '1.21.1', auth: 'offline' });
  bot.on('spawn', () => {
    if (!bot.camboSpawned) { bot.camboSpawned = true; connections++; console.log('CAMBO_BOT_LOGIN_OK', connections); }
  });
  bot.on('messagestr', message => {
    if (message === 'CAMBO_RECALL') {
      bot._client.write('entity_action', { entityId: bot.entity.id, actionId: 'start_sneaking', jumpBoost: 0 });
      bot._client.write('use_item', { hand: 0, sequence: 2, rotation: { x: 0, y: 0 } });
      recalls++;
      console.log('CAMBO_BOT_RECALL', recalls);
      return;
    }
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
    const encoded = JSON.stringify(reason);
    if (encoded.includes('CAMBO_RECONNECT')) reconnectRequested = true;
    else if (!encoded.includes('multiplayer.disconnect.server_shutdown')) {
      console.error('Bot expulsé', reason); process.exitCode = 1;
    }
  });
  bot.on('error', error => { console.error('Erreur du bot', error); process.exitCode = 1; });
  bot.on('end', reason => {
    if (reconnectRequested && connections === 1 && !process.exitCode) {
      reconnectRequested = false;
      setTimeout(connect, 2000);
      return;
    }
    clearTimeout(timeout);
    if (connections === 2 && clicks >= 1 && recalls >= 1 && transitions >= 2 && !process.exitCode) {
      console.log('CAMBO_BOT_ROUNDTRIP_OK');
    } else {
      console.error('Test client incomplet', reason, 'connexions', connections, 'transitions', transitions);
      process.exitCode = 1;
    }
  });
}
connect();
