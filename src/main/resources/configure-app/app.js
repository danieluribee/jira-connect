//(function () {
    angular.module('JiraSettingsApp', ['ngRoute'])
    .constant('SERVER_BASE_URL', 'http://localhost:2990/jira/rest/standbot/latest')
    .config(function($routeProvider, $locationProvider) {
        $routeProvider
        .when('/', {
            templateUrl: '/settings/templates/slack-team.html',
            controller: 'SlackTeamController',
            controllerAs: 'vm'
        }).when('/slack-verify', {
            templateUrl: '/settings/templates/slack-verify.html',
            controller: 'SlackVerifyController',
            controllerAs: 'vm'
        }).when('/settings', {
            templateUrl: '/settings/templates/settings.html',
            controller: 'SettingsController',
            controllerAs: 'vm'
        });
    });
//})();