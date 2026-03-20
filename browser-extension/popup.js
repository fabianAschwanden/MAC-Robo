const SERVER_URL = 'http://127.0.0.1:7890';

async function updateStatus() {
    const dot = document.getElementById('dot');
    const label = document.getElementById('statusLabel');
    const urlEl = document.getElementById('serverUrl');
    const hint = document.getElementById('hint');
    try {
        const resp = await fetch(`${SERVER_URL}/status`, { signal: AbortSignal.timeout(1500) });
        if (resp.ok) {
            dot.className = 'dot active';
            label.textContent = 'Aufzeichnung läuft';
            urlEl.textContent = `${SERVER_URL}`;
            hint.textContent = 'Browser-Interaktionen werden aufgezeichnet. Klicke auf Elemente, um sie zu erfassen.';
        }
    } catch {
        dot.className = 'dot inactive';
        label.textContent = 'Nicht verbunden';
        urlEl.textContent = '';
        hint.textContent = 'Starte die Aufzeichnung in der Robo-App, dann verbindet sich die Extension automatisch.';
    }
}

updateStatus();
setInterval(updateStatus, 2000);
