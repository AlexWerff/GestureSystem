angular.module('app', ['ngRoute', 'ngMaterial','ngFileUpload'])

.controller('AppCtrl', function ($scope, $window, $mdSidenav, dataservice) {
    $scope.toggleLeft = buildToggler('left');
    $scope.toggleRight = buildToggler('right');

    function buildToggler(componentId) {
        return function () {
            $mdSidenav(componentId).toggle();
        };
    }

    $scope.selectPage = function (page) {
        $window.location = "#/" + page;
        $scope.toggleLeft;
    }
});



angular.module("app").config(function ($routeProvider) {
    $routeProvider.
    when('/scenes', {
        templateUrl: 'app/content/scenes/scenes.html',
        controller: 'ScenesController'
    }).
    when('/scene', {
        templateUrl: 'app/content/scene/scene.html',
        controller: 'SceneController'
    }).
    when('/provider', {
        templateUrl: 'app/content/provider/provider.html',
        controller: 'ProviderController'
    }).
    when('/consumer', {
        templateUrl: 'app/content/consumer/consumer.html',
        controller: 'ConsumerController'
    }).
    when('/gestures', {
        templateUrl: 'app/content/gestures/gestures.html',
        controller: 'GesturesController'
    }).
    when('/notes', {
        templateUrl: 'app/content/notes/notes.html',
        controller: 'NotesController'
    }).
    when('/landing', {
        templateUrl: 'app/content/landing/landing.html',
        controller: 'LandingController'
    }).
    otherwise({
        redirectTo: '/landing'
    });
});