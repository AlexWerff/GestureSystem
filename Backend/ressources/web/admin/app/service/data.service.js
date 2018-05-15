angular.module("app").service('dataservice', function ($rootScope, $http, $window) {
    var baseUrl = "/"; //"http://localhost:8080/";

    this.getInfo = function (onComplete) {
        var url = baseUrl + "api/getInfo";
        $http.get(url).success(function (data) {
            onComplete(data.InfoResponse);
        });
    }




    this.getGestures = function (onComplete) {
        $http.get(baseUrl + "api/getGestures").success(function (data) {
            var result = [];
            data.GesturesResponse.gestures.forEach(function (gesture) {
                var type = Object.keys(gesture)[0];
                var gest = gesture[type];
                gest["type"] = type;
                result.push(gest);
            });
            onComplete(result);
        });
    }

    this.postGesture = function (gesture, onComplete) {
        var post = {};
        post[gesture.type] = {
            active: gesture.active,
            name: gesture.name
        };
        $http.post(baseUrl + "api/postGesture", post).success(function (data) {
            onComplete();
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    }

    this.getProviderConfigs = function (onComplete) {
        $http.get(baseUrl + "api/getProviderConfigs").success(function (data) {
            var result = [];
            data.ProviderConfigsResponse.configs.forEach(function (config) {
                result.push(config);
            });
            onComplete(result);
        });
    }

    this.deleteProviderConfig = function (config, onComplete) {
        var post = {};
        $http.post(baseUrl + "api/deleteProviderConfig?identifier=" + config.identifier, post).
        success(function (data) {
            onComplete();
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    }

    this.updateProviderConfig = function (config, onComplete) {
        var post = {
            identifier: config.identifier,
            modelIdentifier: config.modelIdentifier,
            sceneIdentifier: config.sceneIdentifier,
            port: config.port,
            remoteAddress: config.remoteAddress,
            providerTypes: config.providerTypes

        };
        $http.post(baseUrl + "api/postProviderConfig", post).success(function (data) {
            onComplete();
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    }

    this.getConsumerConfigs = function (onComplete) {
        $http.get(baseUrl + "api/getConsumerConfigs").success(function (data) {
            var result = [];
            data.ConsumerConfigsResponse.configs.forEach(function (config) {
                var type = Object.keys(config)[0];
                var conf = config[type];
                conf["type"] = type;
                if (type === "PhillipsHueConsumerConfig") {
                    conf["logoUrl"] = "https://upload.wikimedia.org/wikipedia/en/a/a1/Philips_hue_logo.png"
                }
                result.push(conf);
            });
            onComplete(result);
        });
    }

    this.deleteConsumerConfig = function (config, onComplete) {
        var post = {};
        $http.post(baseUrl + "api/deleteConsumerConfig?identifier=" + config.identifier, post).
        success(function (data) {
            onComplete();
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    }

    this.updateConsumerConfig = function (config, onComplete) {
        var post = {};
        post[config.type] = {
            identifier: config.identifier,
            port: config.port,
            address: config.address,
            name: config.name
        };
        if (config.username !== undefined) {
            post[config.type]['port'] = 80;
            post[config.type]['username'] = config.username;
        }
        $http.post(baseUrl + "api/postConsumerConfig", post).success(function (data) {
            onComplete();
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    }

    this.deleteModel = function (sceneID, model, onComplete) {
        var post = {
            sceneIdentifier: sceneID,
            identifier: model.identifier,
            parentIdentifier: model.parent.identifier
        }
        $http.post(baseUrl + "api/deleteObject", post).success(function (data) {
            onComplete()
                //$window.alert(JSON.stringify(data));
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    };

    this.updateModel = function (sceneID, model, onComplete) {
        var post = {
            sceneIdentifier: sceneID,
            identifier: model.identifier,
            parentIdentifier: model.parent.identifier === undefined ? sceneID : model.parent.identifier,
        }
        if(model.note.content !== undefined && model.note.name !== undefined){
            post.model = {
                NoteObject: {
                    name: model.name,
                    modelProperties: model.modelProperties,
                    note: {InfoNote:model.note},
                    models: {}
                }
            }
        }
        else{
            post.model = {
                PrefabObject: {
                    name: model.name,
                    modelProperties: model.modelProperties,
                    prefab: model.prefab,
                    models: {}
                }
            }
        }

        $http.post(baseUrl + "api/postObject", post).success(function (data) {
            onComplete()
                //$window.alert(JSON.stringify(data));
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    }

    this.postModel = function (dataModel, onComplete) {
        var post = dataModel
        $http.post(baseUrl + "api/postModel", post).success(function (data) {
            onComplete()
                //$window.alert(JSON.stringify(data));
        }).error(function (error) {
            $window.alert(JSON.stringify(error));
        });
    }


    var extractModels = function (parent) {
        var models = [];
        Object.keys(parent.models).forEach(function (k) {
            var obj = parent.models[k];
            var type = Object.keys(obj)[0];
            obj = obj[type];
            obj["identifier"] = k;
            obj["parent"] = parent;
            obj["type"] = type;
            if(type === "NoteObject"){
                obj['note']= obj['note'][Object.keys(obj.note)[0]];
            }
            obj.models = extractModels(obj);
            models.push(obj);
        });
        return models;
    }

    this.getModel = function (onComplete) {
        $http.get(baseUrl + "api/getModel").success(function (data) {
            var model = data.ModelResponse.model;
            var newModel = {
                scenes: [],
                prefabs: model.prefabs
            };
            Object.keys(model.scenes).forEach(function (key) {
                var scene = model.scenes[key];
                var newScene = {};
                newScene["metaData"] = scene["metaData"];
                newScene["identifier"] = key;
                var models = extractModels(scene);
                models.forEach(function (m) {
                    m.parent = newScene;
                });
                newScene.models = models;
                newModel.scenes.push(newScene);
            });

            onComplete(newModel);
        });
    }
});



