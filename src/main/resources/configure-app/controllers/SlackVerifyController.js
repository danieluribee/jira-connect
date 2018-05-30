(function (angular) {
    angular.module('JiraSettingsApp')
    .controller('SlackVerifyController', SlackVerifyController);

    function SlackVerifyController() {
        var vm = this;

        vm.resourcePrefix = function() {
            return window.standbotEnvironmentLocal ? '/jira' : '';
        };
    }
})(window.angular);
