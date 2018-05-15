angular.module("app").controller("SceneController", function ($scope, $routeParams, $mdDialog, dataservice) {
    $scope.scene = {};
    $scope.model = {}
    var load = function () {
        dataservice.getModel(function (model) {
            $scope.model = model;
            var identifier = $routeParams.id;
            var scenes = model.scenes.filter(function (obj) {
                return obj.identifier == identifier;
            });
            $scope.scene = scenes[0];
        });
    }
    load();

    $scope.expandModelClick = function (model) {
        model.expanded = !model.expanded;
    };

    $scope.deleteModelClick = function (model, ev) {
        var confirm = $mdDialog.confirm()
            .title('Confirm')
            .textContent('Are you sure you want to delete this model?')
            .targetEvent(ev)
            .ok('Delete')
            .cancel('Cancel');

        $mdDialog.show(confirm).then(function () {
            //Delete Model
            dataservice.deleteModel($scope.scene.identifier, model, function () {
                load();
            });
        }, function () {
            //Cancel
        });
    }

    $scope.addSubModelClick = function (parentModel, ev) {
        $mdDialog.show({
            locals: {
                models: getAllModels($scope.scene),
                parent: parentModel,
                prefabs: $scope.model.prefabs
            },
            clickOutsideToClose: true,
            controllerAs: 'ctrl',
            templateUrl: 'app/content/scene/addDialog.html',
            controller: addDialogCtrl,
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false
        }).then(function (newModel) {
            dataservice.updateModel($scope.scene.identifier, newModel, function () {
                load();
            });
        }, function () {

        });
    };

    $scope.editModelClick = function (selectedModel, ev) {
        try {
            selectedModel.parent.models = selectedModel.parent.models.filter(function (ob) {
                return ob.name != selectedModel.name;
            });
        } catch (err) {

        }
        $mdDialog.show({
            locals: {
                model: selectedModel,
                models: getAllModels($scope.scene)
            },
            clickOutsideToClose: true,
            controllerAs: 'ctrl',
            templateUrl: 'app/content/scene/editDialog.html',
            controller: editDialogCtrl,
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false
        }).then(function (newModel) {
            newModel.parent.models.push(newModel);
            dataservice.updateModel($scope.scene.identifier, newModel, function () {
                load();
            });
        }, function () {
            selectedModel.parent.models.push(selectedModel);
        });
    };

    var editDialogCtrl = function ($scope, model, models) {
        $scope.currentModel = model;
        $scope.parent = model.parent;
        $scope.models = models;

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.save = function () {
            $mdDialog.hide($scope.currentModel);
        };
    }

    var addDialogCtrl = function ($scope, models, parent, prefabs) {
        $scope.models = models;
        $scope.prefabs = prefabs;
        $scope.newModel = {
            modelProperties: {
                position: [0, 0, 0],
                orientation: [0, 0, 0],
                scale: [0, 0, 0]
            },
            name: "",
            prefab: {
                name: ""
            },
            identifier: "",
            parent: parent,
            models: [],
            note:{}
        };

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.add = function () {
            $mdDialog.hide($scope.newModel);
        };
    }

    var getAllModels = function (parent) {
        var result = [];
        result.push(parent);
        parent.models.forEach(function (model) {
            //result.push(model);
            var next = getAllModels(model);
            next.forEach(function (subModel) {
                result.push(subModel);
            });
        });
        return result;
    };

    $scope.getModelIcon = function (model) {
        if(model.type === "NoteObject"){
            return "note";
        }
        else{
            return "wallpaper";
        }
    }

});