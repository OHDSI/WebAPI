pipeline {
    agent any

    stages {
        stage("scm") {
            steps {
                dir('HONEUR-Security') {
                    checkout([$class: 'GitSCM', branches: [[name: 'release/1.8']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '6121a432-5731-44ea-8734-dcdb62d0f26d', url: 'https://github.com/solventrix/HONEUR-Security.git']]])
                }
                dir('HONEUR-Common') {
                    checkout([$class: 'GitSCM', branches: [[name: 'release/1.8']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '6121a432-5731-44ea-8734-dcdb62d0f26d', url: 'https://github.com/solventrix/HONEUR-Common.git']]])
                }
            }
        }

        stage("build dependencies") {
            steps {
                dir('HONEUR-Security') {
                    sh './build.sh'
                }
                dir('HONEUR-Common') {
                    sh './build.sh'
                }
            }
        }

        stage("build") {
            steps {
                dir('WebAPI') {
                    sh './build_image_central.sh'
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