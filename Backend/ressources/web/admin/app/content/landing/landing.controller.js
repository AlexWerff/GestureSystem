angular.module("app").controller("LandingController",function($scope,dataservice){
    $scope.info = {};
    
    var load = function(){
        dataservice.getInfo(function(data){
            $scope.info = data; 
        });
    };
    
    load();
});