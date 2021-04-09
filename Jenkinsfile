pipeline {
  agent {
    kubernetes {
      yaml """\
        apiVersion: v1
        kind: Pod
        spec:
          serviceAccount: jenkins
          volumes:
          - name: maven-data
            emptyDir: {}
          - name: docker-auth
            emptyDir: {}
          containers:
          - name: aws-cli
            image: amazon/aws-cli
            imagePullPolicy: IfNotPresent
            command:
            - cat
            tty: true
            volumeMounts:
            - name: docker-auth
              mountPath: /root/.docker
          - name: openjdk-11-builder
            image: openjdk:11-jdk-slim-buster
            imagePullPolicy: IfNotPresent
            command:
            - cat
            tty: true
            volumeMounts:
            - name: maven-data
              mountPath: /root/.m2
            resources:
              requests:
                cpu: 1
                memory: 1Gi
              limits:
                cpu: 1
                memory: 1Gi
          - name: kaniko
            image: gcr.io/kaniko-project/executor:v1.5.2-debug
            imagePullPolicy: IfNotPresent
            command:
            - /busybox/cat
            tty: true
            volumeMounts:
            - name: docker-auth
              mountPath: /kaniko/.docker
            resources:
              requests:
                cpu: 1
                memory: 1Gi
              limits:
                cpu: 1
                memory: 1Gi
        """.stripIndent()
        slaveConnectTimeout 400
    }
  }
  stages {
    stage('Build image and push') {
      steps {
        dir('HONEUR-Security') {
          checkout([$class: 'GitSCM', branches: [[name: 'develop']], extensions: [], userRemoteConfigs: [[credentialsId: 'susverwimp-github-credentials', url: 'https://github.com/solventrix/HONEUR-Security.git']]])
        }
        dir('HONEUR-Common') {
          checkout([$class: 'GitSCM', branches: [[name: 'develop']], extensions: [], userRemoteConfigs: [[credentialsId: 'susverwimp-github-credentials', url: 'https://github.com/solventrix/HONEUR-Common.git']]])
        }
        container('aws-cli') {
            sh '''\
            PASSWORD=$(aws ecr get-login-password --region eu-west-1)
            USERNAME=AWS
            BASE64=$(echo -n "$USERNAME:$PASSWORD" | base64 -w 0)
            printf '{"auths":{"973455288590.dkr.ecr.eu-west-1.amazonaws.com":{"auth": "%s"}}}' "$BASE64" > /root/.docker/config.json
            cat /root/.docker/config.json
            '''.stripIndent()
        }
        container('openjdk-11-builder') {
          withCredentials([string(credentialsId: 'sonarqube-admin-key', variable: 'SONARQUBE_KEY')]) {
            dir('HONEUR-Security') {
              sh './build.sh'
            }
            dir('HONEUR-Common') {
              sh './build.sh'
            }
            sh './build_image_central.sh'
          }
        }
        container('kaniko') {
          sh "/kaniko/executor -f \$(pwd)/Dockerfile -c \$(pwd) --cache=true --destination=973455288590.dkr.ecr.eu-west-1.amazonaws.com/honeur/webapi:latest"
        }
      }
    }
  }
}
