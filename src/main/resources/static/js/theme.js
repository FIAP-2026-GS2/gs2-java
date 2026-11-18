(function () {
    const KEY = 'sampatech-theme';
    const root = document.documentElement;
    const options = document.querySelectorAll('.theme-option');
    if (!options.length) return;

    const ICONS = {
        light: { still: '/images/theme/sun.svg', anim: '/images/theme/sun.gif', duration: 1170 },
        dark: { still: '/images/theme/moon.svg', anim: '/images/theme/moon.gif', duration: 2400 }
    };
    const timers = {};

    function currentTheme() {
        return root.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
    }

    function markActive() {
        options.forEach(function (option) {
            const active = option.dataset.themeValue === currentTheme();
            option.classList.toggle('active', active);
            option.setAttribute('aria-pressed', active);
        });
    }

    function save(theme) {
        try {
            localStorage.setItem(KEY, theme);
        } catch (e) {
        }
    }

    function play(option) {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
        const theme = option.dataset.themeValue;
        const img = option.querySelector('img');

        clearTimeout(timers[theme]);
        img.src = ICONS[theme].anim + '?t=' + Date.now();
        timers[theme] = setTimeout(function () {
            img.src = ICONS[theme].still;
        }, ICONS[theme].duration);
    }

    markActive();

    options.forEach(function (option) {
        option.addEventListener('click', function () {
            const theme = option.dataset.themeValue;
            if (theme === currentTheme()) return;

            root.setAttribute('data-bs-theme', theme);
            save(theme);
            markActive();
            play(option);
        });
    });
})();