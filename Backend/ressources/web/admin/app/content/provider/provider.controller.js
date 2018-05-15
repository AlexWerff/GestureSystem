angular.module("app").controller("ProviderController", function ($rootScope, $scope, $mdDialog, dataservice) {
    $scope.provider = [];
    $scope.scenes = [];

    var load = function () {
        $scope.provider = [];
        dataservice.getProviderConfigs(function (configs) {
            $scope.provider = configs;
            dataservice.getModel(function (model) {
                $scope.scenes = model.scenes;
                $scope.provider.forEach(function (config) {
                    var configScene = $scope.scenes.filter(function (obj) {
                        return obj.identifier == config.sceneIdentifier;
                    })[0];
                    config.scene = configScene;
                    config.model = getAllModels(configScene).filter(function (sc) {
                        return sc.identifier == config.modelIdentifier;
                    })[0];
                });
            });
        });
    }
    load();

    $scope.addProviderClick = function (ev) {
        $mdDialog.show({
            locals: {
                scenes: $scope.scenes
            },
            clickOutsideToClose: true,
            controllerAs: 'ctrl',
            templateUrl: 'app/content/provider/addDialog.html',
            controller: addDialogCtrl,
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false
        }).then(function (prov) {
            dataservice.updateProviderConfig(prov, function () {
                load();
            });
        });
    }

    $scope.deleteProviderClick = function (prov, ev) {
        var confirm = $mdDialog.confirm()
            .title('Confirm')
            .textContent('Are you sure you want to delete this provider?')
            .targetEvent(ev)
            .ok('Delete')
            .cancel('Cancel');

        $mdDialog.show(confirm).then(function () {
            dataservice.deleteProviderConfig(prov, function () {
                load();
            });
        }, function () {
            //Cancel
        });
    }

    $scope.editProviderClick = function (prov, ev) {
        var identifier = prov.sceneIdentifier;
        var scene = $scope.scenes.filter(function (obj) {
            return obj.identifier == identifier;
        })[0];
        $mdDialog.show({
            locals: {
                provider: prov,
                scenes: $scope.scenes
            },
            clickOutsideToClose: true,
            controllerAs: 'ctrl',
            templateUrl: 'app/content/provider/editDialog.html',
            controller: editDialogCtrl,
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false
        }).then(function (prov) {
            dataservice.updateProviderConfig(prov, function () {
                load();
            });
        });
    }

    var getAllModels = function (parent) {
        var result = [];
        parent.models.forEach(function (model) {
            result.push(model);
            var next = getAllModels(model);
            next.forEach(function (subModel) {
                result.push(subModel);
            });
        });
        return result;
    };

    var addDialogCtrl = function ($scope, scenes) {
        $scope.types = [
            {
                "name": "Speech",
                type: "SpeechProviderConfig"
            },
            {
                "name": "Skeleton",
                type: "SkeletonProviderConfig"
            }
            ];
        var getAllModels = function (parent) {
            var result = [];
            if (parent !== undefined) {
                parent.models.forEach(function (model) {
                    result.push(model);
                    var next = getAllModels(model);
                    next.forEach(function (subModel) {
                        result.push(subModel);
                    });
                });
            }
            return result;
        };
        $scope.scenes = scenes;
        $scope.models = [];
        $scope.currentProvider = {
            model: {},
            scene: {},
            types:[]
        };
        
        $scope.exists = function(type){
            return $scope.currentProvider.types.filter(function(t){
                return t.type === type.type;
            }).length !== 0;   
        }
        
        $scope.toggle = function(type){
            if($scope.exists(type)){
                $scope.currentProvider.types.splice($scope.selectedTypes.indexOf(type),1);
            }
            else{
                $scope.currentProvider.types.push(type);
            }  
        }
        $scope.sceneChanged = function () {
            $scope.models = getAllModels(provider.scene);
        }

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.save = function () {
            $mdDialog.hide($scope.currentProvider);
        };

    }

    var editDialogCtrl = function ($scope, provider, scenes) {
        var getAllModels = function (parent) {
            var result = [];
            if (parent !== undefined) {
                parent.models.forEach(function (model) {
                    result.push(model);
                    var next = getAllModels(model);
                    next.forEach(function (subModel) {
                        result.push(subModel);
                    });
                });
            }
            return result;
        };
        $scope.currentProvider = provider;
        $scope.models = getAllModels(provider.scene);
        $scope.scenes = scenes;

        $scope.sceneChanged = function () {
            $scope.models = getAllModels(provider.scene);
        }

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.save = function () {
            $mdDialog.hide($scope.currentProvider);
        };


    }
});