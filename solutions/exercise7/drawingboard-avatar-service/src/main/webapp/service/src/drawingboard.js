var avatar = require("org/glassfish/avatar");
var appstate = avatar.application.state;
var bus = avatar.application.bus;

var pushContexts = [];
appstate.put("drawings", []);
appstate.put("lastId", 1);

avatar.registerPushService({url: "api/events"}, function() {
    this.onOpen = function(context) {
        pushContexts.push(context);
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
        print("DELETE: " + JSON.stringify(drawing) + " ThreadIndex: " + avatar.application.threadIndex);
        drawings.splice(drawings.indexOf(drawing), 1);
        appstate.put("drawings", drawings);
        bus.publish('updated', drawing);
        return response.send(null);
    };
});

avatar.registerRestService({url: "api/drawings/"},
function() {
    this.onGet = function(request, response) {
        print("serving get drawings at index: " + avatar.application.threadIndex);
        return response.send(appstate.get("drawings"));
    };
    this.onPost = function(request, response) {
        var drawings = appstate.get("drawings");
        var drawing = {id: appstate.get("lastId"), name: request.data.name, shapes: []};
        print("POST: " + JSON.stringify(drawing) + " ThreadIndex: " + avatar.application.threadIndex);
        drawings.push(drawing);
        appstate.put("drawings", drawings);
        appstate.put("lastId", drawing.id + 1);
        bus.publish('updated', drawing);
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

bus.on('updated', function(body, message) {
    print("drawings update received at ThreadIndex: " + avatar.application.threadIndex
            + " for drawing " + JSON.stringify(body));
    pushAll();
});

function pushAll() {
    for (var i in pushContexts) {
        pushContexts[i].sendMessage({msg: "collection changed"});
    };
};


function getDrawing(id) {
    var drawings = appstate.get("drawings");
    return drawings.filter(function(d) {return d.id == id})[0];
};

