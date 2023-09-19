(function (window) {
  window.__env = window.__env || {};
  window.__env.socketEndpointConnect = '/dashboardengine/dsengine/solver';
  window.__env.socketEndpointSend = '/dsengine/solver';
  window.__env.socketEndpointSubscribe = '/dsengine/broker';
  window.__env.endpointControlPanel = '/controlpanel';
  window.__env.endpointDashboardEngine = '/dashboardengine';
  window.__env.dashboardEngineUsername = 'administrator';
  window.__env.dashboardEnginePassword = 'changeIt!';
  window.__env.dashboardEngineLoginRest = '/loginRest';
  window.__env.dashboardEngineLoginRestTimeout = 5000;
  window.__env.enableDebug = false;
  window.__env.urlParameters = {};
  window.__env.dashboardCheckHeaderLibs=true;
}(this));
