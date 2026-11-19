(function () {
    const widget = document.getElementById('chat-widget');
    if (!widget) return;

    const toggle = document.getElementById('chat-widget-toggle');
    const closeButton = document.getElementById('chat-widget-close');
    const resetButton = document.getElementById('chat-widget-reset');
    const form = document.getElementById('chat-widget-form');
    const input = document.getElementById('chat-widget-input');
    const messages = document.getElementById('chat-widget-messages');

    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    const ERROR_MESSAGE = 'Não consegui responder agora. Tente de novo em instantes.';

    let historyPromise = null;
    let busy = false;

    function buildHeaders(json) {
        const headers = {};
        if (json) headers['Content-Type'] = 'application/json';
        if (tokenMeta && headerMeta && tokenMeta.content && headerMeta.content) {
            headers[headerMeta.content] = tokenMeta.content;
        }
        return headers;
    }

    function scrollToBottom() {
        messages.scrollTop = messages.scrollHeight;
    }

    function addMessage(text, author) {
        const div = document.createElement('div');
        div.className = 'chat-msg chat-msg-' + author;
        div.textContent = text;
        messages.appendChild(div);
        scrollToBottom();
    }

    function addTyping() {
        const div = document.createElement('div');
        div.className = 'chat-msg chat-msg-bot chat-typing';
        div.innerHTML = '<span></span><span></span><span></span>';
        messages.appendChild(div);
        scrollToBottom();
        return div;
    }

    function showWelcome() {
        if (!messages.children.length) {
            addMessage('Olá! Pergunte sobre as ocorrências da cidade. Exemplo: "Tem buraco aberto na Sé?"', 'bot');
        }
    }

    async function loadHistory() {
        try {
            const response = await fetch('/chat/history', { headers: buildHeaders(false) });
            if (response.ok) {
                const history = await response.json();
                history.forEach(function (item) {
                    addMessage(item.question, 'user');
                    addMessage(item.answer, 'bot');
                });
            }
        } catch (e) {
        }
        showWelcome();
    }

    function ensureHistory() {
        if (!historyPromise) historyPromise = loadHistory();
        return historyPromise;
    }

    function open() {
        widget.classList.add('open');
        toggle.setAttribute('aria-expanded', 'true');
        toggle.setAttribute('aria-label', 'Fechar chat');
        ensureHistory();
        setTimeout(function () {
            input.focus();
        }, 200);
    }

    function close() {
        widget.classList.remove('open');
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-label', 'Abrir chat');
    }

    async function send(text) {
        if (!text || busy) return;

        busy = true;
        addMessage(text, 'user');
        const typing = addTyping();

        try {
            const response = await fetch('/chat/ask', {
                method: 'POST',
                headers: buildHeaders(true),
                body: JSON.stringify({ message: text })
            });
            const data = response.ok ? await response.json() : null;
            typing.remove();
            addMessage(data && data.answer ? data.answer : ERROR_MESSAGE, 'bot');
        } catch (e) {
            typing.remove();
            addMessage(ERROR_MESSAGE, 'bot');
        }

        busy = false;
        input.focus();
    }

    async function ask(text) {
        open();
        await ensureHistory();
        send(text);
    }

    toggle.addEventListener('click', function () {
        if (widget.classList.contains('open')) {
            close();
        } else {
            open();
        }
    });

    closeButton.addEventListener('click', close);

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && widget.classList.contains('open')) close();
    });

    resetButton.addEventListener('click', async function () {
        try {
            await fetch('/chat/history', { method: 'DELETE', headers: buildHeaders(false) });
        } catch (e) {
        }
        messages.innerHTML = '';
        showWelcome();
        input.focus();
    });

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        const text = input.value.trim();
        if (!text) return;
        input.value = '';
        send(text);
    });

    document.querySelectorAll('[data-chat-question]').forEach(function (button) {
        button.addEventListener('click', function () {
            ask(button.getAttribute('data-chat-question'));
        });
    });

    window.SampaTechChat = { open: open, ask: ask };
})();
