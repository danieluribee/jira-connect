(function (angular) {
    angular.module('JiraSettingsApp', ['ngRoute'])
        .constant('SERVER_BASE_URL', 'http://localhost:2990/jira/rest/standbot/latest')
        .constant('HOST_BASE_URL', window.location.href.split('/plugins')[0])
        .factory('hostBaseUrlInterceptor', function (HOST_BASE_URL) {
            return {
                request: function (config) {
                    if (config.url.indexOf('.html') >= 0) {
                        return config;
                    }

                    config.url += config.url.indexOf('?') >= 0 ? '&' : '?';
                    config.url += 'hostBaseUrl=' + encodeURIComponent(HOST_BASE_URL);

                    return config;
                }
            }
        })
        .run(function($http) {
            $http.defaults.headers.common['Accept-Language'] = 'en-US,en;q=0.5';
            $http.defaults.headers.common['Accept'] = 'application/json';
        })
        .config(function ($routeProvider, $httpProvider) {
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
                })
                .otherwise({
                    redirect: '/'
                });

            $httpProvider.interceptors.push('hostBaseUrlInterceptor');
        });
}(window.angular));