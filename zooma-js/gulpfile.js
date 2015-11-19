(function(){
    "use strict";

    Array.prototype.clone = function() {
        return this.slice(0);
    };

    var gulp        = require('gulp');
    var rename      = require('gulp-rename');
    var uglify      = require('gulp-uglify');
    var minifyCss   = require('gulp-minify-css');
    var concat      = require('gulp-concat');
    var del         = require('del');
    var runSequence = require('run-sequence');
    var watch       = require('gulp-watch');
    var browserSync = require('browser-sync').create();
    var bf          = require('bower-files')();
    var filter      = require('gulp-filter');
    var order       = require('gulp-order');
    var addsrc      = require('gulp-add-src');

    var TARGET      = 'target/classes/META-INF/resources/webjars/zooma-js/2.0.0/';
    var PUBLIC      = 'public/';
    var PUBLIC_JS   = PUBLIC + "/js/";
    var PUBLIC_CSS  = PUBLIC + "/css/";



    gulp.task('clean', function() {
        del(TARGET);
    });

    gulp.task('copy-js-libraries', function() {
        return gulp.src(bf.ext("js").files)
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('package-js-libraries', function() {
        return gulp.src(bf.ext("js").files)
                .pipe(concat('vendor.js'))
                .pipe(uglify())
                .pipe(rename({extname: '.min.js'}))
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('copy-css-libraries', function() {
        return gulp.src(bf.ext("css").files)
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('package-css-libraries', function() {
        return gulp.src(bf.ext("css").files)
                .pipe(concat('vendor.css'))
                .pipe(minifyCss())
                .pipe(rename({extname: '.min.css'}))
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('copy-project-js', function() {
        return gulp.src('src/main/javascript/*.js')
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('old-package-project-js', function() {
        return gulp.src('src/main/javascript/*.js')
                .pipe(uglify())
                .pipe(rename({extname: '.min.js'}))
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('package-project-js',function(){

        var jsOrder    = [
            "**/jquery.js",
            "**/mustache.js"
        ];
        
        return gulp.src(bf.ext('js').files)
                    .pipe(order(jsOrder))
                    .pipe(concat('vendor.js'))
                    .pipe(gulp.dest(TARGET))
                    .pipe(uglify())
                    .pipe(rename({extname: '.min.js'}))
                    .pipe(gulp.dest(TARGET));
                    
    });

    gulp.task('copy-project-css', function() {
        return gulp.src('src/main/stylesheets/*.css')
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('old-package-project-css', function() {
        return gulp.src('src/main/stylesheets/*.css')
                .pipe(minifyCss())
                .pipe(rename({extname: '.min.css'}))
                .pipe(gulp.dest(TARGET));
    });

    gulp.task('package-project-css',function(){

        var cssOrder  = [
            "**/tooltipster.css",
            "**/tooltipster-light.css",
            "**/bootstrap-tagsinput.css",
            "**/spinners.css",
            "**/throbber.css",
        ];

        var cssFilter = cssOrder.clone();

        return gulp.src(bf.ext("css").files)
                    // .pipe(addsrc('src/main/stylesheets/*.css'))
                    .pipe(filter(cssFilter))
                    .pipe(order(cssOrder))
                    .pipe(concat('vendor.css'))
                    .pipe(gulp.dest(TARGET))
                    .pipe(minifyCss())
                    .pipe(rename({extname: '.min.css'}))
                    .pipe(gulp.dest(TARGET));
    });

    gulp.task('install', function(callback) {
        return runSequence(
                'clean',
                'copy-js-libraries',
                'package-js-libraries',
                'copy-css-libraries',
                'package-css-libraries',
                'copy-project-js',
                'package-project-js',
                'copy-project-css',
                'package-project-css',
                function(error) {
                    if (error) {
                        console.log(error.message);
                    }
                    else {
                        console.log('INSTALL COMPLETE');
                    }
                    callback(error);
                });
    });

    gulp.task('serve', function() {
        browserSync.init({
            server: {
                baseDir: "public"
            },
            port: 9999,
            logLevel: "info",
            open: false,
            notify: false,
            files: [
                "public/css/**/*.css",
                "public/js/**/*.js",
                "public/index.html"
            ]
        });
    });

    gulp.task('copy-public-js', function() {
        return gulp.src('src/main/javascript/*.js')
                .pipe(watch("src/main/javascript/*.js"))
                .pipe(gulp.dest(PUBLIC_JS))
                .pipe(uglify())
                .pipe(rename({extname: '.min.js'}))
                .pipe(gulp.dest(PUBLIC_JS));
    });

    gulp.task('copy-public-css',function() {
        return gulp.src('src/main/stylesheets/*.css')
                   .pipe(watch("src/main/stylesheets/*.css"))
                   .pipe(gulp.dest(PUBLIC_CSS))
                   .pipe(minifyCss())
                   .pipe(rename({extname: '.min.css'}))
                   .pipe(gulp.dest(PUBLIC_CSS));
    });

    gulp.task('copy-public-vendor',function() {
        var filter = filter("vendor.min*");
        return gulp.src(TARGET)
                   .pipe(filter)
                   .pipe(gulp.dest('public/vendor'));
    });


    gulp.task('default',['serve','copy-public-js','copy-public-css']);
})();
