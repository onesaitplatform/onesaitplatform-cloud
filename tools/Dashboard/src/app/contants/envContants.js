var env = {};

// Import variables if present (from env.js)
if(window && window.__env){
  Object.assign(env, window.__env);
  angular.module('dashboardFramework').constant('__env', env);
}
else{//Default config
  console.info("__env properties not defined globally, manual definition is required")
}
