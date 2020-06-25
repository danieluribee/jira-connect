(function (angular) {
    angular.module('JiraSettingsApp')
        .controller('SettingsController', SettingsController);

    SettingsController.$inject = ['SERVER_BASE_URL', '$http', '$q', '$window', '$log', '$location'];

    function SettingsController(SERVER_BASE_URL, $http, $q, $window, $log, $location) {
        var vm = this;
        var token = $window.token;
        var fistTime = true;

        vm.slackSubdomain = '';
        vm.slackTeamName = '';
        vm.slackTeamId = null;
        vm.standups = [];
        vm.uniqueStandups = [];
        vm.projects = [];
        vm.relations = [];
        vm.settings = [];
        vm.loading = true;
        vm.saved = false;
        vm.selectedTab = 'JIRA_CONNECTIONS';
        vm.currentStandupSettings = [];
        vm.currentStandupSettings.questions = null;
        vm.currentStandupSettings.displayNoUpdates = null;
        vm.currentStandupSettings.postProd = null;
        vm.currentStandupSettings.reportASAP = null;
        vm.currentStandupSettings.sameTime = null;
        vm.currentStandupSettings.PersonalizedTime = null;
        vm.currentStandupSettings.newStatus = null;
        vm.currentStandupSettings.displayNewStatus = null;
        vm.currentStandupSettings.editStatus = null;
        vm.currentStandupSettings.displayEditStatus = null;
        vm.currentStandupSettings.deleteStatus = null;
        vm.currentStandupSettings.standUpName = null;
        vm.currentStandupSettings.time = null;
        vm.currentStandupSettings.timeZone = null;
        vm.currentStandupSettings.scrumMaster = null;
        vm.currentStandupSettings.periodicity = null;
        vm.clickJiraConnectionsBg = "white";
        vm.clickStandUpsBg = "#e9e9e9";
        vm.showNewQuestion = false;
        vm.newQuestion = "";
        vm.currentStandupSettings.tz = null;
        vm.resourcePrefix = function () {
            return window.standbotEnvironmentLocal ? '/jira' : '';
        };

        vm.saveStandupsConfiguration = saveStandupsConfiguration;
        vm.addNewConnection = addNewConnection;
        vm.removeUselessRelation = removeUselessRelation;
        vm.removeDuplicatedRelations = removeDuplicatedRelations;
        vm.usedInAnotherRelation = usedInAnotherRelation;
        vm.sentStandupConfig = sentStandupConfig;
        vm.updateCurrentSettings = updateCurrentSettings;
        vm.setSettingsWindow = setSettingsWindow;
        vm.clickJiraConnections = clickJiraConnections;
        vm.clickStandUps = clickStandUps;
        vm.showNewQuestionWindow = showNewQuestionWindow;
        vm.addnewQuestion = addnewQuestion;


        _init();

        function _init() {
            _verifyRelationWithSlack()
                .then(function (teamData) {
                    vm.slackTeamName = teamData.data.slack_data.team_name;
                    vm.slackSubdomain = teamData.data.slack_data.domain;
                    _buildRelations();
                });
        }

        function _verifyRelationWithSlack() {
            return $http.get(SERVER_BASE_URL + '/slack/relations').then(function (relationsData) {
                $log.log(relationsData, relationsData.data.length);
                if (relationsData.data && relationsData.data.length > 0) {
                    vm.slackTeamId = relationsData.data[0].messaging.slack_data.team_id;

                    return $http.get(SERVER_BASE_URL + '/slack/teams/' + vm.slackTeamId);
                } else {
                    $location.path('/');
                }
            });
        }

        function _buildRelations() {
            var standupsPromise = $http
                .get(SERVER_BASE_URL + '/slack/teams/' + vm.slackTeamId + '/standups')
                .then(function (standupsResult) {
                    vm.standups = standupsResult.data.reduce((acc, val) => acc.concat(val), []);

                    const uniquePlatformConversationIds = [];

                    vm.uniqueStandups = angular.copy(vm.standups, []).filter(function (standup) {
                        if (uniquePlatformConversationIds.indexOf(standup.platform_conversation_id) >= 0) {
                            return false;
                        }

                        uniquePlatformConversationIds.push(standup.platform_conversation_id);
                        return true;
                    });
                });

            var projectsPromise = $http
                .get(SERVER_BASE_URL + '/jira/projects')
                .then(function (projectsResult) {
                    vm.projects = projectsResult.data.map(function (project) {
                        return {
                            id: project.id,
                            name: project.name
                        }
                    });
                    $log.log(vm.projects);
                });

            $q.all([standupsPromise, projectsPromise])
                .then(function () {
                    const keys = {};

                    vm.relations = angular.copy(vm.standups, [])
                        .filter(function (standup) {
                            return standup.jira_project_id;
                        })
                        .map(function (standup) {
                            return {
                                standup: standup
                            };
                        });

                    vm.loading = false;
                });
        }


        /**
         * Adds a new connection to the relations array
         */
        function addNewConnection() {
            vm.relations.push({
                standup: {
                    platform_conversation_id: '0',
                    jira_project_id: '0'
                }
            });
        }

        /**
         * Removes the current relation if the user unselected both drop-downs
         * @param  {int} $index - indexOf the relation in vm.relations
         */
        function removeUselessRelation($index) {
            /*
             * check collection as dirty
             */
            vm.saved = false;

            var relation = vm.relations[$index];

            if (
                relation.standup.platform_conversation_id == '0'
                && relation.standup.jira_project_id == '0'
            ) {
                vm.relations.splice($index, 1);
            }
        }

        /**
         * Removes duplicated relations by deselecting the jira_project_id
         * when a duplicated relation is found
         */
        function removeDuplicatedRelations() {
            for (let i = 0; i < vm.relations.length; i++) {
                const relation = vm.relations[i];

                if (!relation.standup.platform_conversation_id || !relation.standup.jira_project_id) {
                    continue;
                }

                for (let j = i + 1; j < vm.relations.length; j++) {
                    const innerRelation = vm.relations[j];

                    if (equal(relation, innerRelation)) {
                        innerRelation.standup.jira_project_id = '0';
                    }
                }

            }
        }

        /**
         * @returns {boolean} - True if platform_conversation_id and jira_project_id are equals
         *
         * @param  {Relation} relationA
         * @param  {Relation} relationB
         */
        function equal(relationA, relationB) {
            return relationA.standup.platform_conversation_id == relationB.standup.platform_conversation_id
                && relationA.standup.jira_project_id == relationB.standup.jira_project_id;
        }

        function saveStandupsConfiguration() {
            vm.relations = vm.relations.filter(function (relation) {
                return relation.standup.platform_conversation_id != '0'
                    && relation.standup.jira_project_id != '0';
            });

            console.log({
                relations: vm.relations,
                slack_team_id: vm.slackTeamId
            });

            return $http.post(SERVER_BASE_URL + '/jira/relations', {
                    relations: vm.relations.map(function (relation) {
                        return {
                            standup: {
                                jira_project_id: relation.standup.jira_project_id,
                                name: relation.standup.name,
                                platform_conversation_id: relation.standup.platform_conversation_id,
                                team_id: relation.standup.team_id
                            }
                        }
                    }),
                    slack_team_id: vm.slackTeamId
                })
                .then(function () {
                    vm.saved = true;

                    if (vm.slackUserId && fistTime) {
                        fistTime = false;
                        $http.post(SERVER_BASE_URL + '/jira/notifyJiraInStandup', {
                            slack_team_id: vm.slackTeamId,
                            slack_user_id: vm.slackUserId
                        });
                    }
                });
        }

        function usedInAnotherRelation(relation, projectId) {
            const count = vm.relations.filter(function (r) {
                return r.standup.platform_conversation_id == relation.standup.platform_conversation_id && r.standup.jira_project_id == projectId;
            }).length;

            /**
             * avoid disable for the relation's option, that will cause a null value
             */
            if (count == 1 && relation.standup.jira_project_id == projectId) {
                return false;
            } else {
                return count >= 1;
            }
        }

        function sentStandupConfig() {
            const questions = ["What did you accomplish since your last stand up?",
            "What are you working on today?"];

            const newSettings = {
                "standup_questions": questions,
                "no_updates_button": vm.currentStandupSettings.displayNoUpdates,
                "post_in_prod": vm.currentStandupSettings.postProd,
                "post_asap_everyone": vm.currentStandupSettings.reportASAP,
                "same_time_zone": vm.currentStandupSettings.sameTime,
                "porsonalized_time_zone": vm.currentStandupSettings.PersonalizedTime,
                "new_status": vm.currentStandupSettings.newStatus,
                "display_new_status": vm.currentStandupSettings.displayNewStatus,
                "edit_status": vm.currentStandupSettings.editStatus,
                "display_edit_status": vm.currentStandupSettings.displayEditStatus,
                "delete_status": vm.currentStandupSettings.deleteStatus
            };

            return $http.post('http://localhost:8082/addSettings',newSettings);
        }

        function updateCurrentSettings(standUpItem) {
            console.log(standUpItem.periodicity);
            currentCuestions = [];
            var daysString = "";

            for(var current in standUpItem.questions){
                currentCuestions.push(standUpItem.questions[current].text);
            }

            for(var current in standUpItem.periodicity){
                switch (parseInt(standUpItem.periodicity[current])) {
                    case 1:
                        daysString += " Monday"
                      break;
                    case 2:
                        daysString += " Tuesday"
                      break;
                    case 3:
                        daysString += " Wednesday"
                      break;
                    case 4:
                        daysString += " Thursday"
                      break;
                    case 5:
                        daysString += " Friday"
                      break;
                    case 6:
                        daysString += " Saturday"
                      break;
                    case 7:
                        daysString += " Sunday"
                      break;
                  }
            }

            vm.currentStandupSettings.questions = currentCuestions;
            vm.currentStandupSettings.displayNoUpdates = standUpItem.allowNoUpdates;
            vm.currentStandupSettings.postProd = !standUpItem.notifications.summary.sendOnComplete;
            vm.currentStandupSettings.reportASAP = standUpItem.notifications.summary.sendOnComplete;
            vm.currentStandupSettings.sameTime = !standUpItem.notifications.summary.useFixedTime;
            vm.currentStandupSettings.PersonalizedTime = standUpItem.notifications.summary.useFixedTime;
            vm.currentStandupSettings.newStatus = standUpItem.notifications.channel.sendOnNewStatus;
            vm.currentStandupSettings.displayNewStatus = standUpItem.notifications.channel.sendNewStatusFull;
            vm.currentStandupSettings.editStatus = standUpItem.notifications.channel.sendOnEditStatus;
            vm.currentStandupSettings.displayEditStatus = standUpItem.notifications.channel.sendEditStatusFull;
            vm.currentStandupSettings.deleteStatus = standUpItem.notifications.channel.sendOnDeleteStatus;
            vm.currentStandupSettings.standUpName = standUpItem.name;
            vm.currentStandupSettings.time = standUpItem.notifications.summary.time;
            vm.currentStandupSettings.timeZone = standUpItem.notifications.summary.tz;
            vm.currentStandupSettings.scrumMaster = standUpItem.scrumMasterName;
            vm.currentStandupSettings.periodicity = daysString;
            vm.currentStandupSettings.tz = standUpItem.notifications.summary.time;
        }

        function setSettingsWindow(settingType) {
            vm.selectedTab = settingType;
        }

        function clickJiraConnections(){
            vm.clickJiraConnectionsBg = "white";
            vm.clickStandUpsBg = "#e9e9e9";
        }

        function clickStandUps(){
            vm.clickJiraConnectionsBg = "#e9e9e9";
            vm.clickStandUpsBg = "white";
        }

        function showNewQuestionWindow(){
            vm.showNewQuestion = true;
        }

        function addnewQuestion(newQuestionAdd){
            vm.currentStandupSettings.questions.push(newQuestionAdd);
            vm.showNewQuestion = false;
            vm.newQuestion = "";
        }

    }
})(window.angular);