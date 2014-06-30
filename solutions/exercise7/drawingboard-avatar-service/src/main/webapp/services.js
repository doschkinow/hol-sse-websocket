'use strict';

/* Services */

var myModule = angular.module('myApp.services', ['ngResource']);

myModule.factory('DrawingService', function ($resource) {
    return $resource('/drawingboard-avatar-service/api/drawings/:drawingId', {drawingId:'@drawingId'}, {});
});
