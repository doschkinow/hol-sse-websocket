var avatar = require("org/glassfish/avatar");
var common = require("org/glassfish/avatar/common");
var appstate = common.application.state;


appstate.put("drawings", []);
appstate.put("lastId", 1);
appstate.put("pushContexts", []);
print("app initialized at index: " + common.application.threadIndex);

avatar.registerPushService({url: "api/events"}, function() {
    this.onOpen = function(context) {
        appstate.get("pushContexts").push(context);
    };
    this.onClose = function(context) {
        var pushContexts = appstate.get("pushContexts");
        var pos = pushContexts.indexOf(context);
        pushContexts.splice(pos, 1);
    };
});


avatar.registerRestService({url: "api/drawings/{id}"},
function() {
    this.onGet = function(request, response) {
        return response.send({});
    };
    this.onDelete = function(request, response) {
        var drawings = appstate.get("drawings");
        var drawing = getDrawing(this.id);
        print("DELETE: " + JSON.stringify(drawing) + " ThreadIndex: " + common.application.threadIndex);
        drawings.splice(drawings.indexOf(drawing), 1);
        appstate.emit('updated');
        return response.send(null);
    };
});

avatar.registerRestService({url: "api/drawings/"},
function() {
    this.onGet = function(request, response) {
        print("serving get drawings at index: " + common.application.threadIndex);
        return response.send(appstate.get("drawings"));
    };
    this.onPost = function(request, response) {
        var drawings = appstate.get("drawings");
        var drawing = {id: appstate.get("lastId"), name: request.data.name, shapes: []};
        print("POST: " + JSON.stringify(drawing) + " ThreadIndex: " + common.application.threadIndex);
        drawings.push(drawing);
        appstate.put("lastId", drawing.id + 1);
        appstate.emit('updated');
        return response.send(drawing.id);
    };
});

avatar.registerSocketService({url: "websockets/{id}"}, function() {
    this.onOpen = function(peer) {
        var drawings = appstate.get("drawings");
        var drawing = getDrawing(this.id);
        var shapes = drawing.shapes;
        for (var i in shapes)
            peer.getContext().sendAll(shapes[i]);
    };
    this.onMessage = function(peer, message) {
        var drawing = getDrawing(this.id);
        drawing.shapes.push(message);
        peer.getContext().sendAll(message);
    };
});

appstate.on('updated', function() {
    pushAll();
});

function pushAll() {
    for (var i in appstate.get("pushContexts")) {
        appstate.get("pushContexts")[i].sendMessage({
                msg: "collection changed, conn: " + i});
        };
};


function getDrawing(id) {
    var drawings = appstate.get("drawings");
    return drawings.filter(function(d) {return d.id == id})[0];
};

