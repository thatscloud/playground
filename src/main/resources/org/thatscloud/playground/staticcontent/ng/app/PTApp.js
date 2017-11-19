var PTApp = angular.module("PTApp",[
 'ngRoute'
 ]);

PTApp.config(function($routeProvider) {
     $routeProvider.when('/', {
    	 templateUrl: 'ng/pages/index.html',
         controller: 'indexController'
     })
     .otherwise(
     {
    	 templateUrl: 'ng/pages/404.html'
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

PTApp.filter('formatDecimal', function ($filter) {
	  return function (input) {
	    return isNaN(input) ? input : $filter('strReplace')($filter('number')(input,2),',','');
	    		
	  };
	});
 
PTApp.filter('formatOrdinal', function ($filter) {
	return function(input) {
		   var s=["th","st","nd","rd"],
		       v=input%100;
		   return input+(s[(v-20)%10]||s[v]||s[0]);
		}
});

PTApp.filter('formatPercentage', ['$filter', function ($filter) {
	  return function (input, decimals) {
	    return $filter('number')(input * 100, decimals) + '%';
	  };
	}]);

var httpConfig = {headers: {'Accept': 'application/json;odata=verbose'}};
 