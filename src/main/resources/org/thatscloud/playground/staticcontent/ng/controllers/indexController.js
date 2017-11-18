PTApp.controller('indexController', function($scope, $http) {

	$http.get("/rest/rankings", httpConfig).then(function(response) {
		$scope.players = response.data.players;
	});
});