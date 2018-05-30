module.exports = function (grunt) {

    var CWD = './src/main/resources/configure-app';
    var config = grunt.file.readJSON('config.json');

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        clean: [CWD + '/build'],

        concat: {
            options: {
                separator: ';',
            },
            dist: {
                src: [
                    CWD + '/app.js',
                    CWD + '/controllers/SettingsController.js',
                    CWD + '/controllers/SlackTeamController.js',
                    CWD + '/controllers/SlackVerifyController.js',
                    CWD + '/temp/*.js',
                ],
                dest: CWD + '/build/main.js',
            },
            vendor: {
                src: [
                    './node_modules/angular/angular.js',
                    './node_modules/angular-route/angular-route.js',
                ],
                dest: CWD + '/build/vendor.js',
            },
        },
        clean: [CWD + '/build'],
        uglify: {
            dist: {
                files: {
                    './src/main/resources/configure-app/build/main.min.js': [CWD + '/build/main.js']
                }
            }
        },
        ngtemplates: {
            app: {
                options: {
                    prefix: '/settings/templates/',
                    url:    function(url) {
                        return url.replace(CWD + '/templates/', '');
                    },
                    module: 'JiraSettingsApp'
                },
                src: CWD + '/templates/*.html',
                dest: CWD + '/temp/templates.js'
            }
        },
        copy: {
            config: {
                files: [
                    // generate a new atlassian-plugin.xml to replace timestamp
                    {
                        src: './src/main/java/com/softwaredevtools/standbot/config/StandbotConfigTemplate.java',
                        dest: './src/main/java/com/softwaredevtools/standbot/config/StandbotConfig.java'
                    }
                ],
                options: {
                    process: function(content, srcpath) {
                        if (srcpath == './src/main/java/com/softwaredevtools/standbot/config/StandbotConfigTemplate.java') {
                            return content.replace('StandbotConfigTemplate', 'StandbotConfig');
                        }
                        return content;
                    }
                }
            },
            js: {
                files: [
                    {
                        src: './src/main/resources/configure-app/controllers/SlackTeamControllerTemplate.js',
                        dest: './src/main/resources/configure-app/controllers/SlackTeamController.js'
                    }
                ]
            }
        },
        replace: {
            local: {
                options: {
                    patterns: [
                        {
                            match: 'SLACK_CLIENT_ID',
                            replacement: config.local.slackClientId
                        }
                    ]
                },
                files: [
                    {
                        expand: true,
                        flatten: true,
                        src: ['./src/main/resources//configure-app/controllers/SlackTeamController.js'],
                        dest: './src/main/resources//configure-app/controllers/'
                    }
                ]
            },
            stage: {
                options: {
                    patterns: [
                        {
                            match: 'SLACK_CLIENT_ID',
                            replacement: config.stage.slackClientId
                        }
                    ]
                },
                files: [
                    {
                        expand: true,
                        flatten: true,
                        src: ['./src/main/resources//configure-app/controllers/SlackTeamController.js'],
                        dest: './src/main/resources//configure-app/controllers/'
                    }
                ]
            },
            production: {
                options: {
                    patterns: [
                        {
                            match: 'SLACK_CLIENT_ID',
                            replacement: config.production.slackClientId
                        }
                    ]
                },
                files: [
                    {
                        expand: true,
                        flatten: true,
                        src: ['./src/main/resources//configure-app/controllers/SlackTeamController.js'],
                        dest: './src/main/resources//configure-app/controllers/'
                    }
                ]
            },
            config_local: {
                options: {
                    patterns: [
                        {
                            match: 'JWT_SECRET',
                            replacement: config.local.jwt
                        },
                        {
                            match: 'STANDBOT_API_BASE_URL',
                            replacement: config.local.api_url
                        },
                        {
                            match: 'ENVIRONMENT',
                            replacement: "LOCAL"
                        }
                    ]
                },
                files: [
                    {
                        expand: true,
                        flatten: true,
                        src: ['./src/main/java/com/softwaredevtools/standbot/config/StandbotConfig.java'],
                        dest: './src/main/java/com/softwaredevtools/standbot/config/'
                    }
                ]
            },
            config_stage: {
                options: {
                    patterns: [
                        {
                            match: 'JWT_SECRET',
                            replacement: config.stage.jwt
                        },
                        {
                            match: 'STANDBOT_API_BASE_URL',
                            replacement: config.stage.api_url
                        },
                        {
                            match: 'ENVIRONMENT',
                            replacement: "STAGE"
                        }
                    ]
                },
                files: [
                    {
                        expand: true,
                        flatten: true,
                        src: ['./src/main/java/com/softwaredevtools/standbot/config/StandbotConfig.java'],
                        dest: './src/main/java/com/softwaredevtools/standbot/config/'
                    }
                ]
            },
            config_production: {
                options: {
                    patterns: [
                        {
                            match: 'JWT_SECRET',
                            replacement: config.production.jwt
                        },
                        {
                            match: 'STANDBOT_API_BASE_URL',
                            replacement: config.production.api_url
                        },
                        {
                            match: 'ENVIRONMENT',
                            replacement: "PRODUCTION"
                        }
                    ]
                },
                files: [
                    {
                        expand: true,
                        flatten: true,
                        src: ['./src/main/java/com/softwaredevtools/standbot/config/StandbotConfig.java'],
                        dest: './src/main/java/com/softwaredevtools/standbot/config/'
                    }
                ]
            },
        }
    });

    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-replace');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-angular-templates');

    // Tell Grunt what to do when we type "grunt" into the terminal
    grunt.registerTask('default', [
        'clean', 'copy:js', 'replace:local', 'ngtemplates', 'concat:vendor', 'concat:dist', 'copy:config', 'replace:config_local',
    ]);

    grunt.registerTask('stage', [
        'clean', 'copy:js', 'replace:stage', 'ngtemplates', 'concat:vendor', 'concat:dist', 'copy:config', 'replace:config_stage'
    ]);

    grunt.registerTask('production', [
        'clean', 'copy:js', 'replace:production', 'ngtemplates', 'concat:vendor', 'concat:dist', 'copy:config', 'replace:config_production'
    ]);
};
