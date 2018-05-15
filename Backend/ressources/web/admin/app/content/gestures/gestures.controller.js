angular.module("app").controller("GesturesController", function ($scope, $window,$mdDialog, dataservice) {
    $scope.gestures = [];
    dataservice.getGestures(function (gestures) {
        $scope.gestures = gestures;
    });

    $scope.gestureActiveChanged = function(gesture){
        dataservice.postGesture(gesture,function(){
            
        });
    }

});