//(function () {
    angular.module('JiraSettingsApp')
        .controller('SettingsController', SettingsController);

    SettingsController.$inject = ['$http', '$q', '$window','$log', '$location'];

    function SettingsController($http, $q, $window, $log, $location) {
        var vm = this;
        var token = $window.token;

        vm.slackSubdomain = '';
        vm.slackTeamName = '';
        vm.slackTeamId = null;
        vm.standups = [];
        vm.projects = [];
        vm.selectedProjects = [];
        vm.loading = true;
        vm.saved = false;

        vm.verifySlackTeam = verifySlackTeam;
        vm.saveStandupsConfiguration = saveStandupsConfiguration;
        vm._fetchStandupsAndTeams = _fetchStandupsAndTeams;
        vm.checkSelectedProjects = checkSelectedProjects;
        vm.isSelected = isSelected;

        _init();

        function _init() {
            $http.get('/api/jira-instances/current/relations?jwt=' + token).then(function (relationsData) {
                $log.log(relationsData, relationsData.data.length);
                if (relationsData.data && relationsData.data.length > 0) {
                    vm.slackTeamId = relationsData.data[0].slack_team_id;

                    $http.get('/api/slack/teams/' + vm.slackTeamId + '?jwt=' + token).then(function (teamData) {
                        vm.slackTeamName = teamData.data.team_name;
                        vm.slackSubdomain = teamData.data.domain;
                    });

                    _fetchStandupsAndTeams();
                } else {
                    $location.path('/');
                }
            });
        }

        function _fetchStandupsAndTeams() {
            var standupsPromise = $http.get('/api/slack/teams/' + vm.slackTeamId + '/standups?jwt=' + token).then(function (standupsResult) {
                vm.standups = standupsResult.data;
            });
            var projectsPromise = $http.get('/api/jira/projects?jwt=' + token).then(function (projectsResult) {
                vm.projects = projectsResult.data;
                $log.log(vm.projects);
            });

            $q.all([
                standupsPromise,
                projectsPromise
            ])
                .then(function () {
                    for (var idx in vm.standups) {
                        if (!vm.standups[idx].jira_project_id) {
                            vm.standups[idx].jira_project_id = '0';
                        }
                    }

                    checkSelectedProjects();
                    vm.loading = false;
                });
        }

        function checkSelectedProjects() {
            vm.selectedProjects = vm.standups.map(function(standup) {
                return standup.jira_project_id ? standup.jira_project_id : undefined;
            });
        }

        function isSelected(projectId) {
            return vm.selectedProjects.includes(projectId);
        }

        function verifySlackTeam() {
            $window.alert(vm.slackSubdomain);
        }

        function saveStandupsConfiguration() {
            var promises = vm.standups.map(function (standup) {
                /** 
                 * If jira_project_id == 0, we should send a DELETE but is not yet implemented
                */
                return $http.post('/api/jira/projects/' + standup.jira_project_id + '/standups?jwt=' + token, {
                    slack_channel_id: standup.channel_id,
                    slack_team_id: vm.slackTeamId
                });
            });

            $q.all(promises).then(function () {
                vm.saved = true;
            });
        }
    }
//})();
