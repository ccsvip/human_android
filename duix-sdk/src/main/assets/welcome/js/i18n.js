/**
 * WebView国际化管理
 *
 * 功能：
 * - 为WebView页面提供国际化文本
 * - 支持JavaScript Bridge调用
 * - 管理HTML页面的多语言文本
 */

const i18n = {
    zh: {
        // 欢迎页面
        welcome_title: "索灵数字人",
        welcome_subtitle: "请选择一种方式开始您的智能体验",
        local_mode_title: "本地数字人",
        local_mode_desc: "在您的设备上本地运行，无需网络，保护隐私",
        cloud_mode_title: "云端数字人",
        cloud_mode_desc: "通过云服务访问,获取更强大的AI能力",
        footer_text: "AI驱动 · 智能交互 · 技术支持",
        language_selector: "语言",
        language_chinese: "中文",
        language_english: "English"
    },
    en: {
        // Welcome page
        welcome_title: "Suoling Digital Human",
        welcome_subtitle: "Choose a mode to start your intelligent experience",
        local_mode_title: "Local Digital Human",
        local_mode_desc: "Run locally on your device, no network required, privacy protected",
        cloud_mode_title: "Cloud Digital Human",
        cloud_mode_desc: "Access via cloud service for more powerful AI capabilities",
        footer_text: "AI Powered · Intelligent Interaction · Technical Support",
        language_selector: "Language",
        language_chinese: "中文",
        language_english: "English"
    }
};

// 当前语言
let currentLanguage = 'zh';

/**
 * 设置当前语言
 */
function setLanguage(lang) {
    currentLanguage = lang;
    updatePageText();
}

/**
 * 获取文本
 */
function getText(key) {
    return i18n[currentLanguage][key] || key;
}

/**
 * 更新页面文本
 */
function updatePageText() {
    const elements = document.querySelectorAll('[data-i18n]');
    elements.forEach(element => {
        const key = element.getAttribute('data-i18n');
        element.textContent = getText(key);
    });
}

/**
 * Android调用：设置语言
 */
function setLanguageFromAndroid(lang) {
    setLanguage(lang);
}

/**
 * 获取当前语言
 */
function getCurrentLanguage() {
    return currentLanguage;
}

/**
 * 初始化语言（从Android传入）
 */
window.initLanguage = function(lang) {
    currentLanguage = lang || 'zh';
    updatePageText();
};

/**
 * 页面加载时的默认语言处理
 */
window.addEventListener('DOMContentLoaded', function() {
    // 默认中文，等待Android传入实际语言
    currentLanguage = 'zh';
    updatePageText();
});
