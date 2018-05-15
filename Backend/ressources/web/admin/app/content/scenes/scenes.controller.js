angular.module("app").controller("ScenesController", function ($scope, $window, $mdDialog, $http, dataservice) {
    $scope.scenes = [];
    dataservice.getModel(function (model) {
        $scope.scenes = model.scenes;
    });

    $scope.deleteSceneClick = function (scene, ev) {
        var confirm = $mdDialog.confirm()
            .title('Confirm')
            .textContent('Are you sure you want to delete this scene?')
            .targetEvent(ev)
            .ok('Delete')
            .cancel('Cancel');

        $mdDialog.show(confirm).then(function () {
            //Delete Model
        }, function () {
            //Cancel
        });
    }

    $scope.addScenesClick = function (ev) {
        $mdDialog.show({
            clickOutsideToClose: true,
            controllerAs: 'ctrl',
            templateUrl: 'app/content/scenes/addDialog.html',
            controller: addDialogCtrl,
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false
        }).then(function (newModel) {
            $window.alert(newModel);
            dataservice.postModel(newModel, function () {
                dataservice.getModel(function (model) {
                    $scope.scenes = model.scenes;
                });
            });
        });
    };

    var addDialogCtrl = function ($scope) {
        $scope.hide = function () {
            $mdDialog.hide();
        };
        $scope.$watch("file", function (file) {
            if (file) {
                var aReader = new FileReader();
                aReader.readAsText(file, "UTF-8");
                aReader.onload = function (evt) {
                    $scope.fileContent = aReader.result;
                    $scope.newModel = JSON.parse($scope.fileContent);
                    $scope.numberScenes = Object.keys($scope.newModel.Scenes).length;
                }
                aReader.onerror = function (evt) {
                    $scope.fileContent = "error";
                }
            }
        });


        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.add = function () {
            if ($scope.file === undefined) {
                $mdDialog.cancel();
            } else {
                $mdDialog.hide($scope.newModel);
            }
        };
    }

});