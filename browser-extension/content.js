// Robo Web Capture – Content Script
const SERVER_URL = 'http://127.0.0.1:7890';
let isRecording = false;
let lastEventTime = Date.now();

// Route fetch through background service worker to bypass CORS restrictions
function bgFetch(url, options = {}) {
    return new Promise((resolve, reject) => {
        chrome.runtime.sendMessage({ type: 'fetch', url, options }, response => {
            if (chrome.runtime.lastError) { reject(new Error(chrome.runtime.lastError.message)); return; }
            resolve(response || { ok: false });
        });
    });
}

async function checkStatus() {
    try {
        const resp = await bgFetch(`${SERVER_URL}/status`);
        isRecording = resp.ok;
        console.log('[MACRobo] Status:', isRecording ? 'connected ✓' : 'disconnected');
    } catch (e) {
        console.warn('[MACRobo] Status-Check fehlgeschlagen:', e.message);
        isRecording = false;
    }
}

function getCssSelector(el) {
    if (!el || el === document.body) return 'body';
    // Stable attributes first
    if (el.id) return `#${CSS.escape(el.id)}`;
    for (const attr of ['data-testid', 'data-id', 'data-cy', 'aria-label', 'name', 'type']) {
        const val = el.getAttribute(attr);
        if (val) return `${el.tagName.toLowerCase()}[${attr}="${val}"]`;
    }
    // Fallback: tag + classes (stable, non-random)
    const tag = el.tagName.toLowerCase();
    const stableClasses = [...el.classList].filter(c => !/\d{3,}/.test(c)).slice(0, 2);
    const base = stableClasses.length ? `${tag}.${stableClasses.join('.')}` : tag;
    // Add nth-of-type if needed for uniqueness
    const parent = el.parentElement;
    if (!parent) return base;
    const siblings = [...parent.children].filter(c => c.tagName === el.tagName);
    if (siblings.length > 1) {
        const idx = siblings.indexOf(el) + 1;
        return `${getCssSelector(parent)} > ${tag}:nth-of-type(${idx})`;
    }
    return `${getCssSelector(parent)} > ${base}`;
}

function getXPath(el) {
    if (!el || el.nodeType !== Node.ELEMENT_NODE) return '';
    if (el.id) return `//*[@id="${el.id}"]`;
    const parts = [];
    let current = el;
    while (current && current.nodeType === Node.ELEMENT_NODE && current !== document.documentElement) {
        const tag = current.tagName.toLowerCase();
        const siblings = [...(current.parentNode?.children || [])].filter(c => c.tagName === current.tagName);
        const idx = siblings.length > 1 ? `[${siblings.indexOf(current) + 1}]` : '';
        parts.unshift(`${tag}${idx}`);
        current = current.parentNode;
    }
    return '//' + parts.join('/');
}

async function sendEvent(type, el, payload = null) {
    if (!isRecording || !el) return;
    const now = Date.now();
    const timing = Math.min(now - lastEventTime, 30000);
    lastEventTime = now;
    const text = (el.innerText || el.value || el.getAttribute('aria-label') || '').trim().substring(0, 80) || null;
    try {
        await bgFetch(`${SERVER_URL}/capture`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                eventType: type,
                cssSelector: getCssSelector(el),
                xpath: getXPath(el),
                textContent: text,
                payload: payload,
                timingMs: timing,
                url: window.location.href
            })
        });
    } catch (e) {
        console.warn('[MACRobo] Event senden fehlgeschlagen:', e.message);
        isRecording = false;
    }
}

// Capture clicks (on interactive elements)
document.addEventListener('click', e => {
    const el = e.target.closest('a,button,input,select,textarea,[role="button"],[onclick]') || e.target;
    sendEvent('CLICK', el);
}, true);

// Capture text input
document.addEventListener('change', e => {
    const el = e.target;
    if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.tagName === 'SELECT') {
        sendEvent('TYPE', el, el.value || null);
    }
}, true);

console.log('[MACRobo] Content Script geladen, Server:', SERVER_URL);
// Poll recording status every 2 seconds
setInterval(checkStatus, 2000);
checkStatus();
