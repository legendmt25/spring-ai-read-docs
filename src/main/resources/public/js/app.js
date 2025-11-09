(function () {
  // Constants
  const API_CHAT = '/api/v1/chat';
  const API_STORE = '/api/v1/store';

  // State
  let conversationId = null;

  // DOM references
  const form = document.getElementById('chat-form');
  const textarea = document.getElementById('message');
  const messagesEl = document.getElementById('messages');
  const sendButton = document.getElementById('send');

  // Store controls
  const storeFileForm = document.getElementById('store-file-form');
  const storeFileInput = document.getElementById('store-file');
  const storeFileProgress = document.getElementById('store-file-progress');
  const storeUrlForm = document.getElementById('store-url-form');
  const storeUrlInput = document.getElementById('store-url');
  const storeUrlProgress = document.getElementById('store-url-progress');

  if (!form || !textarea || !messagesEl) {
    console.warn('Chat UI elements not found. Aborting chat script.');
    return;
  }

  // Helpers
  function createMessageEl(text, cls) {
    const div = document.createElement('div');
    div.className = 'message ' + (cls ? cls.toLowerCase() : 'assistant');
    div.textContent = text;
    return div;
  }

  function createMessage(messageType, text) {
    const mt = (messageType || 'assistant').toString();
    const label = mt.charAt(0).toUpperCase() + mt.slice(1).toLowerCase();
    return `${label}: ${text}`;
  }

  function appendMessage(text, cls) {
    const el = createMessageEl(text, cls);
    messagesEl.appendChild(el);
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }

  function appendMessages(messages) {
    if (!Array.isArray(messages)) return;
    messages.forEach(msg => {
      const mtRaw = msg && msg.messageType ? msg.messageType : (msg && msg.metadata && msg.metadata.role ? msg.metadata.role : 'assistant');
      const mt = ('' + mtRaw).toLowerCase();
      const text = msg && (msg.text || msg.content || msg.message || '') || '';
      appendMessage(createMessage(mt, text), mt);
    });
  }

  function setSending(isSending) {
    if (sendButton) sendButton.disabled = isSending;
    textarea.disabled = isSending;
    if (sendButton) sendButton.textContent = isSending ? 'Sending...' : 'Send';
  }

  // Response parsing utility: supports JSON object/array or plain text
  async function parseResponseText(res) {
    const contentType = (res.headers.get('content-type') || '').toLowerCase();
    if (contentType.includes('application/json')) {
      try {
        return await res.json();
      } catch (_) {
        // fall through to plain text
      }
    }
    return await res.text();
  }

  const MessageType = {
    USER: 'user',
    ASSISTANT: 'assistant',
    SYSTEM: 'system',
    INFO: 'info',
    ERROR: 'error'
  };

  // Fetch conversation history by id
  async function getMessages(id) {
    const convId = id || conversationId;
    if (!convId) return; // nothing to fetch

    try {
      const res = await fetch(`${API_CHAT}/${encodeURIComponent(convId)}`, {
        credentials: 'same-origin'
      });
      if (!res.ok) {
        const txt = await res.text();
        appendMessage('Error: ' + res.status + ' ' + txt, MessageType.ERROR);
        return;
      }

      const data = await parseResponseText(res);
      appendMessages(data);
    } catch (err) {
      appendMessage('Network error: ' + err.message, MessageType.ERROR);
    }
  }

  // Send a message to the chat API and append the response
  async function send(message) {
    if (!message || !message.trim()) return;
    appendMessage(createMessage(MessageType.USER, message), MessageType.USER);
    setSending(true);
    try {
      const postUrl = conversationId ? `${API_CHAT}/${encodeURIComponent(conversationId)}` : API_CHAT;
      const res = await fetch(postUrl, {
        credentials: 'same-origin',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message })
      });

      if (!res.ok) {
        const txt = await res.text();
        appendMessage('Error: ' + res.status + ' ' + txt, MessageType.ERROR);
        return;
      }

      const data = await parseResponseText(res);

      // If server returns a conversationId (new conversation), store it and update URL
      if (data && typeof data === 'object' && data.conversationId) {
        conversationId = data.conversationId;
        try {
          const url = new URL(window.location.href);
          url.searchParams.set('conversationId', conversationId);
          history.replaceState(null, '', url.toString());
        } catch (_) {
          // ignore URL update errors
        }
      }

      // Support different response shapes
      let dataMessage;
      if (data && typeof data === 'object') {
        dataMessage = data.message || (Array.isArray(data) ? data[0] : null) || { messageType: MessageType.ASSISTANT, text: JSON.stringify(data) };
      } else {
        dataMessage = { messageType: MessageType.ASSISTANT, text: String(data) };
      }

      const mt = (dataMessage.messageType || dataMessage.metadata && dataMessage.metadata.role || 'assistant').toString().toLowerCase();
      const txt = dataMessage.text || dataMessage.content || dataMessage.message || '';
      appendMessage(createMessage(mt, txt), mt);
    } catch (err) {
      appendMessage('Network error: ' + err.message, MessageType.ERROR);
    } finally {
      setSending(false);
    }
  }

  // --- Store handlers ---
  function showProgress(progressEl, show) {
    if (!progressEl) return;
    progressEl.hidden = !show;
    progressEl.setAttribute('aria-hidden', String(!show));
  }

  function updateProgress(progressEl, percent) {
    if (!progressEl) return;
    progressEl.value = Math.max(0, Math.min(100, Math.round(percent)));
  }

  // Use XHR for file upload to show progress reliably (returns a Promise)
  function uploadFileToStore(file) {
    appendMessage(`Uploading file: ${file.name}`, MessageType.INFO);
    const fd = new FormData();
    fd.append('file', file);

    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', API_STORE, true);
      xhr.withCredentials = true;

      xhr.upload.addEventListener('progress', function (e) {
        if (e.lengthComputable) {
          const pct = (e.loaded / e.total) * 100;
          updateProgress(storeFileProgress, pct);
          showProgress(storeFileProgress, true);
        }
      });

      xhr.addEventListener('load', function () {
        showProgress(storeFileProgress, false);
        if (xhr.status >= 200 && xhr.status < 300) {
          appendMessage('File uploaded successfully', MessageType.ASSISTANT);
          resolve(xhr.responseText);
        } else {
          appendMessage(`Store upload error: ${xhr.status} ${xhr.responseText}`, MessageType.ERROR);
          reject(new Error(xhr.responseText || ('Status ' + xhr.status)));
        }
      });

      xhr.addEventListener('error', function () {
        showProgress(storeFileProgress, false);
        appendMessage('Network error during file upload', MessageType.ERROR);
        reject(new Error('Network error'));
      });

      xhr.send(fd);
    });
  }

  async function sendUrlToStore(url) {
    appendMessage(`Sending URL to store: ${url}`, MessageType.INFO);
    showProgress(storeUrlProgress, true);
    updateProgress(storeUrlProgress, 10);

    try {
      const res = await fetch(API_STORE, {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ url })
      });

      updateProgress(storeUrlProgress, 100);
      showProgress(storeUrlProgress, false);

      if (!res.ok) {
        const txt = await res.text();
        appendMessage('Store URL error: ' + res.status + ' ' + txt, MessageType.ERROR);
        return;
      }

      appendMessage('URL sent successfully', MessageType.ASSISTANT);
    } catch (err) {
      showProgress(storeUrlProgress, false);
      appendMessage('Network error: ' + err.message, MessageType.ERROR);
    }
  }

  // Event handlers
  form.addEventListener('submit', function (e) {
    e.preventDefault();
    const value = textarea.value.trim();
    if (!value) return;
    textarea.value = '';
    send(value);
  });

  // Ctrl+Enter to send without submitting the form natively (avoid form.submit())
  textarea.addEventListener('keydown', function (e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      const value = textarea.value.trim();
      if (!value) return;
      textarea.value = '';
      send(value);
    }
  });

  // Store file form handling
  if (storeFileForm) {
    storeFileForm.addEventListener('submit', function (e) {
      e.preventDefault();
      const file = storeFileInput.files && storeFileInput.files[0];
      if (!file) {
        appendMessage('No file selected for upload', MessageType.ERROR);
        return;
      }
      if (file.type && file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
        appendMessage('Only PDF files are supported', MessageType.ERROR);
        return;
      }
      uploadFileToStore(file).catch(()=>{});
    });
  }

  // Store URL form handling
  if (storeUrlForm) {
    storeUrlForm.addEventListener('submit', function (e) {
      e.preventDefault();
      const url = (storeUrlInput.value || '').trim();
      if (!url) {
        appendMessage('No URL provided', MessageType.ERROR);
        return;
      }
      storeUrlInput.value = '';
      sendUrlToStore(url);
    });

    storeUrlInput.addEventListener('keydown', function (e) {
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        e.preventDefault();
        storeUrlBtn.click();
      }
    });
  }

  // Init: parse conversationId from URL and fetch history if present
  function init() {
    try {
      const queryParams = new URLSearchParams(window.location.search);
      const conv = queryParams.get('conversationId');
      if (conv) {
        conversationId = conv;
        getMessages(conversationId);
      }
    } catch (err) {
      console.warn('getMessages init failed:', err);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  // Expose helpers for debugging / manual use from the console
  window.__chat = { send, getMessages, uploadFileToStore, sendUrlToStore };

})();
