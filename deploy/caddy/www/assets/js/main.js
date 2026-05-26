(function(){'use strict';
var L={zh:{heroTitle:'临床研究数据管理平台',heroSub:'面向研究者发起的临床试验的研究电子数据采集系统。从方案设计到数据分析，安全可靠。',signIn:'登录系统',version:'ResearchEDC 3.18-SNAPSHOT',welcomeBack:'登录',loginSub:'输入您的账号信息',username:'用户名',usernamePlaceholder:'用户名',password:'密码',passwordPlaceholder:'密码',loginBtn:'登录',loginInfoTitle:'研究数据管理平台',loginInfoDesc:'ResearchEDC 提供安全可靠的临床研究数据采集与管理服务，支持电子 CRF、受试者管理、随机化和数据导出等核心功能。',loginFeat1:'电子 CRF 设计与部署',loginFeat2:'受试者全程管理',loginFeat3:'数据导出与随机化'},
en:{heroTitle:'Clinical Data Management Platform',heroSub:'A secure research EDC platform for investigator-initiated clinical trials. From protocol design to data analysis.',signIn:'Sign In',version:'ResearchEDC 3.18-SNAPSHOT',welcomeBack:'Sign In',loginSub:'Enter your credentials',username:'Username',usernamePlaceholder:'Username',password:'Password',passwordPlaceholder:'Password',loginBtn:'Sign In',loginInfoTitle:'Research Data Platform',loginInfoDesc:'ResearchEDC provides secure clinical research data capture and management with eCRF, subject management, randomization, and data export.',loginFeat1:'eCRF Design & Deployment',loginFeat2:'Subject Lifecycle Management',loginFeat3:'Data Export & Randomization'}};

var lang=localStorage.getItem('rlang')||'zh',theme=localStorage.getItem('rtheme')||'dark';
function applyLang(l){
  lang=l;localStorage.setItem('rlang',l);document.documentElement.lang=l;
  document.querySelectorAll('[data-i18n]').forEach(function(e){
    var k=e.dataset.i18n;if(L[l]&&L[l][k])e.textContent=L[l][k];
  });
  document.querySelectorAll('[data-i18n-placeholder]').forEach(function(e){
    var k=e.dataset.i18nPlaceholder;if(L[l]&&L[l][k])e.placeholder=L[l][k];
  });
  document.querySelectorAll('.lang-toggle,.controls button:last-child').forEach(function(e){
    e.textContent=l==='zh'?'EN':'中';
  });
}
function applyTheme(t){
  theme=t;localStorage.setItem('rtheme',t);document.documentElement.setAttribute('data-theme',t);
}
window.toggleLang=function(){applyLang(lang==='zh'?'en':'zh')};
window.toggleTheme=function(){applyTheme(theme==='dark'?'light':'dark')};
applyLang(lang);applyTheme(theme);
})();
