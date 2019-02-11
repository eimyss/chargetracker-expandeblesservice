podTemplate(label: 'buildpod', containers: [
    containerTemplate(name: 'maven', image: 'maven:3.3.9-jdk-8-alpine', command: 'cat', ttyEnabled: true),
    containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
    containerTemplate(name: 'kubectl', image: 'jorgeacetozi/jenkins-slave-kubectl', command: 'cat', ttyEnabled: true)
],
    volumes: [
        hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
    ]
) {
    node('buildpod') {

 environment {
     APP = "expensable"
   }

  stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git branch: 'kubernetes', url: 'https://github.com/eimyss/chargetracker-expandeblesservice.git'
   }

        stage('Maven Build') {
            container('maven') {
                    sh 'mvn clean install -Dmaven.test.skip'
            }
        }

        stage('Build image') {
            container('docker') {
                // example to show you can run docker commands when you mount the socket
                //sh 'docker ps'
                echo 'build nr is: $BUILD_NUMBER'

                    sh 'docker build . -t eimyss/expenses-${APP}:1.$BUILD_NUMBER'

                withCredentials([string(credentialsId: 'docker-pass', variable: 'PW1')]) {
                    echo "My password is '${PW1}'!"
                    sh 'docker login -u=testuser -p=${PW1} docker-registry.eimantas.server'
                }
                sh 'docker tag eimyss/expenses-${APP}:1.$BUILD_NUMBER docker-registry.eimantas.server/expenses-${APP}:1.$BUILD_NUMBER'
                sh 'docker push docker-registry.eimantas.server/expenses-${APP}:1.$BUILD_NUMBER'
            }
        }

                container('kubectl') {
            stage('Kube update deployment') {
                withCredentials([string(credentialsId: 'kube-ca', variable: 'PW1')]) {
                    withKubeConfig(caCertificate: '${PW1}', clusterName: 'kubernetes', contextName: 'jenkins', credentialsId: '37047961-56be-4bf1-a36e-f4651a546fe4', serverUrl: 'https://192.168.123.243:6443') {
                        sh 'kubectl --insecure-skip-tls-verify="true"  get pods'
                        sh 'kubectl --insecure-skip-tls-verify="true" set image deployment.v1.apps/${APP} expenses=docker-registry.eimantas.server/expenses-${APP}:1.$BUILD_NUMBER --record=true -n expenses'

                    }
                }

            }
        }
    }
}