angular.module("app").controller("NotesController", function ($scope, $window,$mdDialog, dataservice) {
    $scope.noteVisibilityChanged = function (visible) {

    };



    var update = function () {
        dataservice.getModel(function (model) {
            $scope.model = model;
            $scope.noteModels = [];
        });
    }
    
    $scope.getModel = function (note) {
        return "TV-1"
    };




    $scope.deleteNoteClick = function (note, ev) {
        var confirm = $mdDialog.confirm()
            .title('Confirm')
            .textContent('Are you sure you want to delete this note?')
            .targetEvent(ev)
            .ok('Delete')
            .cancel('Cancel');

        $mdDialog.show(confirm).then(function () {
            dataservice.deleteModel(note.identifier,function () {

            });
        }, function () {
            //Cancel
        });
    }


    $scope.editNoteClick = function (note,ev) {
        $mdDialog.show({
            clickOutsideToClose: true,
            controllerAs: 'ctrl',
            templateUrl: 'app/content/notes/editDialog.html',
            controller: editDialogCtrl,
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false
        }).then(function (newNote) {
            dataservice.updateNote(newNote,function () {

            });
        });
    }

    var editDialogCtrl = function ($scope) {

        $scope.newNote = {};

        $scope.hide = function () {
            $mdDialog.hide();
        };

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        $scope.save = function () {
            $mdDialog.hide($scope.newNote);
        };
    }

    update();
});