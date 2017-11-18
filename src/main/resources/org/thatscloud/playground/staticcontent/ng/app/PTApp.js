var PTApp = angular.module("PTApp",[
 'ngRoute'
 ]);

PTApp.config(function($routeProvider) {
     $routeProvider.when('/', {
    	 templateUrl: 'ng/pages/index.html',
         controller: 'indexController'
     })
     .when('/queues', {
         templateUrl: 'ng/pages/index.html',
         controller: 'indexController'
     })
     .otherwise(
     {
    	 templateUrl: '404.html',
     });
 });

PTApp.filter('strReplace', function () {
	  return function (input, from, to) {
	    input = input || '';
	    from = from || '';
	    to = to || '';
	    return input.replace(new RegExp(from, 'g'), to);
	  };
	});

PTApp.filter('formatDecimal', function () {
	  return function (input) {
	    val = isNaN(input) ? input : sprintf( "%.2f", input );
	  };
	});
 
 var httpConfig = {headers: {'Accept': 'application/json;odata=verbose'}};
 