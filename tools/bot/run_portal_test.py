"""Démarre le serveur de CI, attend sa disponibilité puis lance le vrai client headless."""
import os
import pathlib
import subprocess
import time

ROOT = pathlib.Path(__file__).resolve().parents[2]
RUN = ROOT / 'build' / 'servercheck'
RUN.mkdir(parents=True, exist_ok=True)
(RUN / 'eula.txt').write_text('eula=true\n', encoding='utf8')
(RUN / 'server.properties').write_text('online-mode=false\nserver-port=25565\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n', encoding='utf8')
log_path = ROOT / 'servercheck-bot.log'
env = dict(os.environ, CAMBO_BOT_CHECK='1')
with log_path.open('w', encoding='utf8') as log:
    server = subprocess.Popen(['./gradlew', 'runServercheck', '--console=plain'], cwd=ROOT,
                              env=env, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
    try:
        deadline = time.monotonic() + 240
        while time.monotonic() < deadline:
            if server.poll() is not None:
                raise RuntimeError('Le serveur s’est arrêté avant le client')
            if 'Done (' in log_path.read_text(encoding='utf8', errors='replace'):
                break
            time.sleep(1)
        else:
            raise TimeoutError('Démarrage serveur trop long')
        result = subprocess.run(['node', 'tools/bot/portal.js'], cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=120)
        print('BOT OUTPUT:', result.stdout[-4000:])
        if result.returncode != 0:
            raise RuntimeError('Client headless en échec')
        server.wait(timeout=30)
        if server.returncode != 0 or 'CAMBO_PLAYER_ROUNDTRIP_OK' not in log_path.read_text(encoding='utf8', errors='replace'):
            raise RuntimeError('Scénario serveur incomplet')
    except Exception:
        print('SERVER OUTPUT:', '\n'.join(log_path.read_text(encoding='utf8', errors='replace').splitlines()[-18:]))
        raise
    finally:
        if server.poll() is None:
            import signal
            os.killpg(server.pid, signal.SIGTERM)
            server.wait(timeout=30)
