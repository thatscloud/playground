PTApp.directive('tablesorter', function( $timeout ) {
    return {
        restrict: "C",
        link: 
            function( scope, element, attrs, controller ) {
                $( "#resultsTable" ).tablesorter(
                {
                    sortInitialOrder: "desc",
                    sortReset: "true",
                    stringTo: "bottom"
                } );
                scope.$watch( 
                    "players",
                    function() {
                        console.log( "ng watch applied to table" );
                        $timeout( function() {
                            $( "#resultsTable" ).trigger( "update" );
                        } );
                    },
                    true );
            }
    };
});
