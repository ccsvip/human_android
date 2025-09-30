/**
 * 欢迎页面交互逻辑
 * TODO: 后续可扩展更多配置选项和动画效果
 */

// Logo配置对象 - TODO: 后续可通过接口或配置文件动态加载
const LogoConfig = {
    // TODO: 支持动态配置logo图标路径
    iconPath: './images/default_digital_human.svg',
    // TODO: 支持主题色配置
    iconColor: '#3B82F6',
    lightEffectEnabled: true,
    // TODO: 根据设备性能自动调整光效
    lightEffectColor: '#3B82F6',
    // TODO: 提供光效动画自定义选项
    lightEffectIntensity: 'medium' // low, medium, high
};

// 页面配置
const WelcomeConfig = {
    // 选择类型常量
    CHOICE_LOCAL: 'local',
    CHOICE_CLOUD: 'cloud',

    // 动画配置
    animationDuration: 300,

    // 调试模式
    debugMode: false
};

/**
 * 用户选择处理函数
 * @param {string} choice - 用户选择 ('local' 或 'cloud')
 */
function selectOption(choice) {
    if (WelcomeConfig.debugMode) {
        console.log('用户选择:', choice);
    }

    // 添加选中状态视觉反馈
    const selectedElement = document.getElementById(choice + 'Option');
    if (selectedElement) {
        selectedElement.style.transform = 'scale(0.98)';
        selectedElement.style.transition = 'transform 0.1s ease-in-out';

        // 恢复动画
        setTimeout(() => {
            selectedElement.style.transform = 'scale(1)';
        }, 100);
    }

    // 通过Android WebView JavaScript Bridge传递选择结果
    if (typeof AndroidInterface !== 'undefined') {
        // 生产环境：通过Android接口传递选择
        AndroidInterface.onUserChoice(choice);
    } else {
        // 开发环境：模拟选择结果
        if (WelcomeConfig.debugMode) {
            alert(`模拟选择: ${choice === WelcomeConfig.CHOICE_LOCAL ? '本地数字人' : '云端数字人'}`);
        }

        // 模拟页面跳转延迟
        setTimeout(() => {
            simulatePageTransition(choice);
        }, WelcomeConfig.animationDuration);
    }
}

/**
 * 模拟页面过渡动画（开发调试用）
 * @param {string} choice - 用户选择
 */
function simulatePageTransition(choice) {
    const body = document.body;
    body.style.opacity = '0';
    body.style.transition = `opacity ${WelcomeConfig.animationDuration}ms ease-out`;

    setTimeout(() => {
        console.log(`跳转到 ${choice === WelcomeConfig.CHOICE_LOCAL ? '本地' : '云端'} 数字人界面`);
    }, WelcomeConfig.animationDuration);
}

/**
 * 页面初始化
 */
function initWelcomePage() {
    // TODO: 根据设备性能调整光效强度
    if (LogoConfig.lightEffectEnabled) {
        adjustLightEffects();
    }

    // 添加触摸反馈
    addTouchFeedback();

    // 检查是否为调试模式
    if (window.location.search.includes('debug=true')) {
        WelcomeConfig.debugMode = true;
        console.log('欢迎页面调试模式已启用');
    }

    // 通知Android页面已加载完成
    if (typeof AndroidInterface !== 'undefined') {
        AndroidInterface.onPageLoaded();
    }
}

/**
 * 根据设备性能调整光效
 * TODO: 实现性能检测和自动调整
 */
function adjustLightEffects() {
    // 简单的性能检测 - 后续可扩展
    const isLowPerformance = window.navigator.hardwareConcurrency < 4;

    if (isLowPerformance && LogoConfig.lightEffectIntensity === 'high') {
        // 降低光效强度
        LogoConfig.lightEffectIntensity = 'medium';

        // 应用低性能优化
        const lightRings = document.querySelectorAll('.light-ring');
        lightRings.forEach(ring => {
            ring.style.animationDuration = '4s'; // 减慢动画速度
        });
    }
}

/**
 * 添加触摸反馈效果
 */
function addTouchFeedback() {
    const choiceButtons = document.querySelectorAll('.choice-button');

    choiceButtons.forEach(button => {
        // 触摸开始
        button.addEventListener('touchstart', function(e) {
            this.style.transform = 'scale(0.98)';
            this.style.transition = 'transform 0.1s ease-in-out';
        });

        // 触摸结束
        button.addEventListener('touchend', function(e) {
            this.style.transform = 'scale(1)';
        });

        // 触摸取消
        button.addEventListener('touchcancel', function(e) {
            this.style.transform = 'scale(1)';
        });
    });
}

/**
 * 页面可见性变化处理
 */
function handleVisibilityChange() {
    if (document.hidden) {
        // 页面隐藏时暂停动画以节省性能
        const lightRings = document.querySelectorAll('.light-ring');
        lightRings.forEach(ring => {
            ring.style.animationPlayState = 'paused';
        });
    } else {
        // 页面显示时恢复动画
        const lightRings = document.querySelectorAll('.light-ring');
        lightRings.forEach(ring => {
            ring.style.animationPlayState = 'running';
        });
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', initWelcomePage);

// 监听页面可见性变化
document.addEventListener('visibilitychange', handleVisibilityChange);

// 防止页面缩放
document.addEventListener('touchmove', function(e) {
    if (e.scale !== 1) {
        e.preventDefault();
    }
}, { passive: false });

// TODO: 后续可添加更多功能
// - 主题切换（浅色/深色模式）
// - 语言切换
// - 无障碍支持
// - 更丰富的动画效果
// - 统计和分析埋点