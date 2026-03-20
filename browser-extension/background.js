// Robo Web Capture – Background Service Worker
chrome.action.onClicked.addListener(() => {
    chrome.action.openPopup();
});

// Proxy fetch requests from content scripts (bypasses CORS for private network)
chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
    if (message.type !== 'fetch') return false;
    fetch(message.url, message.options || {})
        .then(resp => resp.text().then(text => sendResponse({ ok: resp.ok, status: resp.status, text })))
        .catch(err => sendResponse({ ok: false, error: err.message }))
        .catch(() => {}); // ignore errors if port closed after navigation
    return true; // keep channel open for async response
});
