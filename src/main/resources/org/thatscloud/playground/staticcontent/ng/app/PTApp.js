var PTApp = angular.module("PTApp",[
 'ngRoute'
 ]);

PTApp.config(function($routeProvider) {
     $routeProvider.when('/index', {
         templateUrl: 'ng/pages/index.html',
         controller: 'indexController'
     })
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
           return isNaN(input) ? input : $filter('number')(input,0) +(s[(v-20)%10]||s[v]||s[0]);
        }
});

PTApp.filter('formatPercentage', ['$filter', function ($filter) {
      return function (input, decimals) {
        return $filter('number')(input * 100, decimals) + '%';
      };
    }]);

PTApp.run( function() {
    $.tablesorter.addParser(
    {
        id: "playground-ordinal",
        is: function( s, table, cell, $cell ) { return false; },
        format: function( s, table, cell, cellIndex ) 
        {
            if( !/\d/g.test( s ) )
            {
                return s;
            }
            
            var commaSeparatedNumber = s.replace( /((?:\d+,)*(?:\d+))(?:st|nd|rd|th)/, "$1" );
            return -( commaSeparatedNumber.replace( /,/, "" ) );
        },
        parsed: false,
        type: 'numeric'
    } );
    
    $.tablesorter.addParser(
    {
        id: "playground-rank",
        is: function( s, table, cell, $cell ) { return false; },
        format: function( s, table, cell, cellIndex ) 
        {
            if( !/\d/g.test( s ) )
            {
                return s;
            }
            
            return -( s.replace( /,/, "" ) );
        },
        parsed: false,
        type: 'numeric'
    } );
});

var httpConfig = {headers: {'Accept': 'application/json'}};