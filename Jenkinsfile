pipeline {
    agent {
        label 'amzl-honeur'
    }

    stages {
        stage("build") {
            steps {
                dir('WebAPI') {
                    sh './build.sh'
                }
            }
        }

        stage("upload image") {
            steps {
                dir('WebAPI') {
                    sh './publish.sh'
                }
            }
        }
    }
}