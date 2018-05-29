(function (angular) {
    angular.module('JiraSettingsApp')
        .controller('SlackTeamController', SlackTeamController);

    SlackTeamController.$inject = ['SERVER_BASE_URL', '$http', '$location', '$q', '$window', '$log', '$document'];

    function SlackTeamController(SERVER_BASE_URL, $http, $location, $q, $window, $log, $document) {
        var vm = this;
        var token = $window.token;

        vm.slackSubdomain = '';
        vm.showInstallInstructions = false;
        vm.loading = true;
        vm.slackTeamId = null;
        vm.standups = [];
        vm.projects = [];

        vm.verifySlackTeam = verifySlackTeam;
        vm.saveStandupsConfiguration = saveStandupsConfiguration;

        _init();

        function _init() {
            $http.get(SERVER_BASE_URL + '/slack/relations').then(function (relationsData) {
                $log.log(relationsData, relationsData.data.length);
                if (relationsData.data && relationsData.data.length) {
                    vm.slackTeamId = relationsData.data[0].slack_team_id;
                    $location.path('/settings');
                } else {
                    $log.log($location.search(), $location.url(), $window.location.href, getUrlParameter('slackSubdomain'));
                    vm.slackSubdomain = getUrlParameter('slackSubdomain');

                    if (vm.slackSubdomain) {
                        verifySlackTeam().then(function () {
                            vm.loading = false;
                        });
                    } else {
                        vm.loading = false;
                    }

                }

            }).catch(function(response) {
            });
        }

        function verifySlackTeam() {
            return $http.get(SERVER_BASE_URL + '/slack/verify?subdomain=' + vm.slackSubdomain)
                .then(function (res) {
                    $log.log(res.data);
                    if (res.data.verified) {
                        $location.path('/settings');
                    } else {
                        $location.path('/slack-verify');
                    }
                })
                .catch(function (res) {
                    if (res.status === 404) {
                        vm.slackUrl = 'https://slack.com/oauth/authorize?client_id=@@SLACK_CLIENT_ID&scope=bot,users:read,users.profile:read,chat:write:bot,groups:read,channels:read,team:read,chat:write:bot&state=' + $window.btoa(document.referrer + '||' + vm.slackSubdomain);
                        vm.showInstallInstructions = true;
                    }
                });
        }

        function saveStandupsConfiguration() {
            var promises = vm.standups.map(function (standup) {
                if (standup.jira_project_id != 0) {
                    return $http.post('/api/jira/projects/' + standup.jira_project_id + '/standups?jwt=' + token, {slack_channel_id: standup.channel_id});

                }
            });

            $q.all(promises).then(function () {
                $window.alert('Saved');
            });
        }

        function getUrlParameter(param, dummyPath) {
            var sPageURL = dummyPath || $window.location.search.substring(1),
                sURLVariables = sPageURL.split(/[&||?]/),
                res;

            for (var i = 0; i < sURLVariables.length; i += 1) {
                var paramName = sURLVariables[i],
                    sParameterName = (paramName || '').split('=');

                if (sParameterName[0] === param) {
                    res = sParameterName[1];
                }
            }

            return res;
        }
    }
}(window.angular));