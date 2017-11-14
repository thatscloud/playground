PTApp.controller('indexController', function($scope, $http, userService) {

	$http.get("/rest/rankings", httpConfig).success(function(response) {
		$scope.rankings = response;
	});
});