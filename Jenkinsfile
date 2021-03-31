pipeline {
    agent any
    environment {
        WAR_ARCHIVE = 'app/build/libs/app.war'
        WEBAPPS_PATH = '/usr/local/tomcat/webapps'
    }
    parameters {
        choice(
            name: 'testSkip',
            choices: ['true', 'false'],
            description: '是否跳过测试'
        )
    }
    stages {
        stage('Build') {
            steps {
                echo 'Build.'
                // sh 'chmod +x gradlew'
                sh './gradlew clean assemble'
            }
        }
        stage("Test") {
            when {
                equals expected: 'false', actual: params.testSkip
            }
            steps {
                echo 'Test.'
                sh './gradlew check'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploy.'
                sh "cp -v $WAR_ARCHIVE $WEBAPPS_PATH"
            }
        }
    }
}
