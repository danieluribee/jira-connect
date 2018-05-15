module.exports = function (grunt) {

    var CWD = './src/main/resources/configure-app';

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
                    CWD + '/controllers/*.js',
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
        }
    });

    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-angular-templates');

    // Tell Grunt what to do when we type "grunt" into the terminal
    grunt.registerTask('default', [
        'clean', 'ngtemplates', 'concat:vendor', 'concat:dist', //'uglify'
    ]);
};