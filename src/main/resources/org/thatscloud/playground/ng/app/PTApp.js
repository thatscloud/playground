var PTApp = angular.module("PTApp",[
 'ngRoute',
 'directive.g+signin'
 ]);

PTApp.config(function ($routeProvider) {
     $routeProvider.when('/', {
    	 templateUrl: 'pages/index.html',
         controller: 'indexController'
     })
     .when('/queues', {
         templateUrl: 'pages/index.html',
         controller: 'indexController'
     })
     .otherwise(
     {
    	 templateUrl: '404.html',
     });
 });
 
 var httpConfig = {headers: {'Accept': 'application/json;odata=verbose'}};
 