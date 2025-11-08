(function () {
  // Constants
  const API_CHAT = '/api/chat';

  // State
  let conversationId = null;

  // DOM references
  const form = document.getElementById('chat-form');
  const textarea = document.getElementById('message');
  const messagesEl = document.getElementById('messages');
  const sendButton = form ? form.querySelector('button[type="submit"]') : null;

  if (!form || !textarea || !messagesEl) {
    console.warn('Chat UI elements not found. Aborting chat script.');
    return;
  }

  // Helpers
  function createMessageEl(text, cls) {
    const div = document.createElement('div');
    div.className = 'message ' + (cls.toLowerCase() || '');
    div.textContent = text;
    return div;
  }

  function createMessage(messageType, text) {
    console.log('Creating message:', messageType, text);
    return `${messageType.charAt(0).toUpperCase() + messageType.slice(1).toLowerCase()}: ${text}`;
  }

  function appendMessage(text, cls) {
    const el = createMessageEl(text, cls);
    messagesEl.appendChild(el);
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }

  function appendMessages(messages) {
    messages.forEach(msg => {
      appendMessage(createMessage(msg.messageType, msg.text), msg.messageType);
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
    USER: "user",
    ASSISTANT: "assistant",
    SYSTEM: "system"
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
        appendMessage('Error: ' + res.status + ' ' + txt, 'error');
        return;
      }

      const data = await parseResponseText(res);
      appendMessages(data);
    } catch (err) {
      appendMessage('Network error: ' + err.message, 'error');
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
        appendMessage('Error: ' + res.status + ' ' + txt, 'error');
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

      const dataMessage = data.message || JSON.stringify(data);
      appendMessage(createMessage(dataMessage.messageType, dataMessage.text), dataMessage.messageType);
    } catch (err) {
      appendMessage('Network error: ' + err.message, 'error');
    } finally {
      setSending(false);
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
  window.__chat = { send, getMessages };

})();
