angular.module("app").controller("ConsumerController", function ($rootScope, $scope, $mdDialog, dataservice) {
    $scope.consumer = [];

    var load = function () {
        $scope.consumer = [];
        dataservice.getConsumerConfigs(function (configs) {
            $scope.consumer = configs;
        });
    }
    load();


    $scope.deleteConsumerClick = function (prov, ev) {
        var confirm = $mdDialog.confirm()
            .title('Confirm')
            .textContent('Are you sure you want to delete this consumer?')
            .targetEvent(ev)
            .ok('Delete')
            .cancel('Cancel');

        $mdDialog.show(confirm).then(function () {
            dataservice.deleteConsumerConfig(prov, function () {
                load();
            });
        }, function () {
            //Cancel
        });
    }

    $scope.addConsumerClick = function (ev) {
        $mdDialog.show({
            clickOutsideToClose: true,
            controllerAs: 'ctrl',
            templateUrl: 'app/content/consumer/addDialog.html',
            controller: addDialogCtrl,
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false
        }).then(function (newConsumer) {
            dataservice.updateConsumerConfig(newConsumer, function () {
                load();
            });
        });
    }

    var addDialogCtrl = function ($scope) {
        $scope.currentConsumer = {
            type: "",
            identifier: ""
        }
        $scope.selectedIndex = 0;
        $scope.types = [
            {
                "name": "Amazon Alexa",
                type: "AlexaConsumerConfig"
            },
            {
                "name": "Phillips Hue",
                type: "PhillipsHueConsumerConfig"
            }
        ];

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.next = function () {
            $scope.selectedIndex++;
        }
        $scope.saveDisabled = function () { //TODO Add type check etc
            return $scope.currentConsumer.address === undefined || 
                ($scope.currentConsumer.port === undefined && $scope.currentConsumer.username === undefined);
        }

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.save = function () {
            $mdDialog.hide($scope.currentConsumer);
        };

    }

});